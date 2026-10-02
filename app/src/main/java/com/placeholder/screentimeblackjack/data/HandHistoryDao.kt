package com.placeholder.screentimeblackjack.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HandHistoryDao {

    @Insert
    suspend fun insert(hand: HandHistory)

    @Query("SELECT * FROM hand_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHandsFlow(limit: Int = 50): Flow<List<HandHistory>>

    @Query("SELECT COUNT(*) FROM hand_history WHERE timestamp >= :sinceTimestamp")
    suspend fun getHandsCountSince(sinceTimestamp: Long): Int

    @Query("SELECT * FROM hand_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentHandsList(limit: Int = 20): List<HandHistory>

    @Query("DELETE FROM hand_history")
    suspend fun clearHistory()
}
