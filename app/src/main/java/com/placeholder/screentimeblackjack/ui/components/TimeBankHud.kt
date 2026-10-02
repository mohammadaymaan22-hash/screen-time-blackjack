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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.placeholder.screentimeblackjack.ui.theme.*

/**
 * High-end timepiece HUD displaying the player's wagerable screen time currency.
 */
@Composable
fun TimeBankHud(
    balanceMinutes: Int,
    modifier: Modifier = Modifier
) {
    val animatedMinutes by animateIntAsState(
        targetValue = balanceMinutes,
        animationSpec = tween(durationMillis = 400),
        label = "balanceAnim"
    )

    val formattedTime = formatMinutes(animatedMinutes)
    val isDepleted = balanceMinutes <= 0
    val isLow = balanceMinutes in 1..10

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
                        text = if (isDepleted) "DIGITAL CURFEW ACTIVE" else "AVAILABLE DEVICE TIME",
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
                    text = formattedTime,
                    color = accentColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "MIN",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
    }
}

private fun formatMinutes(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        "${hours}h ${minutes}m"
    } else {
        "$minutes"
    }
}
