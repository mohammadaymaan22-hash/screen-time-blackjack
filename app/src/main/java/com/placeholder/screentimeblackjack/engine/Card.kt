package com.placeholder.screentimeblackjack.engine

/**
 * Represents a playing card suit.
 */
enum class Suit(val symbol: String) {
    HEARTS("♥"),
    DIAMONDS("♦"),
    CLUBS("♣"),
    SPADES("♠")
}

/**
 * Represents a playing card rank with its blackjack point value.
 * Face cards (J, Q, K) are worth 10. Aces are worth 11 here;
 * soft/hard ace logic is handled at the Hand level.
 */
enum class Rank(val display: String, val value: Int) {
    ACE("A", 11),
    TWO("2", 2),
    THREE("3", 3),
    FOUR("4", 4),
    FIVE("5", 5),
    SIX("6", 6),
    SEVEN("7", 7),
    EIGHT("8", 8),
    NINE("9", 9),
    TEN("10", 10),
    JACK("J", 10),
    QUEEN("Q", 10),
    KING("K", 10)
}

/**
 * A single playing card.
 */
data class Card(val rank: Rank, val suit: Suit) {
    /** Text representation, e.g. "A♥", "10♠" */
    override fun toString(): String = "${rank.display}${suit.symbol}"
}
