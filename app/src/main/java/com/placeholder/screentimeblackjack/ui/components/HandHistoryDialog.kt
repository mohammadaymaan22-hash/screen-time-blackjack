package com.placeholder.screentimeblackjack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.placeholder.screentimeblackjack.data.HandHistory
import com.placeholder.screentimeblackjack.engine.HandOutcome
import com.placeholder.screentimeblackjack.ui.GameViewModel
import com.placeholder.screentimeblackjack.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HandHistoryDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val hands by viewModel.recentHands.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = SurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, BorderGold),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = CasinoGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "HAND HISTORY",
                            color = CasinoGoldLight,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            letterSpacing = 1.2.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                HorizontalDivider(
                    color = BorderGold.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                if (hands.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hands recorded yet.\nWager screen time to view analytics and history.",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(hands, key = { it.id }) { hand ->
                            HandHistoryItem(hand = hand, dateFormat = dateFormat)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HandHistoryItem(
    hand: HandHistory,
    dateFormat: SimpleDateFormat
) {
    val (outcomeColor, outcomeLabel) = when (hand.outcome) {
        HandOutcome.PLAYER_BLACKJACK -> Pair(CasinoGold, "BLACKJACK")
        HandOutcome.PLAYER_WIN -> Pair(TimeBankMint, "WIN")
        HandOutcome.PUSH -> Pair(Color(0xFF90A4AE), "PUSH")
        HandOutcome.DEALER_WIN -> Pair(AlertBorderRed, "LOSS")
    }

    val netMinutes = hand.payout - hand.bet
    val netDisplay = if (netMinutes > 0) "+${netMinutes}m" else if (netMinutes < 0) "${netMinutes}m" else "0m"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGold.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Outcome Badge, Net Change, and Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = outcomeColor.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, outcomeColor)
                    ) {
                        Text(
                            text = outcomeLabel,
                            color = outcomeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "Net: $netDisplay",
                        color = outcomeColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = dateFormat.format(Date(hand.timestamp)),
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }

            // Cards Visual Layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Player Hand Visual
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "YOU (${hand.playerValue})",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    HandCardsRow(cardsText = hand.playerCards)
                }

                // Dealer Hand Visual
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "DEALER (${hand.dealerValue})",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    HandCardsRow(cardsText = hand.dealerCards)
                }
            }

            // Footer: Bet, Payout, Resulting Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bet: ${hand.bet}m  •  Payout: ${hand.payout}m",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "Balance: ${hand.balanceAfter}m",
                    color = CasinoGoldLight,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        }
    }
}

/**
 * Parses space-separated cards (e.g. "5♣ 4♦") and renders individual mini playing cards.
 */
@Composable
private fun HandCardsRow(cardsText: String) {
    val cardTokens = remember(cardsText) {
        cardsText.split("\\s+".toRegex()).filter { it.isNotBlank() }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        cardTokens.forEach { token ->
            val suitChar = token.lastOrNull()?.toString() ?: ""
            val rank = if (token.length > 1) token.dropLast(1) else token
            val isRed = suitChar in listOf("♥", "♦", "H", "D")
            MiniCard(rank = rank, suit = suitChar, isRed = isRed)
        }
    }
}

@Composable
private fun MiniCard(rank: String, suit: String, isRed: Boolean) {
    val suitColor = if (isRed) SuitRed else SuitBlack

    Box(
        modifier = Modifier
            .size(width = 28.dp, height = 38.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .background(CardFaceBg)
            .border(width = 0.8.dp, color = CardBorderColor, shape = RoundedCornerShape(4.dp))
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = rank,
                color = suitColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                lineHeight = 11.sp
            )
            Text(
                text = suit,
                color = suitColor,
                fontSize = 10.sp,
                lineHeight = 10.sp
            )
        }
    }
}
