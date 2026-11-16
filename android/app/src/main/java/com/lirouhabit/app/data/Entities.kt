package com.lirouhabit.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dreams")
data class Dream(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val priority: Int = 1, // 1..3 (3 = mais importante)
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "vices")
data class Vice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "hobbies")
data class Hobby(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val lastPracticedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tracked_apps")
data class TrackedApp(
    @PrimaryKey val packageName: String,
    val appName: String,
    val limitMinutes: Int = 5,
    val enabled: Boolean = true
)

@Entity(tableName = "intervention_events")
data class InterventionEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val outcome: String, // TRIGGERED | VICTORY | DEFEAT
    val phrase: String = ""
)
