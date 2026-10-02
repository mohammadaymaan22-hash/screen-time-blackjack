package com.placeholder.screentimeblackjack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.placeholder.screentimeblackjack.engine.*
import com.placeholder.screentimeblackjack.ui.components.*
import com.placeholder.screentimeblackjack.ui.theme.*

@Preview(name = "Playing Cards Preview", showBackground = true, backgroundColor = 0xFF0D251A)
@Composable
fun PlayingCardsPreview() {
    ScreenTimeBlackjackTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PlayingCardView(
                card = Card(Rank.ACE, Suit.SPADES),
                isFaceUp = true
            )
            PlayingCardView(
                card = Card(Rank.KING, Suit.HEARTS),
                isFaceUp = true
            )
            PlayingCardView(
                card = Card(Rank.TEN, Suit.DIAMONDS),
                isFaceUp = true
            )
            PlayingCardView(
                card = Card(Rank.JACK, Suit.CLUBS),
                isFaceUp = false
            )
        }
    }
}

@Preview(name = "Time Bank HUD Preview", showBackground = true, backgroundColor = 0xFF0D251A)
@Composable
fun TimeBankHudPreview() {
    ScreenTimeBlackjackTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TimeBankHud(balanceMinutes = 105)
            TimeBankHud(balanceMinutes = 25)
            TimeBankHud(balanceMinutes = 0)
        }
    }
}

@Preview(name = "Casino Chips Preview", showBackground = true, backgroundColor = 0xFF0D251A)
@Composable
fun CasinoChipsPreview() {
    ScreenTimeBlackjackTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CasinoChip(
                label = "1m",
                baseColor = ChipWhite,
                stripeColor = CasinoGold,
                textColor = TextDark,
                onClick = {}
            )
            CasinoChip(
                label = "5m",
                baseColor = ChipBlue,
                stripeColor = ChipWhite,
                textColor = androidx.compose.ui.graphics.Color.White,
                onClick = {}
            )
            CasinoChip(
                label = "10m",
                baseColor = ChipGreen,
                stripeColor = ChipWhite,
                textColor = androidx.compose.ui.graphics.Color.White,
                onClick = {}
            )
            CasinoChip(
                label = "25m",
                baseColor = ChipRed,
                stripeColor = CasinoGold,
                textColor = androidx.compose.ui.graphics.Color.White,
                onClick = {}
            )
            CasinoChip(
                label = "MAX",
                baseColor = ChipBlack,
                stripeColor = CasinoGold,
                textColor = CasinoGoldLight,
                onClick = {}
            )
        }
    }
}

@Preview(name = "Outcome Banner Preview", showBackground = true, backgroundColor = 0xFF0D251A)
@Composable
fun OutcomeBannerPreview() {
    ScreenTimeBlackjackTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutcomeBanner(
                outcome = HandOutcome.PLAYER_BLACKJACK,
                payout = 37,
                bet = 25
            )
            OutcomeBanner(
                outcome = HandOutcome.PLAYER_WIN,
                payout = 20,
                bet = 10
            )
            OutcomeBanner(
                outcome = HandOutcome.DEALER_WIN,
                payout = 0,
                bet = 15
            )
        }
    }
}
