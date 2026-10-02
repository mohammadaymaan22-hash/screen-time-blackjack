package com.placeholder.screentimeblackjack.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
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
import com.placeholder.screentimeblackjack.engine.Hand
import com.placeholder.screentimeblackjack.ui.theme.*

/**
 * Renders a player's or dealer's hand with overlapping cards and an informative score badge.
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
        // Hand Header: Title & Score Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = title.uppercase(),
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
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

        // Cards Row with Staggered Overlap
        Box(
            modifier = Modifier
                .height(106.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (cards.isEmpty()) {
                // Placeholder card slot
                Box(
                    modifier = Modifier
                        .size(width = 68.dp, height = 98.dp)
                        .border(
                            width = 1.dp,
                            color = BorderGold.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp)
                        )
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-24).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    cards.forEachIndexed { index, card ->
                        val isFaceUp = !(hideFirstCard && index == 0)
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInHorizontally { it / 2 }
                        ) {
                            PlayingCardView(
                                card = card,
                                isFaceUp = isFaceUp,
                                elevation = (4 + index * 2).dp
                            )
                        }
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
    val (badgeText, badgeBg, badgeTextColor) = when {
        hideFirstCard -> Triple("?", SurfaceCardElevated, TextSecondary)
        isBlackjack -> Triple("★ 21 BLACKJACK", CasinoGold, TextDark)
        isBust -> Triple("BUST (${handValue ?: ""})", SuitRed, TextPrimary)
        isSoft && handValue != null -> Triple("Soft $handValue", SurfaceCardElevated, CasinoGoldLight)
        handValue != null -> Triple("$handValue", SurfaceCardElevated, TextPrimary)
        else -> Triple("-", SurfaceCardElevated, TextSecondary)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(badgeBg)
            .border(width = 1.dp, color = BorderGold, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 2.dp)
    ) {
        Text(
            text = badgeText,
            color = badgeTextColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
