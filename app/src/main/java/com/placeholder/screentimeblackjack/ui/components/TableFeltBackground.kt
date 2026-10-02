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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.placeholder.screentimeblackjack.ui.theme.*

/**
 * Luxury Vegas Casino felt table background with realistic radial felt gradient,
 * stitched gold silk rules, dealer shoe marker, and player betting ring.
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
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Outer dark table rail accent line
            drawLine(
                color = CasinoGoldMuted.copy(alpha = 0.2f),
                start = Offset(x = 16.dp.toPx(), y = height * 0.14f),
                end = Offset(x = width - 16.dp.toPx(), y = height * 0.14f),
                strokeWidth = 1.dp.toPx()
            )

            // Center semi-oval arc bounding the blackjack action
            val arcTop = height * 0.20f
            val arcBottom = height * 0.70f
            val arcWidth = width * 1.15f
            val arcLeft = (width - arcWidth) / 2f

            val feltArc = Path().apply {
                addOval(Rect(arcLeft, arcTop, arcLeft + arcWidth, arcBottom))
            }

            // Outer dashed gold stitching
            drawPath(
                path = feltArc,
                color = CasinoGoldMuted.copy(alpha = 0.35f),
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                )
            )

            // Inner solid subtle hairline ring
            val innerArc = Path().apply {
                addOval(Rect(arcLeft + 12.dp.toPx(), arcTop + 12.dp.toPx(), arcLeft + arcWidth - 12.dp.toPx(), arcBottom - 12.dp.toPx()))
            }
            drawPath(
                path = innerArc,
                color = CasinoGoldMuted.copy(alpha = 0.15f),
                style = Stroke(width = 1.dp.toPx())
            )

            // Player betting spot circle
            val playerBetCenterX = width / 2f
            val playerBetCenterY = height * 0.60f
            drawCircle(
                color = CasinoGoldMuted.copy(alpha = 0.25f),
                radius = 34.dp.toPx(),
                center = Offset(playerBetCenterX, playerBetCenterY),
                style = Stroke(
                    width = 1.2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            )

            // Dealer shoe icon outline in top-right corner
            val shoeX = width - 42.dp.toPx()
            val shoeY = height * 0.18f
            drawRoundRect(
                color = CasinoGoldMuted.copy(alpha = 0.3f),
                topLeft = Offset(shoeX - 24.dp.toPx(), shoeY),
                size = androidx.compose.ui.geometry.Size(30.dp.toPx(), 44.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}
