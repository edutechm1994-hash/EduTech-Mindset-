package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val EduTechDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = MidnightNavy,
    primaryContainer = DeepTeal,
    onPrimaryContainer = CyanSoft,
    secondary = ScholarGold,
    onSecondary = MidnightNavy,
    secondaryContainer = Color(0xFF4A3200),
    onSecondaryContainer = WarmAmber,
    tertiary = EmeraldPulse,
    onTertiary = MidnightNavy,
    tertiaryContainer = Color(0xFF0A3A2B),
    onTertiaryContainer = Color(0xFF86EFAC),
    background = MidnightNavy,
    onBackground = Color(0xFFF1F5F9),
    surface = DeepSlate,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = ElevatedNavy,
    onSurfaceVariant = Color(0xFF9FB3C8),
    surfaceContainerLowest = MidnightNavy,
    surfaceContainerLow = DeepSlate,
    surfaceContainer = ElevatedNavy,
    surfaceContainerHigh = CardGlassNavy,
    outline = Color(0xFF2B456B),
    outlineVariant = Color(0xFF1D3250),
    error = CoralAlert,
    onError = Color.White,
)

private val EduTechLightColorScheme = lightColorScheme(
    primary = CobaltPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5F0FF),
    onPrimaryContainer = Color(0xFF00273B),
    secondary = RichAmber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE7B3),
    onSecondaryContainer = Color(0xFF3E2400),
    tertiary = EmeraldPulse,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF064E3B),
    background = AlabasterBg,
    onBackground = InkSlate,
    surface = CrispSurface,
    onSurface = InkSlate,
    surfaceVariant = SoftIceSurface,
    onSurfaceVariant = MutedSlate,
    surfaceContainerLowest = CrispSurface,
    surfaceContainerLow = AlabasterBg,
    surfaceContainer = SoftIceSurface,
    surfaceContainerHigh = Color(0xFFDEE9F7),
    outline = Color(0xFFBBD0EA),
    outlineVariant = Color(0xFFD6E2F2),
    error = CoralAlert,
    onError = Color.White,
)

val EduTechShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun EduTechMindsetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> EduTechDarkColorScheme
        else -> EduTechLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EduTechTypography,
        shapes = EduTechShapes,
        content = content,
    )
}
