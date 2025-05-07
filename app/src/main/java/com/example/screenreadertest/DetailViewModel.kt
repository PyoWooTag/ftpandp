import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.screenreadertest.getLastFiveMonths
import org.json.JSONObject

data class MonthlyData(val month: String, val value: Int)

class DetailViewModel : ViewModel() {

    private fun parseData(context: Context): JSONObject {
        val jsonStr = context.assets.open("monthly_stats.json").bufferedReader().use { it.readText() }
        return JSONObject(jsonStr)
    }

    fun getGraphData(context: Context, field: String): List<MonthlyData> {
        val json = parseData(context)
        val months = getLastFiveMonths()

        return months.map {
            val value = json.getJSONObject(it).getInt(field)
            MonthlyData(month = it.takeLast(2) + "월", value = value)
        }
    }

    fun getDiffFromPrevious(data: List<MonthlyData>): Int {
        return if (data.size < 2) 0
        else data.last().value - data[data.size - 2].value
    }
}