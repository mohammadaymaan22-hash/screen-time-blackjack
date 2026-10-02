package com.placeholder.screentimeblackjack.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlayerStateDao {

    /** Get the player state (single row, id = 1). */
    @Query("SELECT * FROM player_state WHERE id = 1")
    suspend fun get(): PlayerState?

    @Query("SELECT * FROM player_state WHERE id = 1")
    fun getFlow(): kotlinx.coroutines.flow.Flow<PlayerState?>

    /** Insert or replace the player state. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: PlayerState)

    /** Update just the time balance and lastUpdated timestamp. */
    @Query("UPDATE player_state SET timeBalance = :balance, lastUpdated = :timestamp WHERE id = 1")
    suspend fun updateBalance(balance: Int, timestamp: Long = System.currentTimeMillis())
}
