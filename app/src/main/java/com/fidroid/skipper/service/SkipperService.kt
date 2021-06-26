package com.fidroid.skipper.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import com.fidroid.skipper.R
import com.fidroid.skipper.data.defaultAppsData
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
            packageNames = packageNames.plusElement("com.ss.android.article.news")
        }
        this.serviceInfo = myServiceInfo
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Timber.d("onAccessibilityEvent: ${event?.packageName} $event")
        val root = rootInActiveWindow ?: return
        var targetNodes: List<AccessibilityNodeInfo>? = null
        if (!root.packageName.isNullOrEmpty() && defaultAppsData.containsKey(root.packageName))
            targetNodes = root.findAccessibilityNodeInfosByViewId(defaultAppsData[root.packageName])
        if (targetNodes.isNullOrEmpty()) {
            targetNodes = root.findAccessibilityNodeInfosByText(this.getString(R.string.skip))
        }
        if (targetNodes.isNullOrEmpty()) {
            targetNodes = root.findAccessibilityNodeInfosByText(this.getString(R.string.close))
        }
        if (targetNodes.isNullOrEmpty()) {
            targetNodes = root.findAccessibilityNodeInfosByText(this.getString(R.string.i_know))
        }
        if (targetNodes.isNullOrEmpty()) {
            targetNodes = root.findAccessibilityNodeInfosByText(this.getString(R.string.skip_caps))
        }
        if (targetNodes.isNullOrEmpty()) {
            return
        }
        val targetNode = targetNodes[0]
        Timber.i("onAccessibilityEvent: target viewId: ${targetNode.viewIdResourceName}")
        when {
            targetNode.isClickable -> {
                val result = targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Timber.i("onAccessibilityEvent: target clicked: $result")
            }
            shouldClickNode(targetNode) -> {
                Timber.i("onAccessibilityEvent: target touch click")
                val rect = Rect()
                targetNode.getBoundsInScreen(rect)
                clickOnScreen((rect.left + rect.right) / 2.0f, (rect.top + rect.bottom) / 2.0f, null, null)
            }
            else -> {
                Timber.d("onAccessibilityEvent: can not found target")
            }
        }
    }

    private fun shouldClickNode(targetNode: AccessibilityNodeInfo): Boolean {
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
    }


    companion object {
        private const val TAG = "SkipperService"
    }
}