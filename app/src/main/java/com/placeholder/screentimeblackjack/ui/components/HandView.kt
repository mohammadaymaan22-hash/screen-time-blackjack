package com.placeholder.screentimeblackjack.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.placeholder.screentimeblackjack.engine.Card
import com.placeholder.screentimeblackjack.ui.theme.*

/**
 * Animated casino hand view displaying dealt playing cards with natural fan angles,
 * slide-in entrance transitions, and dynamic score badges.
 */
@Composable
fun HandView(
    title: String,
    cards: List<Card>,
    modifier: Modifier = Modifier,
    hideFirstCard: Boolean = false,
    handValue: Int? = null,
    isSoft: Boolean = false,
    isBlackjack: Boolean = false,
    isBust: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hand Header: Label & Score Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Text(
                text = title.uppercase(),
                color = CasinoGoldLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 1.2.sp
            )

            if (cards.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                ScoreBadge(
                    handValue = handValue,
                    hideFirstCard = hideFirstCard,
                    isSoft = isSoft,
                    isBlackjack = isBlackjack,
                    isBust = isBust
                )
            }
        }

        // Cards layout
        if (cards.isEmpty()) {
            // Elegant dashed placeholder on felt
            Box(
                modifier = Modifier
                    .size(width = 68.dp, height = 98.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .border(
                        width = 1.dp,
                        color = CasinoGoldMuted.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(9.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "♠",
                    color = CasinoGoldMuted.copy(alpha = 0.25f),
                    fontSize = 20.sp
                )
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy((-22).dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                cards.forEachIndexed { index, card ->
                    val shouldHide = hideFirstCard && index == 0
                    // Fan tilt angles for tactile table feel
                    val tilt = when (index) {
                        0 -> -3f
                        1 -> if (cards.size > 2) -1f else 2f
                        2 -> 2f
                        3 -> 5f
                        else -> 7f
                    }

                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                            initialOffsetY = { -it / 2 }
                        ) + fadeIn() + scaleIn(initialScale = 0.85f)
                    ) {
                        PlayingCardView(
                            card = if (shouldHide) null else card,
                            isFaceUp = !shouldHide,
                            tiltAngle = tilt
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreBadge(
    handValue: Int?,
    hideFirstCard: Boolean,
    isSoft: Boolean,
    isBlackjack: Boolean,
    isBust: Boolean
) {
    val (text, bgColor, textColor, borderColor) = when {
        hideFirstCard -> Quad("?", SurfaceCardElevated, CasinoGoldLight, BorderGold)
        isBlackjack -> Quad("BLACKJACK 21", CasinoGold, TextDark, CasinoGoldLight)
        isBust -> Quad("BUST $handValue", FeltRed, AlertBorderRed, AlertBorderRed)
        isSoft && handValue != null -> Quad("SOFT $handValue", SurfaceCardElevated, CasinoGoldLight, BorderGold)
        handValue != null -> Quad("$handValue", SurfaceCardElevated, TextPrimary, BorderGold)
        else -> Quad("", Color.Transparent, Color.Transparent, Color.Transparent)
    }

    if (text.isNotEmpty()) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(bgColor)
                .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
