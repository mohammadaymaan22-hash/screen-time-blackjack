package com.placeholder.screentimeblackjack.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
 * Animated banner displayed when a hand resolves, celebrating a win or signaling a loss
 * with the exact screen-time minute delta.
 */
@Composable
fun OutcomeBanner(
    outcome: HandOutcome,
    payout: Int,
    bet: Int,
    modifier: Modifier = Modifier
) {
    val (title, deltaText, accentColor, gradientColors) = when (outcome) {
        HandOutcome.PLAYER_BLACKJACK -> Quad(
            "NATURAL BLACKJACK!",
            "+$payout min earned",
            CasinoGold,
            listOf(Color(0xFF2A2006), Color(0xFF191203))
        )
        HandOutcome.PLAYER_WIN -> Quad(
            "YOU WIN!",
            "+$payout min earned",
            TimeBankMint,
            listOf(Color(0xFF0A2B1D), Color(0xFF05170F))
        )
        HandOutcome.PUSH -> Quad(
            "PUSH",
            "Bet returned ($payout min)",
            TimeBankCyan,
            listOf(Color(0xFF08252C), Color(0xFF041216))
        )
        HandOutcome.DEALER_WIN -> Quad(
            "DEALER WINS",
            "-$bet min lost",
            SuitRed,
            listOf(Color(0xFF2E0909), Color(0xFF170404))
        )
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) +
                scaleIn(initialScale = 0.85f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(gradientColors))
                .border(width = 1.5.dp, color = accentColor, shape = RoundedCornerShape(16.dp))
                .padding(vertical = 12.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = accentColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = deltaText,
                        color = accentColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
