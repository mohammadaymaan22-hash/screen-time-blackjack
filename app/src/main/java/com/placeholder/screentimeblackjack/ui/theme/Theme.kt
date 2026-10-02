package com.placeholder.screentimeblackjack.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CasinoColorScheme = darkColorScheme(
    primary = CasinoGold,
    onPrimary = TextDark,
    primaryContainer = CasinoGoldDark,
    onPrimaryContainer = CasinoGoldLight,
    secondary = TimeBankCyan,
    onSecondary = TextDark,
    secondaryContainer = CasinoGreenLight,
    onSecondaryContainer = TimeBankCyan,
    tertiary = TimeBankMint,
    onTertiary = TextDark,
    background = CasinoGreenDeep,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCardElevated,
    onSurfaceVariant = TextSecondary,
    error = SuitRed,
    onError = TextPrimary,
    outline = BorderGold
)

@Composable
fun ScreenTimeBlackjackTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = CasinoGreenDeep.toArgb()
                window.navigationBarColor = CasinoGreenDeep.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = CasinoColorScheme,
        typography = Typography,
        content = content
    )
}
