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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
    var btn by remember { mutableStateOf(false) } // 숫자 상태 저장

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Hello Android!", modifier = Modifier.padding(bottom = 16.dp))

        Button(onClick = { openAccessibilitySettings(context) }) {
            Text("접근성 설정 열기")
        }

        Spacer(modifier = Modifier.height(16.dp)) // 버튼 사이 여백 추가

//        Button(
//            onClick = {
//                btn = !btn
//                Log.d("MainScreen", "테스트 버튼 클릭됨")
//            }
//        ) {
//            Text("테스트 버튼", modifier = Modifier.semantics { contentDescription = "테스트 버튼" })
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        if (btn) {
//            Button(
//                onClick = { Log.d("MainScreen", "숨겨진 버튼 클릭됨") }
//            ) {
//                Text("숨겨진 버튼", modifier = Modifier.semantics { contentDescription = "숨겨진 버튼" })
//            }
//        }
        Button(
            onClick = {
                btn = !btn
                Log.d("MainScreen", "테스트 버튼 클릭됨")
            },
            modifier = Modifier.semantics { contentDescription = "테스트 버튼" }
        ) {
            Text("테스트 버튼")
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (btn) {
            Button(
                onClick = { Log.d("MainScreen", "숨겨진 버튼 클릭됨") },
                modifier = Modifier.semantics { contentDescription = "테스트 버튼" }
            ) {
                Text("숨겨진 버튼")
            }
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
