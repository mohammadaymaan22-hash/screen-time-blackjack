package com.placeholder.screentimeblackjack.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedAppDao {

    @Query("SELECT * FROM blocked_apps ORDER BY addedAt DESC")
    fun getAllFlow(): Flow<List<BlockedApp>>

    @Query("SELECT * FROM blocked_apps WHERE isBlocked = 1")
    suspend fun getActiveBlockedApps(): List<BlockedApp>

    @Query("SELECT * FROM blocked_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getByPackage(packageName: String): BlockedApp?

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_apps WHERE packageName = :packageName AND isBlocked = 1)")
    suspend fun isAppBlocked(packageName: String): Boolean

    @Query("UPDATE blocked_apps SET timeBalanceMinutes = :minutes, timeBalanceSeconds = :seconds WHERE packageName = :packageName")
    suspend fun updateAppTime(packageName: String, minutes: Int, seconds: Int)

    @Query("UPDATE blocked_apps SET timeBalanceSeconds = :seconds WHERE packageName = :packageName")
    suspend fun updateAppSeconds(packageName: String, seconds: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(app: BlockedApp)

    @Delete
    suspend fun delete(app: BlockedApp)

    @Query("DELETE FROM blocked_apps WHERE packageName = :packageName")
    suspend fun deleteByPackage(packageName: String)
}
