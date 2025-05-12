package com.example.screenreadertest

import android.content.Context
import android.util.Log
import org.json.JSONArray
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object StatsMerger {
    fun getMergedStats(context: Context): Map<String, Int> {
        val currentMonth = YearMonth.now()
        val local = LocalStatsManager(context).getMonthStats()

        val dummy = try {
            val text = context.assets.open("ordered_data.json").bufferedReader().use { it.readText() }
            val arr = JSONArray(text)

            var orderCount = 0
            var stopCount = 0
            var orderAmount = 0
            var savedAmount = 0

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX")

            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val dateStr = obj.getString("orderDate")
                val date = OffsetDateTime.parse(dateStr, formatter)
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
            Log.e("StatsMerger", "더미 JSON 파싱 실패: ${e.message}")
            mapOf(
                "orderCount" to 0,
                "orderAmount" to 0,
                "stopCount" to 0,
                "savedAmount" to 0
            )
        }

        return mapOf(
            "orderCount" to local["orderCount"]!! + dummy["orderCount"]!!,
            "orderAmount" to local["orderAmount"]!! + dummy["orderAmount"]!!,
            "stopCount" to local["stopCount"]!! + dummy["stopCount"]!!,
            "savedAmount" to local["savedAmount"]!! + dummy["savedAmount"]!!
        )
    }

    fun getMonthlyMergedStats(context: Context, key: String): Map<String, Int> {
        val result = mutableMapOf<String, Int>()

        // 1. 로컬 월별 데이터
        val local = LocalStatsManager(context).getMonthlyStats(key)
        for ((month, value) in local) {
            result[month] = (result[month] ?: 0) + value
        }

        // 2. 더미 ordered_data.json 데이터
        try {
            val dummyText = context.assets.open("ordered_data.json").bufferedReader().use { it.readText() }
            val arr = JSONArray(dummyText)

            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val date = LocalDateTime.parse(obj.getString("orderDate"), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                val ym = YearMonth.from(date).toString()
                val amount = obj.getInt("orderAmount")
                val ordered = obj.getInt("ordered") == 1

                val shouldInclude = when (key) {
                    "orderCount", "orderAmount" -> ordered
                    "stopCount", "savedAmount" -> !ordered
                    else -> false
                }
                if (!shouldInclude) continue

                val toAdd = if (key.contains("Count")) 1 else amount
                result[ym] = (result[ym] ?: 0) + toAdd
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }
}