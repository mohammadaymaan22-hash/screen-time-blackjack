package com.placeholder.screentimeblackjack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.placeholder.screentimeblackjack.ui.theme.*

/**
 * High-contrast Noir timepiece HUD displaying exact second-by-second screen time currency.
 * Built with pure Black, White, and Red palette.
 */
@Composable
fun TimeBankHud(
    balanceMinutes: Int,
    balanceSeconds: Int = balanceMinutes * 60,
    isActivelyTracking: Boolean = false,
    selectedAppName: String? = null,
    modifier: Modifier = Modifier
) {
    val totalSeconds = (if (balanceSeconds >= 0) balanceSeconds else balanceMinutes * 60).coerceAtLeast(0)
    val isDepleted = totalSeconds <= 0
    val isLow = totalSeconds in 1..600 // <= 10 min

    // Direct exact integer time format without interpolating animation
    val (timeDisplay, unitLabel) = formatTime(totalSeconds)

    val accentColor = when {
        isDepleted -> AlertBorderRed
        isLow -> CasinoRedBright
        else -> PureWhite
    }

    Box(
        modifier = modifier
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(CasinoSurface)
            .border(
                width = 1.dp,
                color = if (isDepleted) AlertBorderRed else BorderDark,
                shape = RoundedCornerShape(12.dp)
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
                // Red status dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isActivelyTracking) CasinoRed else if (isDepleted) AlertBorderRed else WhiteMuted)
                )

                Column {
                    Text(
                        text = if (selectedAppName != null) "VAULT: ${selectedAppName.uppercase()}" else "TIME BANK VAULT",
                        color = PureWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = when {
                            isDepleted && selectedAppName != null -> "$selectedAppName IS LOCKED (0 MIN)"
                            isDepleted -> "DIGITAL CURFEW ACTIVE"
                            isActivelyTracking && selectedAppName != null -> "COUNTING DOWN ($selectedAppName)"
                            isActivelyTracking -> "COUNTING DOWN (IN USE)"
                            else -> "PAUSED (ONLY IN FOREGROUND)"
                        },
                        color = if (isDepleted) AlertTextRed else WhiteSubtle,
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
                if (unitLabel.isNotEmpty()) {
                    Text(
                        text = unitLabel,
                        color = WhiteSubtle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
        }
    }
}

private fun formatTime(totalSeconds: Int): Pair<String, String> {
    if (totalSeconds <= 0) return Pair("0m 00s", "")
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return when {
        minutes >= 60 -> {
            val h = minutes / 60
            val m = minutes % 60
            val s = seconds
            Pair(String.format("%dh %02dm %02ds", h, m, s), "")
        }
        else -> {
            Pair(String.format("%dm %02ds", minutes, seconds), "")
        }
    }
}
