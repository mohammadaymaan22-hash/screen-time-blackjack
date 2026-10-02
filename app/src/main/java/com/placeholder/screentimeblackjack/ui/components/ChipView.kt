package com.placeholder.screentimeblackjack.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.placeholder.screentimeblackjack.ui.theme.*

/**
 * Authentic casino chip with edge notches, inner gold rim, and denomination label.
 */
@Composable
fun CasinoChip(
    label: String,
    baseColor: Color,
    stripeColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "chipPress"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .shadow(elevation = 6.dp, shape = CircleShape)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val outerRadius = this.size.width / 2f

            // Base chip disk
            drawCircle(
                color = if (enabled) baseColor else baseColor.copy(alpha = 0.4f),
                radius = outerRadius,
                center = center
            )

            // Outer edge stripes (6 casino edge spots)
            val spotCount = 6
            val spotAngle = 360f / spotCount
            for (i in 0 until spotCount) {
                val angleRad = Math.toRadians((i * spotAngle).toDouble())
                val spotLength = 6.dp.toPx()
                val spotWidth = 5.dp.toPx()
                val start = Offset(
                    (center.x + (outerRadius - spotLength) * Math.cos(angleRad)).toFloat(),
                    (center.y + (outerRadius - spotLength) * Math.sin(angleRad)).toFloat()
                )
                val end = Offset(
                    (center.x + outerRadius * Math.cos(angleRad)).toFloat(),
                    (center.y + outerRadius * Math.sin(angleRad)).toFloat()
                )
                drawLine(
                    color = if (enabled) stripeColor else stripeColor.copy(alpha = 0.4f),
                    start = start,
                    end = end,
                    strokeWidth = spotWidth
                )
            }

            // Inner ring groove
            drawCircle(
                color = CasinoGold.copy(alpha = if (enabled) 0.8f else 0.3f),
                radius = outerRadius * 0.72f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Center inset
            drawCircle(
                color = SurfaceCard.copy(alpha = if (enabled) 0.85f else 0.4f),
                radius = outerRadius * 0.65f,
                center = center
            )
        }

        // Chip denomination text
        Text(
            text = label,
            color = if (enabled) textColor else textColor.copy(alpha = 0.4f),
            fontSize = if (label.length > 3) 10.sp else 12.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = (-0.5).sp
        )
    }
}
