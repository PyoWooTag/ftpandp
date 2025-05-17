package com.example.screenreadertest.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun ScreenreadertestTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GDSLightColors,
        typography = Typography,
        content = content
    )
}

val GDSLightColors = lightColorScheme(
    primary = GDSButton,                 // 카드 및 버튼 배경
    onPrimary = GDSTextWhite,         // 카드 및 버튼 내부 텍스트 색

    secondary = GDSChart,         // 차트 배경
    onSecondary = GDSTextDark,          // 차트 내용 등

    background = GDSBackground,       // 전체 배경
    onBackground = GDSTextDark,       // 일반 배경 위 텍스트

    surface = GDSCard,                // 카드 등 서페이스
    onSurface = GDSTextWhite          // 서페이스 위 텍스트
)