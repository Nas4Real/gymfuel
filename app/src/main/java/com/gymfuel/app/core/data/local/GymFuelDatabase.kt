package com.gymfuel.app.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FoodEntity::class, FoodEntryEntity::class, OutboxEntity::class, NutritionTargetEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class GymFuelDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun foodEntryDao(): FoodEntryDao
    abstract fun outboxDao(): OutboxDao
    abstract fun nutritionTargetDao(): NutritionTargetDao

    companion object {
        fun create(context: Context): GymFuelDatabase = Room.databaseBuilder(
            context.applicationContext,
            GymFuelDatabase::class.java,
            "gymfuel.db",
        ).build()
    }
}
