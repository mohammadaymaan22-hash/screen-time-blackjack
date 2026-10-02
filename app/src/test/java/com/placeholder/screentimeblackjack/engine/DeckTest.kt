package com.placeholder.screentimeblackjack.engine

import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class DeckTest {

    @Test
    fun `single deck has 52 cards`() {
        val deck = Deck(numberOfDecks = 1, reshuffleThreshold = 0)
        assertEquals(52, deck.remaining)
    }

    @Test
    fun `two-deck shoe has 104 cards`() {
        val deck = Deck(numberOfDecks = 2, reshuffleThreshold = 0)
        assertEquals(104, deck.remaining)
    }

    @Test
    fun `deal returns a card and decrements remaining`() {
        val deck = Deck(numberOfDecks = 1, reshuffleThreshold = 0)
        val card = deck.deal()
        assertNotNull(card)
        assertEquals(51, deck.remaining)
    }

    @Test
    fun `dealing all 52 cards returns unique cards`() {
        val deck = Deck(numberOfDecks = 1, reshuffleThreshold = 0)
        val dealt = (1..52).map { deck.deal() }
        assertEquals(52, dealt.toSet().size)
    }

    @Test
    fun `shuffle produces different orderings`() {
        // Use two decks with different seeds — should produce different orders
        val deck1 = Deck(numberOfDecks = 1, random = Random(42), reshuffleThreshold = 0)
        val deck2 = Deck(numberOfDecks = 1, random = Random(99), reshuffleThreshold = 0)

        val cards1 = (1..52).map { deck1.deal() }
        val cards2 = (1..52).map { deck2.deal() }

        // Extremely unlikely to be in the same order with different seeds
        assertNotEquals(cards1, cards2)
    }

    @Test
    fun `reshuffle triggers when below threshold`() {
        // Threshold = 50 means after dealing 3 cards (49 remaining), reshuffle
        val deck = Deck(numberOfDecks = 1, reshuffleThreshold = 50)
        deck.deal() // 51 remaining
        deck.deal() // 50 remaining
        deck.deal() // would be 49, triggers reshuffle → back to 52
        assertEquals(52, deck.remaining)
    }

    @Test
    fun `deterministic seed produces repeatable deals`() {
        val deck1 = Deck(numberOfDecks = 1, random = Random(123), reshuffleThreshold = 0)
        val deck2 = Deck(numberOfDecks = 1, random = Random(123), reshuffleThreshold = 0)

        val cards1 = (1..10).map { deck1.deal() }
        val cards2 = (1..10).map { deck2.deal() }

        assertEquals(cards1, cards2)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `zero decks throws`() {
        Deck(numberOfDecks = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative decks throws`() {
        Deck(numberOfDecks = -1)
    }
}
