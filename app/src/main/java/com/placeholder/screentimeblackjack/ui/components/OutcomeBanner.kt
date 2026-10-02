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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.placeholder.screentimeblackjack.engine.HandOutcome
import com.placeholder.screentimeblackjack.ui.theme.*

/**
 * High-impact animated casino outcome banner with rich gradients,
 * celebratory styling for wins, and crisp feedback.
 */
@Composable
fun OutcomeBanner(
    outcome: HandOutcome,
    payout: Int,
    bet: Int,
    modifier: Modifier = Modifier
) {
    val (title, deltaText, gradient, textColor, borderColor) = when (outcome) {
        HandOutcome.PLAYER_BLACKJACK -> Quint(
            "★ NATURAL BLACKJACK ★",
            "+$payout min screen time earned!",
            Brush.horizontalGradient(listOf(Color(0xFFD4AF37), Color(0xFFFFE082), Color(0xFFD4AF37))),
            TextDark,
            CasinoGoldLight
        )
        HandOutcome.PLAYER_WIN -> Quint(
            "HAND WON",
            "+$payout min screen time earned",
            Brush.horizontalGradient(listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))),
            Color.White,
            TimeBankMint
        )
        HandOutcome.PUSH -> Quint(
            "PUSH (TIE)",
            "Wager returned ($payout min)",
            Brush.horizontalGradient(listOf(Color(0xFF263238), Color(0xFF37474F))),
            Color.White,
            BorderSubtle
        )
        HandOutcome.DEALER_WIN -> Quint(
            "HAND LOST",
            "-$bet min deducted",
            Brush.horizontalGradient(listOf(Color(0xFF3B0B11), Color(0xFF5C101A))),
            Color.White,
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
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(gradient)
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
                    color = textColor.copy(alpha = 0.85f),
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
