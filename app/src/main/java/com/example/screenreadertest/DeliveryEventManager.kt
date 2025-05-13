package com.example.screenreadertest

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.time.OffsetDateTime

object DeliveryEventManager {
    private const val FILE_NAME = "ordered_data.json"

    // 반환 데이터 구조
    data class MonthlyStats(
        val stopCount: Int,       // 팝업 차단 후 '아니요' 선택
        val savedAmount: Int,     // 차단된 주문 총 금액
        val orderCount: Int,      // 실제 주문한 횟수
        val orderAmount: Int      // 실제 주문한 총 금액
    )
    
    // 내부 저장소의 ordered_data.json 파일을 반환하거나 초기화
    private fun getDataFile(context: Context): File {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) {
            file.writeText("[]") // 초기화
        }
        return file
    }

    // 새로운 주문 이벤트 추가
    fun appendEvent(context: Context, amount: Int, ordered: Boolean) {
        val file = getDataFile(context)
        val currentArray = JSONArray(file.readText())

        val event = JSONObject().apply {
            put("orderDate", OffsetDateTime.now().toString())
            put("orderAmount", amount)
            put("ordered", if (ordered) 1 else 0)
        }

        currentArray.put(event)
        FileWriter(file, false).use { it.write(currentArray.toString(2)) }
    }

    // 모든 이벤트 불러오기
    fun readAllEvents(context: Context): JSONArray {
        val file = getDataFile(context)
        return JSONArray(file.readText())
    }
    
    // 통계 반환
    fun getMonthlyStats(context: Context, yearMonth: String): MonthlyStats {
        val events = readAllEvents(context)

        var stopCount = 0
        var savedAmount = 0
        var orderCount = 0
        var orderAmount = 0

        for (i in 0 until events.length()) {
            val event = events.getJSONObject(i)
            val date = event.optString("orderDate")
            val amount = event.optInt("orderAmount", 0)
            val ordered = event.optInt("ordered", 0)

            if (date.startsWith(yearMonth)) {
                if (ordered == 1) {
                    orderCount++
                    orderAmount += amount
                } else {
                    stopCount++
                    savedAmount += amount
                }
            }
        }

        return MonthlyStats(
            stopCount = stopCount,
            savedAmount = savedAmount,
            orderCount = orderCount,
            orderAmount = orderAmount
        )
    }
}
