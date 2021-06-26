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

class SkipperService : AccessibilityService() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "onServiceConnected: ")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (rootInActiveWindow == null) return
        Log.d(TAG, "onAccessibilityEvent: ${event?.packageName} $event")
        val root = rootInActiveWindow
        var targetNodes = when (root.packageName) {
            "tv.danmaku.bili" -> root.findAccessibilityNodeInfosByViewId("tv.danmaku.bili:id/count_down")
            "com.zhihu.android" -> root.findAccessibilityNodeInfosByViewId("com.zhihu.android:id/btn_skip")
            "com.gelonghui.glhapp" -> root.findAccessibilityNodeInfosByViewId("com.gelonghui.glhapp:id/skip")
            "com.jd.app.reader" -> root.findAccessibilityNodeInfosByViewId("com.jd.app.reader:id/count_down_time")
            "com.tencent.qqlive" -> root.findAccessibilityNodeInfosByText("跳过")
            else -> root.findAccessibilityNodeInfosByText("跳过")
        }
        if (targetNodes.isNullOrEmpty()) {
            targetNodes = root.findAccessibilityNodeInfosByText("跳过")
        }
        if (targetNodes.isNullOrEmpty()) {
            targetNodes = root.findAccessibilityNodeInfosByText("Skip")
        }
        if (targetNodes.isNullOrEmpty()) {
            return
        }
        val targetNode = targetNodes[0]
        Log.d(TAG, "onAccessibilityEvent: target viewId: ${targetNode.viewIdResourceName}")
        if (targetNode.isClickable) {
            val result = targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d(TAG, "onAccessibilityEvent: target clicked: $result")
        } else {
            val rect = Rect()
            targetNode.getBoundsInScreen(rect)
            clickOnScreen((rect.left + rect.right) / 2.0f, (rect.top + rect.bottom) / 2.0f, null, null)
        }
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
        Log.d(TAG, "onInterrupt: ")
    }


    companion object {
        private const val TAG = "SkipperService"
    }
}