// OrderAmountDetailActivity.kt
package com.example.screenreadertest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.screenreadertest.ui.theme.ScreenreadertestTheme

class OrderAmountDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScreenreadertestTheme {
                DetailScreen("배달한 금액", "58,000원", Color.Red) { finish() }
            }
        }
    }
}
