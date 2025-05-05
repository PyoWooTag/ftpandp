package com.example.screenreadertest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.screenreadertest.ui.theme.ScreenreadertestTheme

class StopDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScreenreadertestTheme {
                DetailScreen("멈춘 횟수", "12회", Color.Blue) { finish() }
            }
        }
    }
}

@Composable
fun DetailScreen(title: String, value: String, barColor: Color, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text("그 돈 씨", style = MaterialTheme.typography.headlineLarge)
        Text("그 돈, Control", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(32.dp))

        // 📦 카드 (그래프 영역만)
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(value, style = MaterialTheme.typography.headlineLarge)
                Text("전월보다 3회 ↓", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Spacer(Modifier.height(16.dp))
                BarGraphPlaceholder(barColor = barColor)
            }
        }

        Spacer(Modifier.height(32.dp))

        // 🔘 버튼 (카드 바깥)
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text("뒤로 가기", fontSize = 18.sp)
        }
    }
}



