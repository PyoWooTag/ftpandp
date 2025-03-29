package com.example.screenreadertest

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

class MyAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("AccessibilityService", "MyAccessibilityService가 시작됨")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
//        Log.d("AccessibilityService", "🔍 onAccessibilityEvent 호출됨")
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
            Log.d("AccessibilityService", "타겟 앱 감지됨: ${event.packageName}")

            Log.d("AccessibilityService", "이벤트 감지됨: ${event.eventType}")

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
                    checkButtons(rootNode)
                }, 200)
            }
        }


    }


    private fun checkButtons(node: AccessibilityNodeInfo?) {
        if (node == null) return

        val stack = mutableListOf<Pair<AccessibilityNodeInfo, Int>>() // (노드, 깊이)
        stack.add(node to 0)

        while (stack.isNotEmpty()) {
            val (currentNode, depth) = stack.removeAt(stack.lastIndex) // 스택에서 pop

            Log.d("AccessibilityService", "현재 깊이: $depth, 노드: ${currentNode.className}, 텍스트: ${currentNode.text}")

            if (depth > 5) continue  // 깊이 제한

            for (i in 0 until currentNode.childCount) {
                val childNode = currentNode.getChild(i)
                if (childNode != null) {
                    stack.add(childNode to depth + 1)
                }
            }
        }

////
//
//        if (node.className?.toString() == "android.widget.Button" && node.text?.toString() == "주문하기") {
//            val buttonText = node.text ?: node.contentDescription  // contentDescription도 확인
//            Log.d("AccessibilityService", "버튼 감지: ${buttonText ?: "null"}, depth: $depth")
//        }
//
//        for (i in 0 until node.childCount) {
//            val childNode = node.getChild(i)
//            checkButtons(childNode, depth+1) // null 체크된 상태에서 재귀 호출
//        }
    }

    override fun onInterrupt() {
        Log.d("AccessibilityService", "서비스 중단됨")
    }
}
