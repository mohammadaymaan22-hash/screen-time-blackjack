package com.placeholder.screentimeblackjack.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.placeholder.screentimeblackjack.ui.theme.*

/**
 * High-end timepiece HUD displaying the player's wagerable screen time currency,
 * supporting real-time second-by-second countdown and active usage status.
 */
@Composable
fun TimeBankHud(
    balanceMinutes: Int,
    balanceSeconds: Int = balanceMinutes * 60,
    isActivelyTracking: Boolean = false,
    modifier: Modifier = Modifier
) {
    val totalSeconds = if (balanceSeconds >= 0) balanceSeconds else balanceMinutes * 60
    val isDepleted = totalSeconds <= 0
    val isLow = totalSeconds in 1..600 // <= 10 min

    val animatedSeconds by animateIntAsState(
        targetValue = totalSeconds,
        animationSpec = tween(durationMillis = 300),
        label = "secAnim"
    )

    val (timeDisplay, unitLabel) = formatTime(animatedSeconds)

    val accentColor = when {
        isDepleted -> TimeBankDanger
        isLow -> TimeBankAmber
        else -> TimeBankCyan
    }

    Box(
        modifier = modifier
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        SurfaceCardElevated.copy(alpha = 0.95f),
                        SurfaceCard.copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = if (isDepleted) AlertBorderRed else BorderGold,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Status indicator & labels
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pulsing dot badge
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )

                Column {
                    Text(
                        text = "TIME BANK VAULT",
                        color = CasinoGoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = when {
                            isDepleted -> "DIGITAL CURFEW ACTIVE"
                            isActivelyTracking -> "COUNTDOWN ACTIVE (IN USE)"
                            else -> "PAUSED (ONLY COUNTS IN APPS)"
                        },
                        color = if (isDepleted) AlertTextRed else TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Right: High-contrast balance numerals
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = timeDisplay,
                    color = accentColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = unitLabel,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
    }
}

private fun formatTime(totalSeconds: Int): Pair<String, String> {
    if (totalSeconds <= 0) return Pair("0", "MIN")
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return when {
        minutes >= 60 -> {
            val h = minutes / 60
            val m = minutes % 60
            Pair("${h}h ${m}m", "TOTAL")
        }
        minutes > 0 && seconds > 0 -> {
            Pair("${minutes}m ${seconds}s", "")
        }
        minutes > 0 -> {
            Pair("$minutes", "MIN")
        }
        else -> {
            Pair("${seconds}s", "")
        }
    }
}
