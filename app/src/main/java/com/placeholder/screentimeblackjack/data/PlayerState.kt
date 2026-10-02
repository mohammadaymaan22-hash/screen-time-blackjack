package com.placeholder.screentimeblackjack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity storing the player's persistent state.
 * Single-row table — there's only one player in v1.
 */
@Entity(tableName = "player_state")
data class PlayerState(
    @PrimaryKey
    val id: Int = 1, // Single row

    /** Current time balance in minutes. */
    val timeBalance: Int = 60,

    /** Timestamp of last balance update (epoch millis). */
    val lastUpdated: Long = System.currentTimeMillis()
)
