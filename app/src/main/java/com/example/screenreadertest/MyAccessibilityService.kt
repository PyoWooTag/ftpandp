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
    private var lastDetectedAmount: Int = 0
    private val handler = Handler(Looper.getMainLooper())
    private var lastCheckTime = 0L
    private var overlayView: View? = null
    private var centerPopupView: View? = null
    private var isConfirmed = false

    private lateinit var windowManager: WindowManager

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
                        handler.postDelayed({
                            checkButtons(node)
                        }, 100)
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
                    val amount = extractAmountFromText(nodeText)
                    if (amount > 0) {
                        Log.d("AccessibilityService", "🎯 버튼 텍스트에서 금액 추출: ${amount}원")
                        lastDetectedAmount = amount
                    }

                    blockButtonWithOverlay(rect)
                    foundButton = true
                    break
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
                    v.performClick()
                    showCenterPopup()
                }
                true
            }
        }

        val layoutParams = WindowManager.LayoutParams(
            rect.width(),
            rect.height(),
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            this.x = rect.left
            this.y = rect.top - windowManager.currentWindowMetrics.windowInsets.getInsets(WindowInsets.Type.systemBars()).top
            gravity = Gravity.TOP or Gravity.START
        }

        windowManager.addView(overlayView, layoutParams)
    }

    private fun removeOverlay() {
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
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
                    val amount = lastDetectedAmount
                    val manager = LocalStatsManager(applicationContext)
                    manager.increment("orderCount", 1)
                    manager.increment("orderAmount", amount)

                    isConfirmed = true
                    removeOverlay()
                    removeCenterPopup()
                }
            })
            addView(Button(context).apply {
                text = "아니요"
                setOnClickListener {
                    val amount = lastDetectedAmount
                    val manager = LocalStatsManager(applicationContext)
                    manager.increment("stopCount", 1)
                    manager.increment("savedAmount", amount)
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
        ).apply {
            gravity = Gravity.CENTER
        }

        windowManager.addView(popup, params)
        centerPopupView = popup
    }

    private fun removeCenterPopup() {
        centerPopupView?.let {
            windowManager.removeView(it)
            centerPopupView = null
        }
    }

    private fun extractAmountFromText(text: String?): Int {
        if (text.isNullOrBlank()) return 0
        val regex = Regex("""([\d,]+)[\s]*원""")
        val match = regex.find(text)
        return match?.groupValues?.get(1)?.replace(",", "")?.toIntOrNull() ?: 0
    }
}