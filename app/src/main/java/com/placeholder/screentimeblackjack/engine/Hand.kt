package com.placeholder.screentimeblackjack.engine

/**
 * Represents a hand of cards in blackjack.
 *
 * Computes the best hand value, handling aces as 11 or 1:
 * - Start by counting all aces as 11
 * - For each ace, if total > 21, count that ace as 1 instead
 *
 * A hand is "soft" if at least one ace is still counted as 11.
 */
data class Hand(val cards: List<Card> = emptyList()) {

    /** Number of aces currently counted as 11 (i.e. contributing the soft +10). */
    private val softAceCount: Int
        get() {
            var total = cards.sumOf { it.rank.value }
            var aces = cards.count { it.rank == Rank.ACE }
            // Demote aces from 11 to 1 (subtract 10) until we're at or below 21
            while (total > 21 && aces > 0) {
                total -= 10
                aces--
            }
            return aces
        }

    /** Best hand value (aces optimally counted as 1 or 11). */
    val value: Int
        get() {
            var total = cards.sumOf { it.rank.value }
            var aces = cards.count { it.rank == Rank.ACE }
            while (total > 21 && aces > 0) {
                total -= 10
                aces--
            }
            return total
        }

    /** True if at least one ace is counted as 11 (a "soft" hand). */
    val isSoft: Boolean
        get() = softAceCount > 0

    /** True if the hand value exceeds 21. */
    fun isBust(): Boolean = value > 21

    /**
     * True if this is a natural blackjack: exactly 2 cards totaling 21.
     * (e.g. Ace + 10/J/Q/K)
     */
    fun isBlackjack(): Boolean = cards.size == 2 && value == 21

    /** Return a new Hand with the given card added. */
    fun withCard(card: Card): Hand = copy(cards = cards + card)

    /** Text representation of all cards, e.g. "A♥ 10♠" */
    override fun toString(): String = cards.joinToString(" ")
}
