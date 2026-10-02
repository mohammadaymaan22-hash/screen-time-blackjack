package com.placeholder.screentimeblackjack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an app configured by the user to be monitored and blocked when time runs out.
 *
 * @property packageName Unique application package identifier (e.g., com.instagram.android).
 * @property appName Human-readable name of the application (e.g., Instagram).
 * @property isBlocked Whether this app is currently subject to screen-time gating.
 * @property addedAt Epoch timestamp when the app was added.
 */
@Entity(tableName = "blocked_apps")
data class BlockedApp(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isBlocked: Boolean = true,
    val addedAt: Long = System.currentTimeMillis()
)
