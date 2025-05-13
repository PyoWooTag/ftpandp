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
        val ym = YearMonth.now().toString()  // "2025-05"
        val stats = DeliveryEventManager.getMonthlyStats(context, ym)

        return mapOf(
            "orderCount" to stats.orderCount.toString(),
            "orderAmount" to String.format("%,d", stats.orderAmount),
            "stopCount" to stats.stopCount.toString(),
            "savedAmount" to String.format("%,d", stats.savedAmount)
        )
    }
}