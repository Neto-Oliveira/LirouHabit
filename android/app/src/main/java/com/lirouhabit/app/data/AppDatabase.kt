package com.lirouhabit.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Dream::class, Vice::class, TrackedApp::class, InterventionEvent::class, DailyUsage::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dreamDao(): DreamDao
    abstract fun viceDao(): ViceDao
    abstract fun trackedAppDao(): TrackedAppDao
    abstract fun eventDao(): EventDao
    abstract fun dailyUsageDao(): DailyUsageDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS daily_usage (" +
                        "date TEXT NOT NULL, " +
                        "packageName TEXT NOT NULL, " +
                        "appName TEXT NOT NULL, " +
                        "totalMs INTEGER NOT NULL, " +
                        "PRIMARY KEY(date, packageName)" +
                        ")"
                )
            }
        }

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lirouhabit.db"
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}
