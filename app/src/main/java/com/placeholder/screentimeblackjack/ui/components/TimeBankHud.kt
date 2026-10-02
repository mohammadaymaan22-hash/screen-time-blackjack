package com.placeholder.screentimeblackjack.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.placeholder.screentimeblackjack.ui.theme.*

/**
 * Top HUD displaying the player's current Screen Time currency with glowing status indicators
 * and smooth rolling number animations.
 */
@Composable
fun TimeBankHud(
    balanceMinutes: Int,
    modifier: Modifier = Modifier
) {
    val animatedMinutes by animateIntAsState(
        targetValue = balanceMinutes,
        animationSpec = tween(durationMillis = 600),
        label = "balanceAnim"
    )

    val (glowColor, statusLabel) = when {
        balanceMinutes <= 0 -> Pair(TimeBankDanger, "EMPTY")
        balanceMinutes < 15 -> Pair(TimeBankDanger, "CRITICAL")
        balanceMinutes < 45 -> Pair(TimeBankAmber, "LOW")
        else -> Pair(TimeBankCyan, "ACTIVE")
    }

    val animatedGlow by animateColorAsState(targetValue = glowColor, label = "glowColor")

    val formattedTime = formatMinutes(animatedMinutes)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        SurfaceCardElevated.copy(alpha = 0.95f),
                        SurfaceCard.copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        BorderGold.copy(alpha = 0.4f),
                        animatedGlow.copy(alpha = 0.8f),
                        BorderGold.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon with glowing background circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(animatedGlow.copy(alpha = 0.15f))
                    .border(width = 1.dp, color = animatedGlow.copy(alpha = 0.4f), shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (balanceMinutes > 0) Icons.Default.Schedule else Icons.Default.HourglassEmpty,
                    contentDescription = "Screen Time",
                    tint = animatedGlow,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Time & Label column
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "SCREEN TIME BANK",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    // Status Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(animatedGlow.copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            color = animatedGlow,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.Baseline,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formattedTime,
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "(${animatedMinutes}m)",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    if (minutes <= 0) return "0m"
    val hours = minutes / 60
    val remMin = minutes % 60
    return when {
        hours > 0 && remMin > 0 -> "${hours}h ${remMin}m"
        hours > 0 -> "${hours}h"
        else -> "${remMin}m"
    }
}
