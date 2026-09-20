package com.gymfuel.app.core.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods WHERE (isTemplate = 1 OR ownerId = :ownerId) AND deletedAtEpochMillis IS NULL ORDER BY isFavorite DESC, name COLLATE NOCASE")
    fun observeAll(ownerId: String): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE id = :id AND (isTemplate = 1 OR ownerId = :ownerId) AND deletedAtEpochMillis IS NULL")
    suspend fun find(id: String, ownerId: String): FoodEntity?

    @Query("SELECT * FROM foods WHERE id = :id AND ownerId = :ownerId")
    suspend fun findOwnedIncludingDeleted(id: String, ownerId: String): FoodEntity?

    @Query("SELECT COUNT(*) FROM foods WHERE isTemplate = 1")
    suspend fun templateCount(): Int

    @Upsert
    suspend fun upsert(food: FoodEntity)

    @Upsert
    suspend fun upsertAll(foods: List<FoodEntity>)

    @Query("UPDATE foods SET syncState = :state WHERE id = :id AND ownerId = :ownerId")
    suspend fun updateSyncState(id: String, ownerId: String, state: String)

    @Query("UPDATE foods SET ownerId = :ownerId WHERE ownerId IS NULL AND isTemplate = 0")
    suspend fun claimLegacy(ownerId: String)
}

@Dao
interface FoodEntryDao {
    @Query("SELECT * FROM food_entries WHERE ownerId = :ownerId AND localDateEpochDay = :epochDay AND deletedAtEpochMillis IS NULL ORDER BY consumedAtEpochMillis DESC, updatedAtEpochMillis DESC")
    fun observeForDate(ownerId: String, epochDay: Long): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entries WHERE ownerId = :ownerId AND localDateEpochDay BETWEEN :startEpochDay AND :endEpochDay AND deletedAtEpochMillis IS NULL ORDER BY localDateEpochDay, consumedAtEpochMillis")
    fun observeBetween(ownerId: String, startEpochDay: Long, endEpochDay: Long): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entries WHERE ownerId = :ownerId AND deletedAtEpochMillis IS NULL ORDER BY localDateEpochDay, consumedAtEpochMillis")
    suspend fun allForExport(ownerId: String): List<FoodEntryEntity>

    @Query("SELECT * FROM food_entries WHERE ownerId = :ownerId AND localDateEpochDay BETWEEN :startEpochDay AND :endEpochDay AND deletedAtEpochMillis IS NULL ORDER BY localDateEpochDay, consumedAtEpochMillis")
    suspend fun forExport(ownerId: String, startEpochDay: Long, endEpochDay: Long): List<FoodEntryEntity>

    @Query("SELECT * FROM food_entries WHERE id = :id AND ownerId = :ownerId")
    suspend fun find(id: String, ownerId: String): FoodEntryEntity?

    @Upsert
    suspend fun upsert(entry: FoodEntryEntity)

    @Query("UPDATE food_entries SET syncState = :state WHERE id = :id AND ownerId = :ownerId")
    suspend fun updateSyncState(id: String, ownerId: String, state: String)

    @Query("UPDATE food_entries SET ownerId = :ownerId WHERE ownerId IS NULL")
    suspend fun claimLegacy(ownerId: String)
}

@Dao
interface WaterEntryDao {
    @Query("SELECT * FROM water_entries WHERE ownerId = :ownerId AND localDateEpochDay = :epochDay AND deletedAtEpochMillis IS NULL ORDER BY loggedAtEpochMillis DESC")
    fun observeForDate(ownerId: String, epochDay: Long): Flow<List<WaterEntryEntity>>

    @Query("SELECT * FROM water_entries WHERE ownerId = :ownerId AND localDateEpochDay BETWEEN :startEpochDay AND :endEpochDay AND deletedAtEpochMillis IS NULL ORDER BY localDateEpochDay, loggedAtEpochMillis")
    fun observeBetween(ownerId: String, startEpochDay: Long, endEpochDay: Long): Flow<List<WaterEntryEntity>>

