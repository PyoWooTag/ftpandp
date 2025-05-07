package com.example.screenreadertest

import java.time.YearMonth
import java.time.format.DateTimeFormatter

fun getLastFiveMonths(): List<String> {
    val current = YearMonth.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
    return (4 downTo 0).map { current.minusMonths(it.toLong()).format(formatter) }
}