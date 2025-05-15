package com.example.screenreadertest

import android.accessibilityservice.AccessibilityService
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
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
import java.util.Calendar

class MyAccessibilityService : AccessibilityService() {
    private var lastDetectedAmount: Int = 0
    private val handler = Handler(Looper.getMainLooper())
    private var lastCheckTime = 0L
    private var overlayView: View? = null
    private var centerPopupView: View? = null

    private var isConfirmed = false
    private var targetButtonNode: AccessibilityNodeInfo? = null
    private var ignoreUntil: Long = 0L
    private var backgroundOverlayView: View? = null

    private var isDeliver = false;
    private var lastNoClickTime = 0L

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
//            "com.fineapp.yogiyo"
        )

//        if (packageName !in targetApps) return

        // 주문 완료 감지
        if (isDeliver && event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            rootInActiveWindow?.let { node ->
                if (checkOrderConfirmedByTime(node)) {
                    val amount = lastDetectedAmount

                    DeliveryEventManager.appendEvent(applicationContext, amount, true)
                    Log.d("AccessibilityService", "✅ 최종 주문 기록 완료")

                    isDeliver = false  // 중복 방지용 리셋
                }
            }
        }

        // 결제하기 버튼 탐지
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOWS_CHANGED -> {
                Log.d(
                    "AccessibilityService",
                    "Event type: ${event.eventType}, Package: $packageName"
                )
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
                            Log.d("AccessibilityService", "checkbuttons")
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

        if (currentTime < ignoreUntil) {
            Log.d("AccessibilityService", "쿨다운 중으로 버튼 탐지 무시")
            return
        }
        if (isConfirmed) {
            Log.d("AccessibilityService", "쿨다운 종료 → isConfirmed = false")
            isConfirmed = false
        }

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
                    if (isConfirmed) {
                        currentNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    } else {
                        targetButtonNode = currentNode // 클릭을 위해 저장
                        blockButtonWithOverlay(rect)
                    }
                    val amount = extractAmountFromText(nodeText)
                    if (amount > 0) {
                        Log.d("AccessibilityService", "🎯 버튼 텍스트에서 금액 추출: ${amount}원")
                        lastDetectedAmount = amount
                    }

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
//           setBackgroundColor(Color.argb(0, 255, 0, 0))
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

        Log.d("AccessibilityService", "blockButtonWithOverlay" )
        val (screenWidth, screenHeight) = getScreenSize()
        Log.d("AccessibilityService", "blockButtonWithOverlay, h: $screenHeight")
        Log.d("AccessibilityService", "Rect, $rect")

        val fullInsets = windowManager.currentWindowMetrics
            .windowInsets
            .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())

        val layoutParams = WindowManager.LayoutParams(
            rect.width(),
            rect.height(),
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            val navSize = windowManager.currentWindowMetrics
                .windowInsets.getInsets(WindowInsets.Type.systemBars())
            this.x = rect.left
//            this.y = rect.top
            if (hasSoftNavigationBar())
                this.y = rect.top - fullInsets.top  // softkey 있는 환경
            else
                this.y = rect.top

            Log.d("AccessibilityService", "NavSize $navSize")
            Log.d("AccessibilityService", "SNav ${hasSoftNavigationBar()}")
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

    private fun getScreenSize(): Pair<Int, Int> {
        val windowMetrics = windowManager.currentWindowMetrics
        val insets1 = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        val insets2 = windowMetrics.windowInsets.getInsets(WindowInsets.Type.systemBars())
        val width = windowMetrics.bounds.width() - insets2.left - insets2.right
        val height = windowMetrics.bounds.height() - insets2.top - insets2.bottom

        Log.d("AccessibilityService", "getInsetsIgn: ${insets1.bottom}, getInsets: ${insets2.bottom}")
        Log.d("AccessibilityService", "getInsetsIgn: $insets1, getInsets: $insets2")
        Log.d("AccessibilityService", "bounds: ${windowMetrics.bounds}")
        Log.d("AccessibilityService", "bounds: w:${windowMetrics.bounds.width()}, h:${windowMetrics.bounds.height()}")

        return width to height
    }

    private fun hasSoftNavigationBar(): Boolean {
        val metrics = windowManager.currentWindowMetrics
        val navInsets = metrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars())
        return navInsets.bottom > 80
    }

