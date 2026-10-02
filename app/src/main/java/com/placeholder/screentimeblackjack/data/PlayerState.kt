package com.placeholder.screentimeblackjack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity storing the player's persistent state and economy/safeguard configuration.
 */
@Entity(tableName = "player_state")
data class PlayerState(
    @PrimaryKey
    val id: Int = 1, // Single row

    /** Current time balance in minutes. */
    val timeBalance: Int = 60,

    /** Timestamp of last balance update (epoch millis). */
    val lastUpdated: Long = System.currentTimeMillis(),

    /** True if balance resets daily to default (60m); false if rolling balance continues. */
    val useDailyReset: Boolean = false,

    /** Epoch millis of the last daily reset check. */
    val lastDailyResetTimestamp: Long = System.currentTimeMillis(),

    /** Default balance awarded on daily reset (default: 60 minutes). */
    val dailyResetBalance: Int = 60,

    /** Max hands allowed per hour (0 = no limit / disabled). Default: 0 (disabled). */
    val maxHandsPerHour: Int = 0,

    /** Consecutive loss threshold before a cooldown is triggered (0 = disabled). Default: 0 (disabled). */
    val consecutiveLossThreshold: Int = 0,

    /** Duration of cooldown in minutes when loss threshold is reached. Default: 15 minutes. */
    val cooldownDurationMinutes: Int = 15,

    /** Epoch millis until which player is in mandatory cooldown (0 if active). */
    val cooldownUntilTimestamp: Long = 0L,

    /** Hard daily loss cap in minutes (0 = disabled). Default: 0 (disabled). */
    val dailyLossCapMinutes: Int = 0,

    /** Cumulative minutes lost today. */
    val currentDailyLossMinutes: Int = 0,

    /** Count of consecutive losses currently tracked. */
    val consecutiveLossesCount: Int = 0
)
