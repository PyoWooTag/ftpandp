package com.example.screenreadertest

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import org.json.JSONObject
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class MainViewModel : ViewModel() {
    fun getThisMonthStats(context: Context): Map<String, String> {
        val currentMonth = YearMonth.now()
        val currentMonthEvents = mutableListOf<JSONObject>()

        try {
            val arr = DeliveryEventManager.readAllEvents(context)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val date = OffsetDateTime.parse(
                    obj.getString("orderDate"),
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
                )
                if (YearMonth.from(date) == currentMonth) {
                    currentMonthEvents.add(obj)
                }
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "이벤트 파싱 실패: ${e.message}")
        }

        val orderedList = currentMonthEvents.filter { it.getInt("ordered") == 1 }
        val stoppedList = currentMonthEvents.filter { it.getInt("ordered") == 0 }

        return mapOf(
            "orderCount" to orderedList.size.toString(),
            "orderAmount" to String.format("%,d", orderedList.sumOf { it.getInt("orderAmount") }),
            "stopCount" to stoppedList.size.toString(),
            "savedAmount" to String.format("%,d", stoppedList.sumOf { it.getInt("orderAmount") })
        )
    }
}
