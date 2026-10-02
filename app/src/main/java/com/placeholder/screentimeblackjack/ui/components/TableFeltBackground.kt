package com.placeholder.screentimeblackjack.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.placeholder.screentimeblackjack.ui.theme.*

/**
 * Background Canvas rendering the casino green felt texture, radial table lighting,
 * and elegant table felt arc markings.
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
                    radius = 1200f
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Felt Table Semi-Oval Arc (Dealer to Player betting arc)
            val arcTop = height * 0.18f
            val arcBottom = height * 0.72f
            val arcWidth = width * 1.3f
            val arcLeft = (width - arcWidth) / 2f

            val feltArc = Path().apply {
                addOval(Rect(arcLeft, arcTop, arcLeft + arcWidth, arcBottom))
            }

            // Outer subtle gold trim
            drawPath(
                path = feltArc,
                color = CasinoGold.copy(alpha = 0.12f),
                style = Stroke(width = 2.dp.toPx())
            )

            // Inner felt boundary line
            val innerWidth = width * 1.15f
            val innerLeft = (width - innerWidth) / 2f
            val innerArc = Path().apply {
                addOval(Rect(innerLeft, arcTop + 24.dp.toPx(), innerLeft + innerWidth, arcBottom - 24.dp.toPx()))
            }

            drawPath(
                path = innerArc,
                color = CasinoGold.copy(alpha = 0.08f),
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )
            )

            // Subtle top table spotlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CasinoGreenHighlight.copy(alpha = 0.15f),
                        androidx.compose.ui.graphics.Color.Transparent
                    ),
                    center = Offset(width / 2f, height * 0.45f),
                    radius = width * 0.6f
                ),
                radius = width * 0.6f,
                center = Offset(width / 2f, height * 0.45f)
            )
        }
    }
}
