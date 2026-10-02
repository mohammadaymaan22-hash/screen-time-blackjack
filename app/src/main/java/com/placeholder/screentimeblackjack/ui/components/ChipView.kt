package com.placeholder.screentimeblackjack.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.placeholder.screentimeblackjack.ui.theme.*

/**
 * 2-color minimal chip: High-contrast circular token in pure black and white.
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
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "chipPress"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(PureBlack)
            .border(width = 1.5.dp, color = PureWhite, shape = CircleShape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner thin geometric circle
        Box(
            modifier = Modifier
                .size(size - 12.dp)
                .border(width = 1.dp, color = WhiteBorder, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = PureWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
