package com.placeholder.screentimeblackjack.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.placeholder.screentimeblackjack.engine.GameState
import com.placeholder.screentimeblackjack.engine.HandOutcome

/**
 * Bare-bones Compose game screen.
 *
 * Shows balance, bet input, deal/hit/stand buttons, hand values,
 * and outcome text. No styling, no animations — plain text and buttons.
 */
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()
    val timeBalance by viewModel.timeBalance.collectAsState()
    val isReady by viewModel.isReady.collectAsState()

    if (!isReady) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading...")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Balance display
        Text(
            text = "Time Balance: $timeBalance min",
            fontSize = 20.sp
        )

        Spacer(Modifier.height(8.dp))

        when (val state = gameState) {
            is GameState.Betting -> BettingView(
                balance = timeBalance,
                onPlaceBet = { viewModel.placeBet(it) }
            )
            is GameState.PlayerTurn -> PlayerTurnView(
                state = state,
                onHit = { viewModel.hit() },
                onStand = { viewModel.stand() }
            )
            is GameState.DealerTurn -> {
                // DealerTurn is transient — engine auto-resolves.
                // Shouldn't normally be visible, but handle gracefully.
                Text("Dealer is playing...")
            }
            is GameState.HandResolved -> HandResolvedView(
                state = state,
                onNewHand = { viewModel.startNewHand() }
            )
        }
    }
}

@Composable
private fun BettingView(
    balance: Int,
    onPlaceBet: (Int) -> Unit
) {
    var betText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Text("Place your bet (1-$balance minutes):", fontSize = 16.sp)

    OutlinedTextField(
        value = betText,
        onValueChange = {
            betText = it
            errorText = null
        },
        label = { Text("Bet amount") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = errorText != null,
        supportingText = errorText?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth()
    )

    Button(
        onClick = {
            val amount = betText.toIntOrNull()
            when {
                amount == null -> errorText = "Enter a number"
                amount < 1 -> errorText = "Minimum bet is 1 minute"
                amount > balance -> errorText = "Cannot bet more than $balance"
                else -> {
                    onPlaceBet(amount)
                    betText = ""
                    errorText = null
                }
            }
        },
        enabled = balance > 0
    ) {
        Text("Deal")
    }

    if (balance == 0) {
        Text(
            "No time remaining. You're out of minutes!",
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun PlayerTurnView(
    state: GameState.PlayerTurn,
    onHit: () -> Unit,
    onStand: () -> Unit
) {
    // Dealer's hand — first card hidden
    Text("Dealer's hand:", fontSize = 16.sp)
    val dealerCards = state.dealerHand.cards
    val dealerDisplay = if (dealerCards.isNotEmpty()) {
        "[hidden] " + dealerCards.drop(1).joinToString(" ")
    } else {
        "(no cards)"
    }
    Text(dealerDisplay, fontSize = 18.sp)

    Spacer(Modifier.height(8.dp))

    // Player's hand
    Text("Your hand:", fontSize = 16.sp)
    Text(
        "${state.playerHand} (value: ${state.playerHand.value})",
        fontSize = 18.sp
    )

    Spacer(Modifier.height(8.dp))

    Text("Bet: ${state.bet} min", fontSize = 14.sp)

    Spacer(Modifier.height(8.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onHit) { Text("Hit") }
        Button(onClick = onStand) { Text("Stand") }
    }
}

@Composable
private fun HandResolvedView(
    state: GameState.HandResolved,
    onNewHand: () -> Unit
) {
    // Dealer's full hand (revealed)
    Text("Dealer's hand:", fontSize = 16.sp)
    Text(
        "${state.dealerHand} (value: ${state.dealerHand.value})",
        fontSize = 18.sp
    )

    Spacer(Modifier.height(8.dp))

    // Player's hand
    Text("Your hand:", fontSize = 16.sp)
    Text(
        "${state.playerHand} (value: ${state.playerHand.value})",
        fontSize = 18.sp
    )

    Spacer(Modifier.height(12.dp))

    // Outcome
    val outcomeText = when (state.outcome) {
        HandOutcome.PLAYER_BLACKJACK -> "BLACKJACK! You win ${state.payout} min"
        HandOutcome.PLAYER_WIN -> "You win! +${state.payout} min"
        HandOutcome.PUSH -> "Push — bet returned (${state.payout} min)"
        HandOutcome.DEALER_WIN -> "Dealer wins. You lose ${state.bet} min"
    }
    Text(
        outcomeText,
        fontSize = 20.sp,
        color = when (state.outcome) {
            HandOutcome.PLAYER_BLACKJACK, HandOutcome.PLAYER_WIN ->
                MaterialTheme.colorScheme.primary
            HandOutcome.PUSH ->
                MaterialTheme.colorScheme.secondary
            HandOutcome.DEALER_WIN ->
                MaterialTheme.colorScheme.error
        }
    )

    Spacer(Modifier.height(12.dp))

    Button(
        onClick = onNewHand,
        enabled = state.timeBalance > 0
    ) {
        Text(if (state.timeBalance > 0) "New Hand" else "Out of Time")
    }
}
