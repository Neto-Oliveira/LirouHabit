package com.lirouhabit.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tempo acumulado por dia em cada app vilão — base do tracker de evolução.
 */
@Entity(tableName = "daily_usage", primaryKeys = ["date", "packageName"])
data class DailyUsage(
    val date: String,        // "yyyy-MM-dd"
    val packageName: String,
    val appName: String,
    val totalMs: Long
) {
    // Room exige chave composta; declaramos via @Entity primaryKeys
}
