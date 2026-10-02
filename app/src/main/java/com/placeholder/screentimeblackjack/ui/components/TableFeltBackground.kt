package com.placeholder.screentimeblackjack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.placeholder.screentimeblackjack.ui.theme.*

/**
 * Minimalist Noir casino background: deep void black with subtle slate gradation.
 */
@Composable
fun TableFeltBackground(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0F0F),
                        Color(0xFF050505),
                        Color(0xFF000000)
                    )
                )
            )
    )
}
