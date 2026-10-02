package com.placeholder.screentimeblackjack.ui.components

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.placeholder.screentimeblackjack.engine.Card
import com.placeholder.screentimeblackjack.ui.theme.*

/**
 * 2-color minimal HandView with clean score badge and overlapping card fan.
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
                color = WhiteMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
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
            Box(
                modifier = Modifier
                    .size(width = 68.dp, height = 98.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(width = 1.dp, color = WhiteBorder, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "—",
                    color = WhiteMuted,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy((-24).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                cards.forEachIndexed { index, card ->
                    val shouldHide = hideFirstCard && index == 0
                    PlayingCardView(
                        card = if (shouldHide) null else card,
                        isFaceUp = !shouldHide
                    )
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
    val text = when {
        hideFirstCard -> "?"
        isBlackjack -> "21 (BJ)"
        isBust -> "BUST"
        isSoft && handValue != null -> "SOFT $handValue"
        handValue != null -> "$handValue"
        else -> ""
    }

    if (text.isNotEmpty()) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (isBlackjack) PureWhite else PureBlack)
                .border(width = 1.dp, color = PureWhite, shape = RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isBlackjack) PureBlack else PureWhite,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
