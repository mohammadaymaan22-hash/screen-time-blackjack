package com.placeholder.screentimeblackjack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.placeholder.screentimeblackjack.ui.theme.*
import com.placeholder.screentimeblackjack.util.SoundManager

/**
 * Dialog explaining the house rules of Screen Time Blackjack with luxury styling.
 */
@Composable
fun RulesDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(width = 1.5.dp, color = BorderGold, shape = RoundedCornerShape(20.dp)),
            color = SurfaceCardElevated
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = "HOUSE RULES",
                    color = CasinoGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 1.2.sp
                )

                HorizontalDivider(color = BorderGold.copy(alpha = 0.4f))

                // Rule Items
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    RuleItem(
                        suit = "♠",
                        title = "Screen Time Currency",
                        description = "Every minute in your bank represents actual device screen time. Win hands to earn more phone time; lose and your time runs out."
                    )
                    RuleItem(
                        suit = "♥",
                        title = "Blackjack Pays 3 to 2",
                        description = "A natural 2-card 21 awards 1.5x your wager in bonus minutes (e.g. 10m bet pays +15m)."
                    )
                    RuleItem(
                        suit = "♦",
                        title = "Dealer Stands on Soft 17",
                        description = "The dealer must draw until reaching at least 17, and always stands on soft 17 (Ace + 6)."
                    )
                    RuleItem(
                        suit = "♣",
                        title = "European No-Hole-Card",
                        description = "The dealer reveals and resolves their full hand after you complete your turn."
                    )
                    RuleItem(
                        suit = "♠",
                        title = "Ties are a Push",
                        description = "If both you and the dealer tie, your original wager is returned with zero penalty."
                    )
                }

                Spacer(Modifier.height(4.dp))

                Button(
                    onClick = {
                        SoundManager.playTap()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CasinoGold,
                        contentColor = TextDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        "Understood",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleItem(suit: String, title: String, description: String) {
    val suitColor = if (suit in listOf("♥", "♦")) SuitRed else CasinoGoldLight

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = suit,
            color = suitColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 1.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                color = CasinoGoldLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
