package com.placeholder.screentimeblackjack.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.placeholder.screentimeblackjack.engine.HandOutcome

/**
 * Persisted record of a completed blackjack hand.
 *
 * @property id Auto-incrementing primary key.
 * @property timestamp Epoch millis when the hand resolved.
 * @property bet Bet placed in minutes.
 * @property payout Payout received in minutes.
 * @property outcome Name of the resolved HandOutcome.
 * @property playerCards String representation of player cards (e.g. "10♠ A♥").
 * @property playerValue Final numeric value of the player hand.
 * @property dealerCards String representation of dealer cards (e.g. "8♦ 9♣").
 * @property dealerValue Final numeric value of the dealer hand.
 * @property balanceAfter Final player balance after payout.
 */
@Entity(tableName = "hand_history")
data class HandHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val bet: Int,
    val payout: Int,
    val outcome: HandOutcome,
    val playerCards: String,
    val playerValue: Int,
    val dealerCards: String,
    val dealerValue: Int,
    val balanceAfter: Int
)
