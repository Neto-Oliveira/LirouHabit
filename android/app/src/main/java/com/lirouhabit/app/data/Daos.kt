package com.lirouhabit.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DreamDao {
    @Query("SELECT * FROM dreams ORDER BY priority DESC, createdAt DESC")
    fun all(): Flow<List<Dream>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(dream: Dream): Long

    @Delete
    suspend fun delete(dream: Dream)
}

@Dao
interface ViceDao {
    @Query("SELECT * FROM vices ORDER BY createdAt DESC")
    fun all(): Flow<List<Vice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vice: Vice): Long

    @Delete
    suspend fun delete(vice: Vice)
}

@Dao
interface HobbyDao {
    @Query("SELECT * FROM hobbies ORDER BY createdAt DESC")
    fun all(): Flow<List<Hobby>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(hobby: Hobby): Long

    @Delete
    suspend fun delete(hobby: Hobby)
}

@Dao
interface TrackedAppDao {
    @Query("SELECT * FROM tracked_apps ORDER BY appName")
    fun all(): Flow<List<TrackedApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(app: TrackedApp)

    @Delete
    suspend fun delete(app: TrackedApp)
}

@Dao
interface EventDao {
    @Insert
    suspend fun insert(event: InterventionEvent): Long

    @Query("SELECT * FROM intervention_events ORDER BY timestamp DESC LIMIT 200")
    fun recent(): Flow<List<InterventionEvent>>

    @Query("SELECT COUNT(*) FROM intervention_events WHERE outcome = 'VICTORY'")
    fun victories(): Flow<Int>

    @Query("SELECT COUNT(*) FROM intervention_events WHERE outcome = 'DEFEAT'")
    fun defeats(): Flow<Int>

    @Query("SELECT COUNT(*) FROM intervention_events WHERE outcome = 'TRIGGERED'")
    fun triggers(): Flow<Int>
}
