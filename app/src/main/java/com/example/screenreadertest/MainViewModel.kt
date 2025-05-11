package com.example.screenreadertest

import android.content.Context
import androidx.lifecycle.ViewModel
import org.json.JSONObject
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class MainViewModel : ViewModel() {

    fun getThisMonthStats(context: Context): Map<String, String> {
        val json = context.assets.open("monthly_stats.json")
            .bufferedReader().use { it.readText() }
        val obj = JSONObject(json)

        val currentMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val data = obj.getJSONObject(currentMonth)

        return mapOf(
            "orderCount" to data.getInt("orderCount").toString(),
            "orderAmount" to String.format("%,d", data.getInt("orderAmount")),
            "stopCount" to data.getInt("stopCount").toString(),
            "savedAmount" to String.format("%,d", data.getInt("savedAmount"))
        )
    }
}