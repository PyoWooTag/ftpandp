package com.example.screenreadertest

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.time.OffsetDateTime

object DeliveryEventManager {
    private const val FILE_NAME = "ordered_data.json"

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
}
