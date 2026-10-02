package com.placeholder.screentimeblackjack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an app monitored with its own independent screen-time balance.
 *
 * @property packageName Unique application package identifier (e.g., com.google.android.youtube).
 * @property appName Human-readable name of the application (e.g., YouTube).
 * @property isBlocked Whether this app is currently subject to screen-time gating.
 * @property timeBalanceMinutes Independent allocated screen time in minutes for this specific app.
 * @property timeBalanceSeconds Exact second-by-second balance remaining.
 * @property addedAt Epoch timestamp when the app was added.
 */
@Entity(tableName = "blocked_apps")
data class BlockedApp(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isBlocked: Boolean = true,
    val timeBalanceMinutes: Int = 15,
    val timeBalanceSeconds: Int = 15 * 60,
    val addedAt: Long = System.currentTimeMillis()
)
