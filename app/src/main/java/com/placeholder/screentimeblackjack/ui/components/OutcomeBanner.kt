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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.placeholder.screentimeblackjack.engine.HandOutcome
import com.placeholder.screentimeblackjack.ui.theme.*

/**
 * 2-color minimal outcome banner in high-contrast black & white.
 */
@Composable
fun OutcomeBanner(
    outcome: HandOutcome,
    payout: Int,
    bet: Int,
    modifier: Modifier = Modifier
) {
    val (title, deltaText) = when (outcome) {
        HandOutcome.PLAYER_BLACKJACK -> Pair(
            "NATURAL BLACKJACK",
            "+$payout min earned"
        )
        HandOutcome.PLAYER_WIN -> Pair(
            "HAND WON",
            "+$payout min earned"
        )
        HandOutcome.PUSH -> Pair(
            "PUSH",
            "Wager returned ($payout min)"
        )
        HandOutcome.DEALER_WIN -> Pair(
            "HAND LOST",
            "-$bet min deducted"
        )
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + scaleIn(initialScale = 0.95f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(PureBlack)
                .border(width = 1.dp, color = PureWhite, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    color = PureWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = deltaText,
                    color = WhiteMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
