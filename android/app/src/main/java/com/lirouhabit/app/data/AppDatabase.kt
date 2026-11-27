package com.lirouhabit.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Dream::class, Vice::class, Hobby::class, TrackedApp::class, InterventionEvent::class, DailyUsage::class, WorkRoutine::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dreamDao(): DreamDao
    abstract fun viceDao(): ViceDao
    abstract fun hobbyDao(): HobbyDao
    abstract fun trackedAppDao(): TrackedAppDao
    abstract fun eventDao(): EventDao
    abstract fun dailyUsageDao(): DailyUsageDao
    abstract fun workRoutineDao(): WorkRoutineDao

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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS hobbies (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "text TEXT NOT NULL, " +
                        "lastPracticedAt INTEGER NOT NULL, " +
                        "createdAt INTEGER NOT NULL" +
                        ")"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS work_routines (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "label TEXT NOT NULL, daysMask INTEGER NOT NULL, " +
                        "startMinute INTEGER NOT NULL, endMinute INTEGER NOT NULL, " +
                        "enabled INTEGER NOT NULL, createdAt INTEGER NOT NULL)"
                )
            }
        }

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lirouhabit.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build().also { instance = it }
            }
    }
}
