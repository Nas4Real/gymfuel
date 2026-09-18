package com.gymfuel.app.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [FoodEntity::class, FoodEntryEntity::class, WaterEntryEntity::class, OutboxEntity::class, NutritionTargetEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class GymFuelDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun foodEntryDao(): FoodEntryDao
    abstract fun waterEntryDao(): WaterEntryDao
    abstract fun outboxDao(): OutboxDao
    abstract fun nutritionTargetDao(): NutritionTargetDao

    companion object {
        fun create(context: Context): GymFuelDatabase = Room.databaseBuilder(
            context.applicationContext,
            GymFuelDatabase::class.java,
            "gymfuel.db",
        ).addMigrations(MIGRATION_1_2).build()

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE nutrition_targets ADD COLUMN waterLiters TEXT NOT NULL DEFAULT '2.5'")
                database.execSQL(
                    "UPDATE nutrition_targets SET waterLiters = CAST(ROUND(CAST(weightKilograms AS REAL) * 0.035, 1) AS TEXT) WHERE weightKilograms IS NOT NULL",
                )
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS water_entries (
                        id TEXT NOT NULL PRIMARY KEY,
                        ownerId TEXT,
                        localDateEpochDay INTEGER NOT NULL,
                        liters TEXT NOT NULL,
                        loggedAtEpochMillis INTEGER NOT NULL,
                        updatedAtEpochMillis INTEGER NOT NULL,
                        deletedAtEpochMillis INTEGER,
                        revision INTEGER NOT NULL,
                        syncState TEXT NOT NULL
                    )""".trimIndent(),
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_water_entries_localDateEpochDay ON water_entries (localDateEpochDay)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_water_entries_updatedAtEpochMillis ON water_entries (updatedAtEpochMillis)")
            }
        }
    }
}
