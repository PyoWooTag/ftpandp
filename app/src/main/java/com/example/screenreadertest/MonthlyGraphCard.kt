package com.example.screenreadertest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFD488))
            .padding(16.dp)
    ) {
        // (1) 상단 타이틀
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp), // 화면의 약 20~25%쯤 내려옴
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.title),
                contentDescription = "그돈씨 TITLE",
                modifier = Modifier
                    .height(150.dp)
                    .padding(bottom = 12.dp)
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        // (2) 중간~하단 그래프 카드
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 30.dp) // 아래로 더 내림
                .fillMaxWidth(0.9f),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(0.dp),
            colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF1F8E9) // 연한 초록 배경 예시
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$current", style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.width(4.dp))
                    Text(unit)
                }
                val diffText = if (diff >= 0) "전월보다 ${diff}$unit ↑" else "전월보다 ${-diff}$unit ↓"
                Text(
                    diffText,
                    color = if (diff >= 0) Color.Red else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

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

        // (3) 아래 뒤로 가기 버튼
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp)
                .align(Alignment.BottomCenter)
                .offset(y = (-140).dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF444444),
                contentColor = Color.White
            ) // ← 여기 괄호 제대로 닫기
        ) {
            Text("뒤로 가기",fontSize = 18.sp)
        }
    }
}