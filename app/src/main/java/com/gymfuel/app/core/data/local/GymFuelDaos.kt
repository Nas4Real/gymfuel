package com.gymfuel.app.core.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods WHERE deletedAtEpochMillis IS NULL ORDER BY isFavorite DESC, name COLLATE NOCASE")
    fun observeAll(): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE id = :id AND deletedAtEpochMillis IS NULL")
    suspend fun find(id: String): FoodEntity?

    @Query("SELECT COUNT(*) FROM foods WHERE isTemplate = 1")
    suspend fun templateCount(): Int

    @Upsert
    suspend fun upsert(food: FoodEntity)

    @Upsert
    suspend fun upsertAll(foods: List<FoodEntity>)

    @Query("UPDATE foods SET syncState = :state WHERE id = :id")
    suspend fun updateSyncState(id: String, state: String)
}

@Dao
interface FoodEntryDao {
    @Query("SELECT * FROM food_entries WHERE localDateEpochDay = :epochDay AND deletedAtEpochMillis IS NULL ORDER BY consumedAtEpochMillis DESC, updatedAtEpochMillis DESC")
    fun observeForDate(epochDay: Long): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entries WHERE localDateEpochDay BETWEEN :startEpochDay AND :endEpochDay AND deletedAtEpochMillis IS NULL ORDER BY localDateEpochDay, consumedAtEpochMillis")
    fun observeBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entries WHERE id = :id")
    suspend fun find(id: String): FoodEntryEntity?

    @Upsert
    suspend fun upsert(entry: FoodEntryEntity)

    @Query("UPDATE food_entries SET syncState = :state WHERE id = :id")
    suspend fun updateSyncState(id: String, state: String)
}

@Dao
interface WaterEntryDao {
    @Query("SELECT * FROM water_entries WHERE localDateEpochDay = :epochDay AND deletedAtEpochMillis IS NULL ORDER BY loggedAtEpochMillis DESC")
    fun observeForDate(epochDay: Long): Flow<List<WaterEntryEntity>>

    @Query("SELECT * FROM water_entries WHERE localDateEpochDay BETWEEN :startEpochDay AND :endEpochDay AND deletedAtEpochMillis IS NULL ORDER BY localDateEpochDay, loggedAtEpochMillis")
    fun observeBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<WaterEntryEntity>>

    @Query("SELECT * FROM water_entries WHERE id = :id")
    suspend fun find(id: String): WaterEntryEntity?

    @Upsert
    suspend fun upsert(entry: WaterEntryEntity)

    @Query("UPDATE water_entries SET syncState = :state WHERE id = :id")
    suspend fun updateSyncState(id: String, state: String)
}

@Dao
interface OutboxDao {
    @Query("SELECT COUNT(*) FROM sync_outbox")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sync_outbox WHERE lastError IS NOT NULL")
    fun observeFailedCount(): Flow<Int>

    @Query("SELECT * FROM sync_outbox ORDER BY createdAtEpochMillis LIMIT :limit")
    suspend fun pending(limit: Int = 50): List<OutboxEntity>

    @Query("SELECT * FROM sync_outbox WHERE id = :id")
    suspend fun find(id: String): OutboxEntity?

    @Upsert
    suspend fun upsert(item: OutboxEntity)

    @Query("DELETE FROM sync_outbox WHERE id = :id AND createdAtEpochMillis = :createdAtEpochMillis")
    suspend fun deleteIfUnchanged(id: String, createdAtEpochMillis: Long): Int

    @Query("UPDATE sync_outbox SET attemptCount = attemptCount + 1, lastError = :message WHERE id = :id AND createdAtEpochMillis = :createdAtEpochMillis")
    suspend fun markFailedIfUnchanged(id: String, createdAtEpochMillis: Long, message: String): Int
}

@Dao
interface NutritionTargetDao {
    @Query("SELECT * FROM nutrition_targets WHERE effectiveDateEpochDay <= :epochDay ORDER BY effectiveDateEpochDay DESC LIMIT 1")
    fun observeCurrent(epochDay: Long): Flow<NutritionTargetEntity?>

    @Query("SELECT * FROM nutrition_targets WHERE id = :id")
    suspend fun find(id: String): NutritionTargetEntity?

    @Query("SELECT * FROM nutrition_targets WHERE effectiveDateEpochDay = :epochDay LIMIT 1")
    suspend fun findForDate(epochDay: Long): NutritionTargetEntity?

    @Upsert
    suspend fun upsert(target: NutritionTargetEntity)

    @Query("UPDATE nutrition_targets SET syncState = :state WHERE id = :id")
    suspend fun updateSyncState(id: String, state: String)
}
