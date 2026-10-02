package com.placeholder.screentimeblackjack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.placeholder.screentimeblackjack.ui.theme.*

/**
 * Luxury Vegas Casino felt table background with smooth radial felt gradient,
 * clean and free of background dotted circles or lines.
 */
@Composable
fun TableFeltBackground(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        CasinoGreenFelt,
                        CasinoGreenDark,
                        CasinoGreenDeep
                    ),
                    center = Offset(Float.POSITIVE_INFINITY / 2f, 0f),
                    radius = 1800f
                )
            )
    )
}
