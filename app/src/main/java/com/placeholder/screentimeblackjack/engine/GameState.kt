package com.placeholder.screentimeblackjack.engine

/**
 * Possible outcomes when a hand is resolved.
 */
enum class HandOutcome {
    /** Player has a natural blackjack (2-card 21). Pays 1.5x bonus. */
    PLAYER_BLACKJACK,
    /** Player wins without blackjack. Pays 1x bet. */
    PLAYER_WIN,
    /** Both hands tie. Bet returned. */
    PUSH,
    /** Dealer wins. Bet forfeited. */
    DEALER_WIN
}

/**
 * Sealed class representing the state machine of a blackjack hand.
 */
sealed class GameState {
    /** Waiting for the player to place a bet. */
    data class Betting(val timeBalance: Int) : GameState()

    /** Player's turn — hit or stand. */
    data class PlayerTurn(
        val playerHand: Hand,
        val dealerHand: Hand,
        val bet: Int,
        val timeBalance: Int
    ) : GameState()

    /** Dealer's turn — engine auto-plays. */
    data class DealerTurn(
        val playerHand: Hand,
        val dealerHand: Hand,
        val bet: Int,
        val timeBalance: Int
    ) : GameState()

    /** Hand is finished; outcome and payout are known. */
    data class HandResolved(
        val playerHand: Hand,
        val dealerHand: Hand,
        val outcome: HandOutcome,
        val payout: Int,
        val bet: Int,
        val timeBalance: Int
    ) : GameState()
}
