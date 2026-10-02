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
            color = CasinoSurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, CasinoGoldDark),
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
                            letterSpacing = 1.sp
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
                    color = CasinoGoldDark.copy(alpha = 0.4f),
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
                            text = "No hands recorded yet.\nPlay a hand to view history and analytics.",
                            color = Color.White.copy(alpha = 0.5f),
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
    val outcomeColor = when (hand.outcome) {
        HandOutcome.PLAYER_BLACKJACK, HandOutcome.PLAYER_WIN -> TimeBankMint
        HandOutcome.PUSH -> CasinoGold
        HandOutcome.DEALER_WIN -> FeltRed
    }

    val outcomeLabel = when (hand.outcome) {
        HandOutcome.PLAYER_BLACKJACK -> "BLACKJACK"
        HandOutcome.PLAYER_WIN -> "WIN"
        HandOutcome.PUSH -> "PUSH"
        HandOutcome.DEALER_WIN -> "LOSS"
    }

    val netMinutes = hand.payout - hand.bet
    val netDisplay = if (netMinutes > 0) "+${netMinutes}m" else if (netMinutes < 0) "${netMinutes}m" else "0m"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CasinoGreenCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGoldDark.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = outcomeColor.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, outcomeColor)
                    ) {
                        Text(
                            text = outcomeLabel,
                            color = outcomeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "Net: $netDisplay",
                        color = outcomeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = dateFormat.format(Date(hand.timestamp)),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Player: ${hand.playerCards} (${hand.playerValue})",
                    color = Color.White,
                    fontSize = 13.sp
                )
                Text(
                    text = "Dealer: ${hand.dealerCards} (${hand.dealerValue})",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Bet: ${hand.bet} min  |  Payout: ${hand.payout} min",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
                Text(
                    text = "Balance: ${hand.balanceAfter}m",
                    color = CasinoGoldLight,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
