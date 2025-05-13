package com.example.screenreadertest

import android.content.Context
import androidx.lifecycle.ViewModel
import org.json.JSONObject
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class MonthlyData(val month: String, val value: Int)

class DetailViewModel : ViewModel() {
    fun getGraphData(context: Context, metric: String): List<MonthlyData> {
        val result = mutableMapOf<String, Int>()

        try {
            val arr = DeliveryEventManager.readAllEvents(context)

            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val date = OffsetDateTime.parse(
                    obj.getString("orderDate"),
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
                )
                val ym = YearMonth.from(date).toString()
                val amount = obj.optInt("orderAmount", 0)
                val ordered = obj.optInt("ordered", 0)

                val value = when (metric) {
                    "stopCount" -> if (ordered == 0) 1 else 0
                    "savedAmount" -> if (ordered == 0) amount else 0
                    "orderCount" -> if (ordered == 1) 1 else 0
                    "orderAmount" -> if (ordered == 1) amount else 0
                    else -> 0
                }

                result[ym] = result.getOrDefault(ym, 0) + value
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result.toSortedMap().map { MonthlyData(it.key, it.value) }
    }
}