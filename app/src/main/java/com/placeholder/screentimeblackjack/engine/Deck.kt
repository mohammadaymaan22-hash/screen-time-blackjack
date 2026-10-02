package com.placeholder.screentimeblackjack.engine

import kotlin.random.Random

/**
 * A shoe of one or more standard 52-card decks.
 *
 * @param numberOfDecks How many decks to include (default 1).
 * @param random Random instance for shuffling (injectable for testing).
 * @param reshuffleThreshold When fewer than this many cards remain, reshuffle
 *                           all cards back into the shoe (default 15).
 */
class Deck(
    private val numberOfDecks: Int = 1,
    private val random: Random = Random,
    private val reshuffleThreshold: Int = 15
) {
    private val cards: MutableList<Card> = mutableListOf()

    init {
        require(numberOfDecks >= 1) { "numberOfDecks must be >= 1, was $numberOfDecks" }
        require(reshuffleThreshold >= 0) { "reshuffleThreshold must be >= 0" }
        buildAndShuffle()
    }

    /** Number of cards remaining in the shoe. */
    val remaining: Int get() = cards.size

    /**
     * Deal one card from the top of the shoe.
     * If the shoe is below the reshuffle threshold after dealing, reshuffle.
     */
    fun deal(): Card {
        if (cards.isEmpty()) {
            buildAndShuffle()
        }
        val card = cards.removeAt(cards.lastIndex)
        if (cards.size < reshuffleThreshold) {
            buildAndShuffle()
        }
        return card
    }

    /** Rebuild the full shoe from scratch and shuffle using Fisher-Yates. */
    private fun buildAndShuffle() {
        cards.clear()
        repeat(numberOfDecks) {
            for (suit in Suit.entries) {
                for (rank in Rank.entries) {
                    cards.add(Card(rank, suit))
                }
            }
        }
        shuffle()
    }

    /** Fisher-Yates (Knuth) shuffle — in-place, O(n). */
    private fun shuffle() {
        for (i in cards.lastIndex downTo 1) {
            val j = random.nextInt(i + 1)
            val temp = cards[i]
            cards[i] = cards[j]
            cards[j] = temp
        }
    }
}
