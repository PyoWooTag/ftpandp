package com.example.screenreadertest

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class MainViewModel : ViewModel() {
    fun getThisMonthStats(context: Context): Map<String, String> {
        val currentMonth = YearMonth.now()

        // 1. 로컬 데이터
        val local = LocalStatsManager(context).getMonthStats()

        // 2. 더미 데이터 병합
        val dummy = try {
            val text =
                context.assets.open("ordered_data.json").bufferedReader().use { it.readText() }
            val arr = JSONArray(text)

            var orderCount = 0
            var stopCount = 0
            var orderAmount = 0
            var savedAmount = 0

            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val dateStr = obj.getString("orderDate")
                val date = OffsetDateTime.parse(
                    dateStr,
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
                )  // ✅ 여기 고침
                val amount = obj.getInt("orderAmount")
                val ordered = obj.getInt("ordered") == 1

                if (YearMonth.from(date) == currentMonth) {
                    if (ordered) {
                        orderCount += 1
                        orderAmount += amount
                    } else {
                        stopCount += 1
                        savedAmount += amount
                    }
                }
            }

            mapOf(
                "orderCount" to orderCount,
                "orderAmount" to orderAmount,
                "stopCount" to stopCount,
                "savedAmount" to savedAmount
            )
        } catch (e: Exception) {
            Log.e("MainViewModel", "더미 JSON 파싱 실패: ${e.message}")
            mapOf(
                "orderCount" to 0,
                "orderAmount" to 0,
                "stopCount" to 0,
                "savedAmount" to 0
            )
        }

        // 3. 병합
        val merged = mapOf(
            "orderCount" to (local["orderCount"] ?: 0) + (dummy["orderCount"] ?: 0),
            "orderAmount" to (local["orderAmount"] ?: 0) + (dummy["orderAmount"] ?: 0),
            "stopCount" to (local["stopCount"] ?: 0) + (dummy["stopCount"] ?: 0),
            "savedAmount" to (local["savedAmount"] ?: 0) + (dummy["savedAmount"] ?: 0)
        )

        Log.d("MainViewModel", "✅ 병합된 통계: $merged")

        // 4. UI용 문자열 포맷
        return mapOf(
            "orderCount" to merged["orderCount"].toString(),
            "orderAmount" to String.format("%,d", merged["orderAmount"] ?: 0),
            "stopCount" to merged["stopCount"].toString(),
            "savedAmount" to String.format("%,d", merged["savedAmount"] ?: 0)
        )

    }
}