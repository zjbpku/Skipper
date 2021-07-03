package com.fidroid.skipper.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import com.fidroid.skipper.BuildConfig
import com.fidroid.skipper.R
import com.fidroid.skipper.cmt.events.SkipperServiceEvent
import com.fidroid.skipper.data.IdentityType
import com.fidroid.skipper.data.defaultAppsData
import org.greenrobot.eventbus.EventBus
import timber.log.Timber

class SkipperService : AccessibilityService() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Timber.tag(TAG)
        Timber.i("onServiceConnected: ")
        val myServiceInfo = this.serviceInfo.apply {
            packageNames = packageNames.plus(defaultAppsData.map { appInfo -> appInfo.packageName })
        }
        this.serviceInfo = myServiceInfo
        EventBus.getDefault().post(SkipperServiceEvent(true))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val root = rootInActiveWindow ?: return
        if (root.packageName.isNullOrEmpty()) return
//        if (BuildConfig.DEBUG) {
//            val targetNodes = parseAccessibilityNodes(root)
//            if (targetNodes.isNullOrEmpty()) return
//            val targetNode = targetNodes[0]
//            Timber.i("onAccessibilityEvent: target viewId: ${targetNode.viewIdResourceName}, parent: ${getAccessorName(targetNode)}")
//            performClickTargetNode(targetNode)
//        }
        val app = defaultAppsData.find { it.packageName == root.packageName }
        app?.let {
            it.skipViews.forEach { skipView ->
                if (!skipView.parentLayoutId.isNullOrEmpty() && root.findAccessibilityNodeInfosByViewId("${it.packageName}:${skipView.parentLayoutId}").isNullOrEmpty())
                    return@forEach
                val targetNodes = if (skipView.identityType == IdentityType.Id) {
                    root.findAccessibilityNodeInfosByViewId("${it.packageName}:${skipView.skipIdentity}")
                } else {
                    parseAccessibilityNodes(root)
                }
                if (targetNodes.isNullOrEmpty()) return@forEach
                val targetNode = targetNodes[0]
                Timber.d("onAccessibilityEvent: target viewId: ${targetNode.viewIdResourceName}, parentViewId: ${getAccessorName(targetNode)}")
                performClickTargetNode(targetNode)
            }
        }
    }

    private fun parseAccessibilityNodes(root: AccessibilityNodeInfo): List<AccessibilityNodeInfo>? {
        var targetNodes = root.findAccessibilityNodeInfosByText(this.getString(R.string.skip))
        if (targetNodes.isNullOrEmpty()) {
            targetNodes = root.findAccessibilityNodeInfosByText(this.getString(R.string.close))
        }
        if (targetNodes.isNullOrEmpty()) {
            targetNodes = root.findAccessibilityNodeInfosByText(this.getString(R.string.i_know))
        }
        if (targetNodes.isNullOrEmpty()) {
            targetNodes = root.findAccessibilityNodeInfosByText(this.getString(R.string.i_known))
        }
        if (targetNodes.isNullOrEmpty()) {
            targetNodes = root.findAccessibilityNodeInfosByText(this.getString(R.string.skip_caps))
        }
        if (targetNodes.isNullOrEmpty()) {
            targetNodes = root.findAccessibilityNodeInfosByText(this.getString(R.string.do_next_time))
        }
        return targetNodes
    }

    private fun getAccessorName(targetNode: AccessibilityNodeInfo?): String? {
        if (!BuildConfig.DEBUG) return targetNode?.parent?.viewIdResourceName
        var parent = targetNode?.parent
        while (parent != null && parent.hashCode() != rootInActiveWindow?.hashCode() && parent.viewIdResourceName == null) {
            parent = targetNode?.parent
        }
        return parent?.viewIdResourceName
    }

    private fun performClickTargetNode(targetNode: AccessibilityNodeInfo) {
        when {
            targetNode.className == "android.widget.TextView" && targetNode.text.isNullOrEmpty() -> return
            targetNode.isClickable -> {
                val result = targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Timber.d("onAccessibilityEvent: target clicked: $result")
            }
            targetNode.parent != null && targetNode.parent.isClickable -> {
                Timber.d("onAccessibilityEvent: target parent clicked: ${targetNode.text}")
//                if (targetNode.packageName == "com.qiyi.video") {
//                    if (targetNode.text == getString(R.string.close)) {
//                        targetNode.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
//                    }
//                } else {
                targetNode.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
//                }
            }
            shouldClickNode(targetNode) -> {
                Timber.d("onAccessibilityEvent: target touch click")
                val rect = Rect()
                targetNode.getBoundsInScreen(rect)
                clickOnScreen((rect.left + rect.right) / 2.0f, (rect.top + rect.bottom) / 2.0f, null, null)
            }
        }
    }

    private fun shouldClickNode(targetNode: AccessibilityNodeInfo): Boolean {
        if (targetNode.text.isNullOrEmpty()) return false
        val skip = getString(R.string.skip)
        val close = getString(R.string.close)
        val text = targetNode.text.trim()
        return text.startsWith(skip) || text.endsWith(skip) || text == skip ||
                text.startsWith(close) || text.endsWith(close) || text == close

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun gestureOnScreen(path: Path, startTime: Long = 0, duration: Long = 100, callback: GestureResultCallback?, handler: Handler? = null) {
        val builder = GestureDescription.Builder()
        builder.addStroke(GestureDescription.StrokeDescription(path, startTime, duration))
        val gesture = builder.build()
        Timber.w("dispatchGesture")
        dispatchGesture(gesture, callback, handler)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun clickOnScreen(x: Float, y: Float, callback: GestureResultCallback?, handler: Handler? = null) {
        val p = Path()
        p.moveTo(x, y)
        gestureOnScreen(p, callback = callback, handler = handler)
    }

    override fun onInterrupt() {
        Timber.w("onInterrupt: ")
        EventBus.getDefault().post(SkipperServiceEvent(false))
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.w("onDestroy: ")
        EventBus.getDefault().post(SkipperServiceEvent(false))
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Timber.w("onTaskRemoved: ")
        EventBus.getDefault().post(SkipperServiceEvent(false))
    }

    companion object {
        private const val TAG = "SkipperService"
    }
}