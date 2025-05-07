package com.example.screenreadertest

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import com.example.screenreadertest.ui.theme.ScreenreadertestTheme
import androidx.lifecycle.viewmodel.compose.viewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScreenreadertestTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        context = this,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(context: Context,
               viewModel: MainViewModel = viewModel(),
               modifier: Modifier = Modifier) {
    val stats = remember { viewModel.getThisMonthStats(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(24.dp))
        Text("그 돈 씨", style = MaterialTheme.typography.headlineLarge)
        Text("그 돈, Control", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoCard("배달한 횟수", stats["orderCount"] ?: "-", "회") {
                context.startActivity(Intent(context, OrderCountDetailActivity::class.java))
            }
            InfoCard("배달한 금액", stats["orderAmount"] ?: "-", "원") {
                context.startActivity(Intent(context, OrderAmountDetailActivity::class.java))
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoCard("멈춘 횟수", stats["stopCount"] ?: "-", "회") {
                context.startActivity(Intent(context, StopDetailActivity::class.java))
            }
            InfoCard("아낀 금액", stats["savedAmount"] ?: "-", "원") {
                context.startActivity(Intent(context, SavedAmountDetailActivity::class.java))
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { openAccessibilitySettings(context) },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Gray
            )
        ) {
            Text("접근성 설정 열기", fontSize = 18.sp)
        }
    }
}


/**
 * 접근성 설정 화면을 여는 함수
 */
fun openAccessibilitySettings(context: Context) {
    Log.d("AccessibilityService", "click")

    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    context.startActivity(intent)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ScreenreadertestTheme {
        MainScreen(context = MainActivity())
    }
}

@Composable
fun InfoCard(label: String, number: String, unit: String = "", onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(width = 160.dp, height = 160.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Black)

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.headlineSmall, // 숫자 강조
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black
                )
            }
        }
    }
}


