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
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo

class MyAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
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

        if (event.packageName in targetApps) {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED ||
                event.eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED ||
                event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
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

        val stack = mutableListOf<Pair<AccessibilityNodeInfo, Int>>() // (노드, 깊이)
        stack.add(node to 0)

        while (stack.isNotEmpty()) {
            val (currentNode, depth) = stack.removeAt(stack.lastIndex)

            val isBtn = currentNode.className?.toString()?.contains("Button") == true
            val isTV = currentNode.className?.toString()?.contains("TextView") == true
            val nodeText = currentNode.text?.toString()
            if ((isBtn || isTV)
                && (nodeText?.contains("결제하기") == true || nodeText?.contains("Pay") == true)) {

                if (!currentNode.isVisibleToUser) {
                    Log.w("AccessibilityService", "❌ 버튼이 화면에서 보이지 않음: ${nodeText}")
                    continue
                }

                Log.d("AccessibilityService", "🚀 차단할 버튼 감지: ${nodeText}, 깊이: $depth")

                val rect = Rect()
                currentNode.getBoundsInScreen(rect)
                Log.d("AccessibilityService", "btn rect.top = ${rect.top}")

                if (rect.top == rect.bottom) {
                    Log.w("AccessibilityService", "⚠️ 버튼 높이가 0임! 부모 노드에서 위치 다시 가져옴.")

                    var targetNode: AccessibilityNodeInfo? = currentNode.parent
                    while (targetNode != null) {
                        targetNode.getBoundsInScreen(rect)
                        if (rect.top != rect.bottom) {
                            Log.d("AccessibilityService", "✅ 부모 노드에서 올바른 위치 찾음: $rect")
                            break
                        }
                        targetNode = targetNode.parent
                    }
                }

                if (rect.top == rect.bottom) {
                    Log.e("AccessibilityService", "🚨 위치 탐지 실패: ${nodeText}, 위치=${rect}")
                    continue
                }

                Log.d("AccessibilityService", "📌 최종 버튼 위치: left=${rect.left}, top=${rect.top}, right=${rect.right}, bottom=${rect.bottom}")
                blockButtonWithOverlay(service, rect)

                // UI가 완전히 렌더링된 후 다시 확인 (100ms 후)
//                handler.postDelayed({
//                    val finalRect = Rect()
//                    currentNode.getBoundsInScreen(finalRect)
//                    Log.d("AccessibilityService", "🔄 다시 확인된 버튼 위치: $finalRect")
//                    blockButtonWithOverlay(service, finalRect)
//                }, 100)
            }

            for (i in 0 until currentNode.childCount) {
                val childNode = currentNode.getChild(i)
                if (childNode != null) {
                    stack.add(childNode to depth + 1)
                }
            }
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
        }

        // 화면 크기 가져오기
        val display = windowManager.defaultDisplay
        val screenWidth = display.width
        val screenHeight = display.height

        // 중앙 원점으로 계산: 화면 중앙을 기준으로 상대적 위치 계산
        val x = 0
        val y = rect.top - screenHeight/2 + (rect.bottom-rect.top)/2 - getNavigationBarHeight(service)
        
        Log.d("AccessibilityService", "blockButtonWithOverlay: screenWidth = $screenWidth, screenHeight = $screenHeight")
        Log.d("AccessibilityService", "rect.left = ${rect.left}, rect.top = ${rect.top}, x = $x, y = $y")

        val layoutParams = WindowManager.LayoutParams(
            rect.width(),
            rect.height(),
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            this.x = x
            this.y = y
        }

        windowManager.addView(overlayView, layoutParams)
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
