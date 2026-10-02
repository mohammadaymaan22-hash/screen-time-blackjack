package com.placeholder.screentimeblackjack.engine

import org.junit.Assert.*
import org.junit.Test

class HandTest {

    // ── Ace value logic ──

    @Test
    fun `single ace counts as 11`() {
        val hand = Hand(listOf(Card(Rank.ACE, Suit.HEARTS)))
        assertEquals(11, hand.value)
        assertTrue(hand.isSoft)
    }

    @Test
    fun `ace plus face card is blackjack with value 21`() {
        val hand = Hand(
            listOf(
                Card(Rank.ACE, Suit.HEARTS),
                Card(Rank.KING, Suit.SPADES)
            )
        )
        assertEquals(21, hand.value)
        assertTrue(hand.isBlackjack())
        assertTrue(hand.isSoft)
    }

    @Test
    fun `ace demoted to 1 when total would bust`() {
        // A(11) + 7 + 5 = 23 → A(1) + 7 + 5 = 13
        val hand = Hand(
            listOf(
                Card(Rank.ACE, Suit.HEARTS),
                Card(Rank.SEVEN, Suit.SPADES),
                Card(Rank.FIVE, Suit.CLUBS)
            )
        )
        assertEquals(13, hand.value)
        assertFalse(hand.isSoft)
    }

    @Test
    fun `two aces — one demoted to avoid bust`() {
        // A(11) + A(11) = 22 → A(11) + A(1) = 12
        val hand = Hand(
            listOf(
                Card(Rank.ACE, Suit.HEARTS),
                Card(Rank.ACE, Suit.SPADES)
            )
        )
        assertEquals(12, hand.value)
        assertTrue(hand.isSoft) // one ace still counted as 11
    }

    @Test
    fun `three aces — two demoted`() {
        // A(11) + A(11) + A(11) = 33 → A(11) + A(1) + A(1) = 13
        val hand = Hand(
            listOf(
                Card(Rank.ACE, Suit.HEARTS),
                Card(Rank.ACE, Suit.SPADES),
                Card(Rank.ACE, Suit.CLUBS)
            )
        )
        assertEquals(13, hand.value)
        assertTrue(hand.isSoft)
    }

    @Test
    fun `soft 17 — ace plus 6`() {
        val hand = Hand(
            listOf(
                Card(Rank.ACE, Suit.HEARTS),
                Card(Rank.SIX, Suit.SPADES)
            )
        )
        assertEquals(17, hand.value)
        assertTrue(hand.isSoft)
    }

    @Test
    fun `hard 17 — 10 plus 7`() {
        val hand = Hand(
            listOf(
                Card(Rank.TEN, Suit.HEARTS),
                Card(Rank.SEVEN, Suit.SPADES)
            )
        )
        assertEquals(17, hand.value)
        assertFalse(hand.isSoft)
    }

    // ── Bust detection ──

    @Test
    fun `hand with value over 21 is bust`() {
        val hand = Hand(
            listOf(
                Card(Rank.TEN, Suit.HEARTS),
                Card(Rank.SEVEN, Suit.SPADES),
                Card(Rank.EIGHT, Suit.CLUBS)
            )
        )
        assertTrue(hand.isBust())
        assertEquals(25, hand.value)
    }

    @Test
    fun `hand at exactly 21 is not bust`() {
        val hand = Hand(
            listOf(
                Card(Rank.TEN, Suit.HEARTS),
                Card(Rank.FIVE, Suit.SPADES),
                Card(Rank.SIX, Suit.CLUBS)
            )
        )
        assertFalse(hand.isBust())
    }

    // ── Blackjack detection ──

    @Test
    fun `natural blackjack — ace plus ten-value`() {
        val hand = Hand(
            listOf(
                Card(Rank.ACE, Suit.HEARTS),
                Card(Rank.TEN, Suit.SPADES)
            )
        )
        assertTrue(hand.isBlackjack())
    }

    @Test
    fun `21 with 3 cards is not blackjack`() {
        val hand = Hand(
            listOf(
                Card(Rank.SEVEN, Suit.HEARTS),
                Card(Rank.SEVEN, Suit.SPADES),
                Card(Rank.SEVEN, Suit.CLUBS)
            )
        )
        assertEquals(21, hand.value)
        assertFalse(hand.isBlackjack())
    }

    @Test
    fun `two non-ace cards totaling less than 21 is not blackjack`() {
        val hand = Hand(
            listOf(
                Card(Rank.TEN, Suit.HEARTS),
                Card(Rank.NINE, Suit.SPADES)
            )
        )
        assertFalse(hand.isBlackjack())
    }

    // ── withCard ──

    @Test
    fun `withCard returns new hand with card added`() {
        val hand = Hand(listOf(Card(Rank.TEN, Suit.HEARTS)))
        val newHand = hand.withCard(Card(Rank.FIVE, Suit.SPADES))
        assertEquals(1, hand.cards.size)
        assertEquals(2, newHand.cards.size)
        assertEquals(15, newHand.value)
    }

    // ── Empty hand ──

    @Test
    fun `empty hand has value 0`() {
        assertEquals(0, Hand().value)
        assertFalse(Hand().isBust())
        assertFalse(Hand().isBlackjack())
    }
}
