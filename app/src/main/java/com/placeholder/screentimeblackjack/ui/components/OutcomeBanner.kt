package com.placeholder.screentimeblackjack.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.placeholder.screentimeblackjack.engine.HandOutcome
import com.placeholder.screentimeblackjack.ui.theme.*

/**
 * High-impact outcome banner styled with Noir Black, Crisp White, and Crimson Red.
 */
@Composable
fun OutcomeBanner(
    outcome: HandOutcome,
    payout: Int,
    bet: Int,
    modifier: Modifier = Modifier
) {
    val (title, deltaText, bgColor, textColor, borderColor) = when (outcome) {
        HandOutcome.PLAYER_BLACKJACK -> Quint(
            "★ NATURAL BLACKJACK ★",
            "+$payout min screen time earned!",
            PureWhite,
            CasinoBlack,
            CasinoRed
        )
        HandOutcome.PLAYER_WIN -> Quint(
            "HAND WON",
            "+$payout min screen time earned",
            CasinoSurfaceElevated,
            PureWhite,
            PureWhite
        )
        HandOutcome.PUSH -> Quint(
            "PUSH (TIE)",
            "Wager returned ($payout min)",
            CasinoSurface,
            WhiteMuted,
            BorderDark
        )
        HandOutcome.DEALER_WIN -> Quint(
            "HAND LOST",
            "-$bet min deducted",
            CasinoRedBg,
            AlertBorderRed,
            AlertBorderRed
        )
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + scaleIn(initialScale = 0.9f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .border(width = 1.5.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    color = textColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = deltaText,
                    color = if (outcome == HandOutcome.PLAYER_BLACKJACK) CasinoBlack else WhiteMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private data class Quint<A, B, C, D, E>(
    val first: A, val second: B, val third: C, val fourth: D, val fifth: E
)
