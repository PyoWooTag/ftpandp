import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.screenreadertest.LocalStatsManager
import com.example.screenreadertest.StatsMerger
import com.example.screenreadertest.getLastFiveMonths
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class MonthlyData(val month: String, val value: Int)

class DetailViewModel : ViewModel() {

    private fun parseData(context: Context): JSONObject? {
        return try {
            val jsonStr = context.assets.open("monthly_stats.json").bufferedReader().use { it.readText() }
            JSONObject(jsonStr)
        } catch (e: Exception) {
            null // 더미 파일 없을 경우 null 반환
        }
    }

    fun getGraphData(context: Context, key: String): List<MonthlyData> {
        val merged = StatsMerger.getMonthlyMergedStats(context, key)
        return merged.toSortedMap().map { MonthlyData(it.key, it.value) }
    }
}