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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.placeholder.screentimeblackjack.ui.theme.*
import com.placeholder.screentimeblackjack.util.SoundManager
import kotlin.math.cos
import kotlin.math.sin

/**
 * Authentic multi-toned casino poker chip with edge inserts, grooved inner ring,
 * spring bounce animation, and audio click feedback.
 */
@Composable
fun CasinoChip(
    label: String,
    baseColor: Color,
    stripeColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    chipSize: Dp = 56.dp,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = tween(durationMillis = 80),
        label = "chipPress"
    )

    Box(
        modifier = modifier
            .size(chipSize)
            .scale(scale)
            .shadow(elevation = if (isPressed) 2.dp else 5.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(baseColor)
            .border(width = 1.dp, color = stripeColor.copy(alpha = 0.5f), shape = CircleShape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    SoundManager.playChipClick()
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Draw 6 edge inserts (notches) like real poker chips
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f
            val insertCount = 6
            val insertWidth = 5.dp.toPx()
            val insertHeight = 6.dp.toPx()

            for (i in 0 until insertCount) {
                val angle = Math.toRadians((i * 360.0 / insertCount)).toFloat()
                val x = center.x + (radius - insertHeight / 2f) * cos(angle)
                val y = center.y + (radius - insertHeight / 2f) * sin(angle)

                drawRect(
                    color = stripeColor,
                    topLeft = Offset(x - insertWidth / 2f, y - insertHeight / 2f),
                    size = Size(insertWidth, insertHeight)
                )
            }

            // Inner dashed stitch ring
            drawCircle(
                color = stripeColor.copy(alpha = 0.6f),
                radius = radius * 0.68f,
                center = center,
                style = Stroke(
                    width = 1.2.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )
            )
        }

        // Inner center disc
        Box(
            modifier = Modifier
                .size(chipSize * 0.65f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            baseColor.copy(alpha = 0.85f),
                            Color.Black.copy(alpha = 0.4f)
                        )
                    )
                )
                .border(width = 1.dp, color = stripeColor.copy(alpha = 0.4f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = textColor,
                fontSize = if (label.length > 2) 11.sp else 13.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }
    }
}
