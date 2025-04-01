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
//import android.view.WindowInsets
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo

class MyAccessibilityService : AccessibilityService() {

//    private val handler = Handler(Looper.getMainLooper())
    private var lastCheckTime = 0L
    private var overlayView: View? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("AccessibilityService", "MyAccessibilityService가 시작됨")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) {
            Log.e("AccessibilityService", "Event is Null")
            return
        }

        val targetApps = setOf(
            "com.example.screenreadertest",
            "com.coupang.mobile.eats",
            "com.sampleapp",
            "com.fineapp.yogiyo"
        )

        val packageName = event.packageName?.toString()
        if (packageName == null) {
            Log.e("AccessibilityService", "❌ packageName is null")
            return
        }

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // ✅ 앱이 백그라운드로 가는 경우 감지
            if (packageName !in targetApps) {
                if (overlayView != null) {  // ✅ 오버레이가 존재할 때만 제거
                    Log.d("AccessibilityService", "🛑 앱이 변경됨. 오버레이 제거")
                    removeOverlay(this)
                }
            }
            return
        }

        if (packageName in targetApps) {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
//                event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED ||
//                event.eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED ||
                event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED
            ) {
                val rootNode = rootInActiveWindow
                if (rootNode == null) {
                    Log.e("AccessibilityService", "rootInActiveWindow is null!")
                    return
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    checkButtons(rootNode, this)
                }, 200)
            }
        }

    }

    private fun checkButtons(node: AccessibilityNodeInfo?, service: AccessibilityService) {
        if (node == null) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCheckTime < 100) {
//            Log.d("AccessibilityService", "🔄 너무 자주 실행됨: 실행 건너뜀")
            return
        }
        lastCheckTime = currentTime

        val stack = mutableListOf(node)
        var foundButton = false

        while (stack.isNotEmpty()) {
            val currentNode = stack.removeAt(stack.lastIndex)

            val className = currentNode.className?.toString()
            val nodeText = currentNode.text?.toString()
            val isBtn = className?.contains("Button") == true
            val isTV = className?.contains("TextView") == true

            if ((isBtn || isTV) && (nodeText?.contains("결제하기") == true || nodeText?.contains("Pay") == true)) {
                if (!currentNode.isVisibleToUser) {
                    Log.w("AccessibilityService", "❌ 버튼이 화면에서 보이지 않음: $nodeText")
                    continue
                }

                val rect = Rect()
                currentNode.getBoundsInScreen(rect)

                if (rect.top == rect.bottom) {
                    Log.w("AccessibilityService", "⚠️ 버튼 높이가 0임. 부모 노드에서 위치 다시 가져오기.")
                    currentNode.parent?.getBoundsInScreen(rect)
                }

                if (rect.top != rect.bottom) {
                    Log.d("AccessibilityService", "🚀 차단할 버튼 위치: $rect")
                    blockButtonWithOverlay(service, rect)
                    foundButton = true
                } else {
                    Log.e("AccessibilityService", "🚨 위치 탐지 실패: $nodeText")
                    foundButton = false
                }
            }

            for (i in 0 until currentNode.childCount) {
                currentNode.getChild(i)?.let { stack.add(it) }
            }
        }

        if (!foundButton) {
            removeOverlay(service)
        }
    }


    private fun blockButtonWithOverlay(service: AccessibilityService, rect: Rect) {
        val windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        overlayView?.let {
            // 이전 오버레이가 존재하면 제거
            windowManager.removeView(it)
        }

        overlayView = View(service).apply {
            setBackgroundColor(Color.argb(150, 255, 0, 0)) // 반투명 빨강
            isClickable = true  // 클릭 가능하도록 설정
            isFocusable = true  // 포커스를 받을 수 있도록 설정

            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    Log.d("AccessibilityService", "🚫 클릭 차단됨")
                }
                true // 터치 이벤트 차단
            }
        }

        // 화면 크기 가져오기
        val display = windowManager.defaultDisplay
        val screenWidth = display.width
        val screenHeight = display.height

        // 중앙 원점으로 계산: 화면 중앙을 기준으로 상대적 위치 계산
        val x = 0
        val y = rect.top - screenHeight / 2 + (rect.bottom - rect.top) / 2

        Log.d("AccessibilityService", "blockButtonWithOverlay: screenWidth = $screenWidth, screenHeight = $screenHeight")
        Log.d("AccessibilityService", "rect.left = ${rect.left}, rect.top = ${rect.top}, x = $x, y = $y")

        val layoutParams = WindowManager.LayoutParams(
            rect.width(),
            rect.height(),
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            this.x = x
            this.y = y
        }

        windowManager.addView(overlayView, layoutParams)
    }

    private fun removeOverlay(service: AccessibilityService) {
        overlayView?.let {
            val windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.removeView(it)
            overlayView = null // ✅ 뷰 객체도 제거
            Log.d("AccessibilityService", "🛑 오버레이 제거 완료")
        }
    }

    private fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
    }

    private fun getNavigationBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
    }

    override fun onInterrupt() {
        Log.d("AccessibilityService", "서비스 중단됨")
    }
}
