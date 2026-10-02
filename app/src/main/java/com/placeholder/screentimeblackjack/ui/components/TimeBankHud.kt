package com.placeholder.screentimeblackjack.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.placeholder.screentimeblackjack.ui.theme.*

/**
 * 2-color minimal HUD displaying the player's screen time currency with clean typography.
 */
@Composable
fun TimeBankHud(
    balanceMinutes: Int,
    modifier: Modifier = Modifier
) {
    val animatedMinutes by animateIntAsState(
        targetValue = balanceMinutes,
        animationSpec = tween(durationMillis = 500),
        label = "balanceAnim"
    )

    val formattedTime = formatMinutes(animatedMinutes)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(PureBlack)
            .border(width = 1.dp, color = WhiteBorder, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Title and subtitle
            Column {
                Text(
                    text = "TIME BANK",
                    color = WhiteMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = if (balanceMinutes <= 0) "DEVICE ACCESS LOCKED" else "REMAINING SCREEN TIME",
                    color = WhiteMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Right: High-contrast balance numerals
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = formattedTime,
                    color = PureWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "MIN",
                    color = WhiteMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
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