    @Query("SELECT * FROM water_entries WHERE id = :id AND ownerId = :ownerId")
    suspend fun find(id: String, ownerId: String): WaterEntryEntity?

    @Upsert
    suspend fun upsert(entry: WaterEntryEntity)

    @Query("UPDATE water_entries SET syncState = :state WHERE id = :id AND ownerId = :ownerId")
    suspend fun updateSyncState(id: String, ownerId: String, state: String)

    @Query("UPDATE water_entries SET ownerId = :ownerId WHERE ownerId IS NULL")
    suspend fun claimLegacy(ownerId: String)
}

@Dao
interface OutboxDao {
    @Query("SELECT COUNT(*) FROM sync_outbox WHERE ownerId = :ownerId")
    fun observeCount(ownerId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM sync_outbox WHERE ownerId = :ownerId AND lastError IS NOT NULL")
    fun observeFailedCount(ownerId: String): Flow<Int>

    @Query("SELECT * FROM sync_outbox WHERE ownerId = :ownerId ORDER BY createdAtEpochMillis LIMIT :limit")
    suspend fun pending(ownerId: String, limit: Int = 50): List<OutboxEntity>

    @Query("SELECT * FROM sync_outbox WHERE id = :id AND ownerId = :ownerId")
    suspend fun find(id: String, ownerId: String): OutboxEntity?

    @Upsert
    suspend fun upsert(item: OutboxEntity)

    @Query("DELETE FROM sync_outbox WHERE id = :id AND createdAtEpochMillis = :createdAtEpochMillis")
    suspend fun deleteIfUnchanged(id: String, createdAtEpochMillis: Long): Int

    @Query("UPDATE sync_outbox SET attemptCount = attemptCount + 1, lastError = :message WHERE id = :id AND createdAtEpochMillis = :createdAtEpochMillis")
    suspend fun markFailedIfUnchanged(id: String, createdAtEpochMillis: Long, message: String): Int

    @Query("UPDATE sync_outbox SET ownerId = :ownerId WHERE ownerId IS NULL")
    suspend fun claimLegacy(ownerId: String)
}

@Dao
interface NutritionTargetDao {
    @Query("SELECT * FROM nutrition_targets WHERE ownerId = :ownerId AND effectiveDateEpochDay <= :epochDay ORDER BY effectiveDateEpochDay DESC LIMIT 1")
    fun observeCurrent(ownerId: String, epochDay: Long): Flow<NutritionTargetEntity?>

    @Query("SELECT * FROM nutrition_targets WHERE id = :id AND ownerId = :ownerId")
    suspend fun find(id: String, ownerId: String): NutritionTargetEntity?

    @Query("SELECT * FROM nutrition_targets WHERE ownerId = :ownerId AND effectiveDateEpochDay = :epochDay LIMIT 1")
    suspend fun findForDate(ownerId: String, epochDay: Long): NutritionTargetEntity?

    @Upsert
    suspend fun upsert(target: NutritionTargetEntity)

    @Query("UPDATE nutrition_targets SET syncState = :state WHERE id = :id AND ownerId = :ownerId")
    suspend fun updateSyncState(id: String, ownerId: String, state: String)

    @Query("UPDATE nutrition_targets SET ownerId = :ownerId WHERE ownerId IS NULL")
    suspend fun claimLegacy(ownerId: String)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE ownerId = :ownerId")
    fun observe(ownerId: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE ownerId = :ownerId")
    suspend fun find(ownerId: String): UserProfileEntity?

    @Upsert
    suspend fun upsert(profile: UserProfileEntity)

    @Query("UPDATE user_profiles SET syncState = :state WHERE ownerId = :ownerId")
    suspend fun updateSyncState(ownerId: String, state: String)
}
