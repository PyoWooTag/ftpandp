package com.example.screenreadertest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Composable
fun MonthlyGraphCard(
    title: String,
    unit: String,
    graphData: List<MonthlyData>,
    color: Color,
    onBack: () -> Unit
) {
    val current = graphData.lastOrNull()?.value ?: 0
    val diff = if (graphData.size >= 2) current - graphData[graphData.size - 2].value else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("그돈씨", style = MaterialTheme.typography.headlineLarge)
        Text("그 돈, Control", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$current", style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.width(4.dp))
                    Text(unit)
                }
                val diffText = if (diff >= 0) "전월보다 ${diff}$unit ↑" else "전월보다 ${-diff}$unit ↓"
                Text(diffText, color = if (diff >= 0) Color.Red else Color.Gray)

                Spacer(Modifier.height(16.dp))
                val maxValue = graphData.maxOfOrNull { it.value } ?: 1

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Row(
                        modifier = Modifier.wrapContentWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        graphData.forEach {
                            val heightRatio = it.value.toFloat() / maxValue
                            val barHeight = (heightRatio * 100).dp
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(barHeight)
                                        .background(color, RoundedCornerShape(4.dp))
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(it.month, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(onClick = onBack) {
            Text("뒤로 가기")
        }
    }
}