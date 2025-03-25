package com.example.screenreadertest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.screenreadertest.ui.theme.ScreenreadertestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScreenreadertestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
fun MainScreen(context: Context, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Hello Android!", modifier = Modifier.padding(bottom = 16.dp))

        Button(onClick = { openAccessibilitySettings(context) }) {
            Text("접근성 설정 열기")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ScreenreadertestTheme {
        MainScreen(context = MainActivity())
    }
}

/**
 * 접근성 설정 화면을 여는 함수
 */
fun openAccessibilitySettings(context: Context) {
    Log.d("AccessibilityService","click")

    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    context.startActivity(intent)
}