//    private fun getStatusBarHeight(context: Context): Int {
//        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
//        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
//    }
//
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
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(overlayView, overlayParams)
    }

    private fun showCenterPopup() {
        if (centerPopupView != null) return

        // 🔹 배경 오버레이 (반투명)
        backgroundOverlayView = View(this).apply {
            setBackgroundColor(Color.argb(120, 0, 0, 0))
        }

        val bgParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }
        windowManager.addView(backgroundOverlayView, bgParams)

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val yearMonth = "$year-$month"

        // (stopCount, stopAmount, orderCount, orderAmount)
        val (_, _, orderCount, orderAmount) = DeliveryEventManager.getMonthlyStats(applicationContext, yearMonth)

        // 🔹 강조 텍스트
        val summaryText = "${month}월 동안 배달 ${orderCount}회, ${"%,d".format(orderAmount)}원 사용\n"

        val summarySpannable = SpannableString(summaryText).apply {
            val boldTarget = "${orderCount}회"
            val start = indexOf(boldTarget)
            if (start >= 0) {
                setSpan(StyleSpan(Typeface.BOLD), start, start + boldTarget.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        // 🔹 텍스트 뷰들
        val summaryTextView = TextView(this).apply {
            text = summarySpannable
            textSize = 18f // 🔹 기존 14f에서 증가
            setTextColor(Color.BLACK) // 🔹 글자 색 더 선명하게
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20) // 🔹 아래 여백 추가
        }

        val questionTextView = TextView(this).apply {
            text = "정말 주문하시겠습니까?"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 40)
        }

        // 🔹 버튼 가로 배치
        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END // ← 오른쪽 정렬로 변경
            setPadding(0, 20, 0, 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val yesButton = Button(this).apply {
            text = "네"
            textSize = 16f
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 20f // ← 모서리 둥글게
                setTextColor(Color.BLACK)
            }
            elevation = 0f // 🔹 그림자 없애기
            stateListAnimator = null
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = 8 }
            setPadding(12,4,12,4)
            setOnClickListener {
                isConfirmed = true
                isDeliver = true
                ignoreUntil = System.currentTimeMillis() + 10_000
                removeOverlay()
                removeCenterPopup()
                overlayView?.isEnabled = false
                targetButtonNode?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                targetButtonNode = null
            }
        }

        val noButton = Button(this).apply {
            text = "아니요"
            textSize = 16f
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 20f // ← 모서리 둥글게
                setColor(Color.parseColor("#00C4C4"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = 8 }
            setPadding(12,4,12,4)
            setOnClickListener {
                val now = System.currentTimeMillis()
                if (now - lastNoClickTime < 5 * 60 * 1000) {
                    Log.d("AccessibilityService", "쿨다운 중: 아니요 클릭 무시")
                    removeCenterPopup()
                    return@setOnClickListener
                }
                lastNoClickTime = now
                val amount = lastDetectedAmount
                DeliveryEventManager.appendEvent(applicationContext, amount, false)
                removeCenterPopup()
            }
        }

        buttonRow.addView(yesButton)
        buttonRow.addView(noButton)


        // 🔹 팝업 뷰 구성
        val popup = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(40, 40, 40, 40)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                cornerRadius = 30f
                setColor(Color.WHITE)
            }
            layoutParams = WindowManager.LayoutParams(
                800, // ← 고정 너비 (또는 WRAP_CONTENT로 넉넉하게)
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
            }

            addView(summaryTextView)
            addView(questionTextView)
            addView(buttonRow)
        }

        val params = WindowManager.LayoutParams(
            750,  // ← 팝업 가로 크기 (픽셀) 직접 지정
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
        backgroundOverlayView?.let {
            windowManager.removeView(it)
            backgroundOverlayView = null
        }
    }

    /**
     * 실제 배달 주문 확인을 위하여 '(정수)분' 키워드 확인
     */
    private fun extractAmountFromText(text: String?): Int {
        if (text.isNullOrBlank()) return 0
        val regex = Regex("""([\d,]+)\s*원""")
        val match = regex.find(text)
        return match?.groupValues?.get(1)?.replace(",", "")?.toIntOrNull() ?: 0
    }

    private fun checkOrderConfirmedByTime(node: AccessibilityNodeInfo): Boolean {
        val stack = mutableListOf(node)
        val regex = Regex("""\d{1,3}\s*분""")  // 1~3자리 숫자 + "분"

        while (stack.isNotEmpty()) {
            val current = stack.removeAt(stack.lastIndex)
            val text = current.text?.toString() ?: continue

            if (regex.containsMatchIn(text)) {
                Log.d("AccessibilityService", "주문 완료 감지됨 (예상시간: $text)")
                return true
            }

            for (i in 0 until current.childCount) {
                current.getChild(i)?.let { stack.add(it) }
            }
        }

        return false
    }
}