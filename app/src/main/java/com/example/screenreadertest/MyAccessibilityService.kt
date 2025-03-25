package com.example.screenreadertest

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

class MyAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("AccessibilityService", "MyAccessibilityService가 시작됨")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

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
            checkButtons(rootNode)
        }
    }


    private fun checkButtons(node: AccessibilityNodeInfo?) {
        if (node == null) return  // null 체크 추가

        if (node.className?.toString() == "android.widget.Button") {
            Log.d("AccessibilityService", "버튼 감지: ${node.text}")
        }

        for (i in 0 until node.childCount) {
            val childNode = node.getChild(i)
            checkButtons(childNode) // null 체크된 상태에서 재귀 호출
        }
    }

    override fun onInterrupt() {
        Log.d("AccessibilityService", "서비스 중단됨")
    }
}
