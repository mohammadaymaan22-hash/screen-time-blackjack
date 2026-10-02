package com.placeholder.screentimeblackjack.engine

import org.junit.Assert.*
import org.junit.Test

class CardTest {

    @Test
    fun `toString formats card correctly`() {
        assertEquals("A♥", Card(Rank.ACE, Suit.HEARTS).toString())
        assertEquals("10♠", Card(Rank.TEN, Suit.SPADES).toString())
        assertEquals("K♦", Card(Rank.KING, Suit.DIAMONDS).toString())
        assertEquals("2♣", Card(Rank.TWO, Suit.CLUBS).toString())
    }

    @Test
    fun `rank values are correct for blackjack`() {
        assertEquals(11, Rank.ACE.value)
        assertEquals(10, Rank.TEN.value)
        assertEquals(10, Rank.JACK.value)
        assertEquals(10, Rank.QUEEN.value)
        assertEquals(10, Rank.KING.value)
        assertEquals(5, Rank.FIVE.value)
    }
}
