package com.example.screenreadertest

import android.accessibilityservice.AccessibilityService
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MyAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var lastCheckTime = 0L
    private var overlayView: View? = null
    private var centerPopupView: View? = null
    private var isConfirmed = false         // overlay 생성 변수

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

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOWS_CHANGED -> {
                Log.d("AccessibilityService", "Event type: ${event.eventType}, Package: $packageName")
                if (packageName !in targetApps && !isAppInForeground(this))
                    removeOverlay()

                if (packageName in targetApps && packageName != "com.example.screenreadertest")
                    isConfirmed = false
            }

            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_CLICKED,
            AccessibilityEvent.TYPE_VIEW_FOCUSED,
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                if (packageName in targetApps) {
                    rootInActiveWindow?.let { node ->
                        handler.postDelayed({ checkButtons(node) }, 100)
                    }
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d("AccessibilityService", "서비스 중단됨")
    }

    private fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses
        runningAppProcesses?.let { processes ->
            for (process in processes) {
                if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return process.processName == "com.example.screenreadertest"
                }
            }
        }
        return false
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
            }
        }

        if (!foundButton) removeOverlay()
    }

    private fun blockButtonWithOverlay(rect: Rect) {
        removeOverlay()

        if (isConfirmed) return

        overlayView = View(this).apply {
            setBackgroundColor(Color.argb(150, 255, 0, 0))
            isClickable = true
            isFocusable = true
            setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    Log.d("AccessibilityService", "클릭 차단됨")
                    Log.d("AccessibilityService", "Checked!")
                    v.performClick()
//                    showPendingPopup()
                    showCenterPopup()
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


    private fun showPendingPopup() {
        removeOverlay()

        overlayView = View(this).apply {
            setBackgroundColor(Color.argb(120, 0, 0, 0)) // 반투명 검정
            isClickable = true
            isFocusable = true

            setOnClickListener {
                showCenterPopup()  // <- 오버레이 클릭 시 팝업 띄우기
            }
        }

        val overlayParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(overlayView, overlayParams)
    }

    private fun showCenterPopup() {
        if (centerPopupView != null) return

        val popup = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(40, 40, 40, 40)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            background = GradientDrawable().apply {
                cornerRadius = 30f
                setColor(Color.WHITE)
            }

            addView(TextView(context).apply {
                text = "정말 주문하시겠습니까?"
                textSize = 18f
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
            })

            addView(Button(context).apply {
                text = "네"
                setOnClickListener {
                    isConfirmed = true
                    removeOverlay()
                    removeCenterPopup()
                }
            })
            addView(Button(context).apply {
                text = "아니요"
                setOnClickListener {
                    val intent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)

                    removeOverlay()
                    removeCenterPopup()
                }
            })
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.CENTER
        windowManager.addView(popup, params)
        centerPopupView = popup
    }

    private fun removeCenterPopup() {
        centerPopupView?.let {
            windowManager.removeView(it)
            centerPopupView = null
        }
    }
}
