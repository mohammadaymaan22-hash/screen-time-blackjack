package com.placeholder.screentimeblackjack.engine

import kotlin.random.Random

/**
 * Pure-Kotlin blackjack game engine. No Android dependencies.
 *
 * Manages the full lifecycle of a hand: betting → dealing → player turn →
 * dealer turn → resolution. Tracks time balance and enforces payout rules.
 *
 * ## Rules (v1)
 * - Hit/stand only (no split, double down, or insurance)
 * - No dealer peek (European no-hole-card rule): player plays out fully,
 *   then dealer reveals and plays
 * - Dealer stands on soft 17
 * - Natural blackjack pays 1.5x bonus (rounded down)
 * - Both naturals = push
 * - Balance floors at 0
 *
 * @param initialBalance Starting time balance in minutes.
 * @param numberOfDecks Number of decks in the shoe (default 1).
 * @param random Random source for shuffling (injectable for testing).
 */
class BlackjackEngine(
    initialBalance: Int = 60,
    numberOfDecks: Int = 1,
    random: Random = Random
) {
    var timeBalance: Int = initialBalance
        private set

    private val deck = Deck(numberOfDecks = numberOfDecks, random = random)

    private var _state: GameState = GameState.Betting(timeBalance)

    /** Current game state. */
    val state: GameState get() = _state

    /**
     * Place a bet and deal the initial hands.
     *
     * @param amount Minutes to wager.
     * @throws IllegalStateException if not in Betting state.
     * @throws IllegalArgumentException if bet is invalid.
     */
    fun placeBet(amount: Int) {
        check(_state is GameState.Betting) { "Can only place bet in Betting state" }
        require(amount >= 1) { "Minimum bet is 1 minute" }
        require(amount <= timeBalance) { "Cannot bet more than current balance ($timeBalance)" }

        // Deduct bet from balance
        timeBalance -= amount

        // Deal 2 cards each: player, dealer, player, dealer (standard deal order)
        var playerHand = Hand()
        var dealerHand = Hand()

        playerHand = playerHand.withCard(deck.deal())
        dealerHand = dealerHand.withCard(deck.deal())
        playerHand = playerHand.withCard(deck.deal())
        dealerHand = dealerHand.withCard(deck.deal())

        // Check for player natural blackjack
        // Note: No dealer peek (European rule). If player has blackjack,
        // we still go to resolution where dealer will reveal.
        if (playerHand.isBlackjack()) {
            resolveHand(playerHand, dealerHand, amount)
        } else {
            _state = GameState.PlayerTurn(
                playerHand = playerHand,
                dealerHand = dealerHand,
                bet = amount,
                timeBalance = timeBalance
            )
        }
    }

    /**
     * Player takes a hit — deal one more card.
     *
     * @throws IllegalStateException if not in PlayerTurn state.
     */
    fun hit() {
        val current = _state
        check(current is GameState.PlayerTurn) { "Can only hit during PlayerTurn" }

        val newHand = current.playerHand.withCard(deck.deal())

        if (newHand.isBust()) {
            // Player busts — auto-loss, no dealer turn needed
            _state = GameState.HandResolved(
                playerHand = newHand,
                dealerHand = current.dealerHand,
                outcome = HandOutcome.DEALER_WIN,
                payout = 0,
                bet = current.bet,
                timeBalance = timeBalance
            )
        } else {
            _state = GameState.PlayerTurn(
                playerHand = newHand,
                dealerHand = current.dealerHand,
                bet = current.bet,
                timeBalance = timeBalance
            )
        }
    }

    /**
     * Player stands — move to dealer's turn and auto-resolve.
     *
     * @throws IllegalStateException if not in PlayerTurn state.
     */
    fun stand() {
        val current = _state
        check(current is GameState.PlayerTurn) { "Can only stand during PlayerTurn" }

        // Dealer plays out
        val finalDealerHand = playDealer(current.dealerHand)

        resolveHand(current.playerHand, finalDealerHand, current.bet)
    }

    /**
     * Start a new hand — return to Betting state.
     *
     * @throws IllegalStateException if not in HandResolved state.
     */
    fun startNewHand() {
        check(_state is GameState.HandResolved) { "Can only start new hand after HandResolved" }
        _state = GameState.Betting(timeBalance)
    }

    /**
     * Dealer logic: hit until hand value >= 17.
     * Stands on soft 17 (A+6 = soft 17 → stand).
     *
     * This is an explicit design choice — some casinos make the dealer hit
     * on soft 17 (H17). We use S17 (stand on soft 17) which is more
     * favorable to the player.
     */
    private fun playDealer(hand: Hand): Hand {
        var dealerHand = hand
        while (dealerHand.value < 17) {
            dealerHand = dealerHand.withCard(deck.deal())
        }
        // Stands on soft 17: if value == 17 and soft, still stands
        return dealerHand
    }

    /**
     * Resolve the hand: determine outcome, compute payout, update balance.
     */
    private fun resolveHand(playerHand: Hand, dealerHand: Hand, bet: Int) {
        // Play out dealer if not already done (needed for player blackjack path)
        val finalDealerHand = if (playerHand.isBlackjack()) {
            // Even with no-peek, dealer reveals and we compare
            playDealer(dealerHand)
        } else {
            dealerHand
        }

        val outcome: HandOutcome
        val payout: Int

        when {
            // Both have natural blackjack → push
            playerHand.isBlackjack() && finalDealerHand.isBlackjack() -> {
                outcome = HandOutcome.PUSH
                payout = bet // return bet only
            }
            // Player blackjack beats everything else
            playerHand.isBlackjack() -> {
                outcome = HandOutcome.PLAYER_BLACKJACK
                // Return bet + 1.5x bonus, rounded down
                payout = bet + (bet * 3) / 2
            }
            // Player bust — already handled in hit(), but defensive
            playerHand.isBust() -> {
                outcome = HandOutcome.DEALER_WIN
                payout = 0
            }
            // Dealer busts
            finalDealerHand.isBust() -> {
                outcome = HandOutcome.PLAYER_WIN
                payout = bet * 2 // return bet + 1x bet
            }
            // Compare values
            playerHand.value > finalDealerHand.value -> {
                outcome = HandOutcome.PLAYER_WIN
                payout = bet * 2
            }
            playerHand.value == finalDealerHand.value -> {
                outcome = HandOutcome.PUSH
                payout = bet
            }
            else -> {
                outcome = HandOutcome.DEALER_WIN
                payout = 0
            }
        }

        // Add payout back to balance
        timeBalance += payout

        _state = GameState.HandResolved(
            playerHand = playerHand,
            dealerHand = finalDealerHand,
            outcome = outcome,
            payout = payout,
            bet = bet,
            timeBalance = timeBalance
        )
    }
}
