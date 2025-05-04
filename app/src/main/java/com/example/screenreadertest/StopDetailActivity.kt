package com.example.screenreadertest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("그 돈 씨", style = MaterialTheme.typography.headlineLarge)
        Text("그 돈, Control", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))

        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(value, style = MaterialTheme.typography.headlineLarge)

        BarGraphPlaceholder(barColor = barColor)

        Spacer(Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("뒤로 가기")
        }
    }
}


