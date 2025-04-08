package com.example.screenreadertest

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo

class MyAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var lastCheckTime = 0L
    private var overlayView: View? = null
    private lateinit var windowManager: WindowManager // WindowManager 미리 선언

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        val packageName = event.packageName?.toString() ?: return

        val targetApps = setOf(
            "com.example.screenreadertest",
            "com.coupang.mobile.eats",
            "com.sampleapp",
            "com.fineapp.yogiyo"
        )

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (packageName !in targetApps) {
                removeOverlay()
                return
            }
        }

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if (packageName !in targetApps) removeOverlay()
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_CLICKED,
            AccessibilityEvent.TYPE_VIEW_FOCUSED,
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                if (packageName in targetApps) {
                    rootInActiveWindow?.let { node ->
                        handler.postDelayed({ checkButtons(node) }, 200)
                    }
                }
            }
        }
    }


    private fun checkButtons(node: AccessibilityNodeInfo) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCheckTime < 200) return
        lastCheckTime = currentTime

        val stack = mutableListOf(node)
        var foundButton = false

        while (stack.isNotEmpty()) {
            val currentNode = stack.removeAt(stack.lastIndex)
            val className = currentNode.className?.toString()
            val nodeText = currentNode.text?.toString()
            val isButton = className?.contains("Button") == true
            val isTextView = className?.contains("TextView") == true

            if ((isButton || isTextView) && (nodeText?.contains("결제하기") == true || nodeText?.contains("Pay") == true)) {
                if (!currentNode.isVisibleToUser) continue

                val rect = Rect().apply { currentNode.getBoundsInScreen(this) }
                if (rect.top == rect.bottom) currentNode.parent?.getBoundsInScreen(rect)

                if (rect.top != rect.bottom) {
                    blockButtonWithOverlay(rect)
                    foundButton = true
                    break // 첫 번째 버튼만 차단
                }
            }

            for (i in 0 until currentNode.childCount) {
                currentNode.getChild(i)?.let { stack.add(it) }
                Log.d("AccessibilityService", "Check!")
            }
        }

        if (!foundButton) removeOverlay()
    }

    private fun blockButtonWithOverlay(rect: Rect) {
        removeOverlay() // 기존 오버레이 제거

        overlayView = View(this).apply {
            setBackgroundColor(Color.argb(150, 255, 0, 0))
            isClickable = true
            isFocusable = true
            setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    Log.d("AccessibilityService", "클릭 차단됨")
                    Log.d("AccessibilityService", "Checked!")
                    v.performClick()
                }
                true
            }
        }

        val (screenWidth, screenHeight) = getScreenSize()
        val x = 0
        val y = rect.top - screenHeight / 2

        val layoutParams = WindowManager.LayoutParams(
            rect.width(),
            rect.height(),
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            this.x = x
            this.y = y
        }

        windowManager.addView(overlayView, layoutParams)
    }

    private fun removeOverlay() {
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
    }

    private fun getScreenSize(): Pair<Int, Int> {
        val windowMetrics = windowManager.currentWindowMetrics
        val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        val width = windowMetrics.bounds.width() - insets.left - insets.right
        val height = windowMetrics.bounds.height() - insets.top - insets.bottom
        return width to height
    }

//    private fun getStatusBarHeight(context: Context): Int {
//        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
//        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
//    }

//    private fun getNavigationBarHeight(context: Context): Int {
//        val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
//        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
//    }

    override fun onInterrupt() {
        Log.d("AccessibilityService", "서비스 중단됨")
    }
}
