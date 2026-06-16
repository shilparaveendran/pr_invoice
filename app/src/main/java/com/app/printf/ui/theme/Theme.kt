package com.app.printf.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val WhiteBlueColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = BrandWhite,
    primaryContainer = BrandBlueSoft,
    onPrimaryContainer = BrandBlueDark,
    secondary = BrandBlue,
    onSecondary = BrandWhite,
    secondaryContainer = BrandBlueSoft,
    onSecondaryContainer = BrandBlueDark,
    tertiary = BrandBlueLight,
    onTertiary = BrandWhite,
    background = BrandBackground,
    onBackground = BrandTextDark,
    surface = BrandWhite,
    onSurface = BrandTextDark,
    surfaceVariant = BrandWhite,
    onSurfaceVariant = BrandGray,
    outline = BrandBlue,
    outlineVariant = BrandBlueSoft,
)

@Composable
fun PrintfTheme(
    content: @Composable () -> Unit,
) {
    val colorScheme = WhiteBlueColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BrandBlue.toArgb()
            window.navigationBarColor = BrandWhite.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = PrintfShapes,
        content = content,
    )
}
