package com.lirouhabit.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(usage: DailyUsage)

    @Query("SELECT * FROM daily_usage ORDER BY date DESC, totalMs DESC")
    fun all(): Flow<List<DailyUsage>>

    @Query("SELECT * FROM daily_usage WHERE packageName = :pkg ORDER BY date DESC")
    fun byPackage(pkg: String): Flow<List<DailyUsage>>

    @Query("SELECT * FROM daily_usage WHERE date = :date")
    suspend fun forDate(date: String): List<DailyUsage>

    @Query("SELECT SUM(totalMs) FROM daily_usage WHERE date = :date")
    suspend fun totalForDate(date: String): Long?
}
