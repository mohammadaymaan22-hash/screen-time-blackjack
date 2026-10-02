package com.placeholder.screentimeblackjack.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.placeholder.screentimeblackjack.ui.theme.*

/**
 * 2-color hyper-minimal table background in pure OLED black with crisp geometric hairline rules.
 */
@Composable
fun TableFeltBackground(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Minimalist hairline divider line across upper third
            drawLine(
                color = WhiteBorder,
                start = Offset(x = 24.dp.toPx(), y = height * 0.16f),
                end = Offset(x = width - 24.dp.toPx(), y = height * 0.16f),
                strokeWidth = 1.dp.toPx()
            )

            // Minimal center semi-oval arc bounding betting layout
            val arcTop = height * 0.22f
            val arcBottom = height * 0.68f
            val arcWidth = width * 1.1f
            val arcLeft = (width - arcWidth) / 2f

            val feltArc = Path().apply {
                addOval(Rect(arcLeft, arcTop, arcLeft + arcWidth, arcBottom))
            }

            drawPath(
                path = feltArc,
                color = WhiteBorder,
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            )
        }
    }
}
