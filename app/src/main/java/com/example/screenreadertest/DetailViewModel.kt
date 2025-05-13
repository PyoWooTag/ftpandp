package com.example.screenreadertest

import android.content.Context
import androidx.lifecycle.ViewModel
import org.json.JSONObject
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class MonthlyData(val month: String, val value: Int)

class DetailViewModel : ViewModel() {
    fun getGraphData(context: Context, ordered: Boolean, valueOnly: Boolean): List<MonthlyData> {
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
                val amount = obj.getInt("orderAmount")
                val isOrdered = obj.getInt("ordered") == 1

                if (isOrdered != ordered) continue

                val toAdd = if (valueOnly) amount else 1
                result[ym] = (result[ym] ?: 0) + toAdd
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result.toSortedMap().map { MonthlyData(it.key, it.value) }
    }
}