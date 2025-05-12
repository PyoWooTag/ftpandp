package com.example.screenreadertest

import android.content.Context
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class LocalStatsManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("stats_prefs", Context.MODE_PRIVATE)

    private fun getKey(field: String): String {
        val month = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        return "${month}:$field"
    }

    fun increment(field: String, by: Int) {
        val key = getKey(field)
        val current = prefs.getInt(key, 0)
        prefs.edit().putInt(key, current + by).apply()
    }

    fun get(field: String): Int {
        val key = getKey(field)
        return prefs.getInt(key, 0)
    }

    fun getMonthStats(): Map<String, Int> {
        val fields = listOf("orderCount", "orderAmount", "stopCount", "savedAmount")
        return fields.associateWith { get(it) }
    }

    fun getAllMonths(): Map<String, Map<String, Int>> {
        return prefs.all.mapNotNull { (key, value) ->
            val parts = key.split(":")
            if (parts.size == 2) parts[0] to (parts[1] to value as Int) else null
        }.groupBy({ it.first }, { it.second })
            .mapValues { entry -> entry.value.toMap() }
    }

    fun getMonthlyStats(key: String): Map<String, Int> {
        val result = mutableMapOf<String, Int>()

        try {
            for ((fullKey, value) in prefs.all) {
                if (fullKey.endsWith(":$key")) {
                    val month = fullKey.removeSuffix(":$key")
                    val intValue = (value as? Int) ?: continue
                    result[month] = intValue
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }
}

