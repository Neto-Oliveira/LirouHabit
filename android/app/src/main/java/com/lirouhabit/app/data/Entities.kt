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

/** Rotina de trabalho/foco com dias e horário; minutos desde meia-noite. */
@Entity(tableName = "work_routines")
data class WorkRoutine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val daysMask: Int = 31, // seg(1) … sex(16); dom=64
    val startMinute: Int = 8 * 60,
    val endMinute: Int = 18 * 60,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isActiveAt(dayOfWeek: Int, minuteOfDay: Int): Boolean {
        if (!enabled) return false
        if (daysMask and (1 shl (dayOfWeek - 1)) == 0) return false
        if (startMinute == endMinute) return false
        return if (startMinute < endMinute) {
            minuteOfDay in startMinute until endMinute
        } else {
            // Rotina que atravessa a meia-noite (ex.: 22h → 06h)
            minuteOfDay >= startMinute || minuteOfDay < endMinute
        }
    }
}

@Entity(tableName = "intervention_events")
data class InterventionEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val outcome: String, // TRIGGERED | VICTORY | DEFEAT
    val phrase: String = ""
)
