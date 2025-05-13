// SavedAmountDetailActivity.kt
package com.example.screenreadertest

import com.example.screenreadertest.DetailViewModel
import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.screenreadertest.ui.theme.ScreenreadertestTheme

class SavedAmountDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScreenreadertestTheme {
                SavedAmountScreen(context = this)
            }
        }
    }
}

@Composable
fun SavedAmountScreen(context: Context, viewModel: DetailViewModel = viewModel()) {
    val data = remember { viewModel.getGraphData(context, metric = "savedAmount") }

    MonthlyGraphCard(
        title = "아낀 금액",
        unit = "원",
        graphData = data,
        color = Color.Blue,
        onBack = { (context as Activity).finish() }
    )
}