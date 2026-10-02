package com.placeholder.screentimeblackjack.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NoirColorScheme = darkColorScheme(
    primary = CasinoRed,
    onPrimary = PureWhite,
    primaryContainer = CasinoRedDark,
    onPrimaryContainer = PureWhite,
    secondary = PureWhite,
    onSecondary = CasinoBlack,
    secondaryContainer = CasinoSurfaceElevated,
    onSecondaryContainer = PureWhite,
    tertiary = CasinoRedBright,
    onTertiary = PureWhite,
    background = CasinoBlack,
    onBackground = PureWhite,
    surface = CasinoSurface,
    onSurface = PureWhite,
    surfaceVariant = CasinoSurfaceElevated,
    onSurfaceVariant = WhiteMuted,
    error = CasinoRed,
    onError = PureWhite,
    outline = BorderDark
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
                window.statusBarColor = CasinoBlack.toArgb()
                window.navigationBarColor = CasinoBlack.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = NoirColorScheme,
        typography = Typography,
        content = content
    )
}
