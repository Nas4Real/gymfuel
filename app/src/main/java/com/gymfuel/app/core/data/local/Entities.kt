package com.gymfuel.app.core.data.local

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "foods", indices = [Index("name"), Index("updatedAtEpochMillis"), Index("ownerId")])
data class FoodEntity(
    @PrimaryKey val id: String,
    val ownerId: String?,
    val sourceTemplateId: String?,
    val name: String,
    val brand: String?,
    val preparation: String,
    val caloriesPer100g: String,
    val proteinPer100g: String,
    val carbohydratePer100g: String,
    val fatPer100g: String,
    val imageReference: String?,
    val isFavorite: Boolean,
    val isTemplate: Boolean,
    val updatedAtEpochMillis: Long,
    val deletedAtEpochMillis: Long?,
    val revision: Long,
    val syncState: String,
)

@Entity(
    tableName = "food_entries",
    indices = [Index("localDateEpochDay"), Index("foodId"), Index("updatedAtEpochMillis"), Index(value = ["ownerId", "localDateEpochDay"])],
)
data class FoodEntryEntity(
    @PrimaryKey val id: String,
    val ownerId: String?,
    val foodId: String?,
    val localDateEpochDay: Long,
    val quantityGrams: String,
    val status: String,
    val consumedAtEpochMillis: Long?,
    val foodNameSnapshot: String,
    val preparationSnapshot: String,
    val imageReferenceSnapshot: String?,
    val caloriesPer100gSnapshot: String,
    val proteinPer100gSnapshot: String,
    val carbohydratePer100gSnapshot: String,
    val fatPer100gSnapshot: String,
    val updatedAtEpochMillis: Long,
    val deletedAtEpochMillis: Long?,
    val revision: Long,
    val syncState: String,
)

@Entity(tableName = "water_entries", indices = [Index("localDateEpochDay"), Index("updatedAtEpochMillis"), Index(value = ["ownerId", "localDateEpochDay"])])
data class WaterEntryEntity(
    @PrimaryKey val id: String,
    val ownerId: String?,
    val localDateEpochDay: Long,
    val liters: String,
    val loggedAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val deletedAtEpochMillis: Long?,
    val revision: Long,
    val syncState: String,
)

@Entity(tableName = "sync_outbox", indices = [Index(value = ["entityType", "entityId"], unique = true), Index(value = ["ownerId", "createdAtEpochMillis"])])
data class OutboxEntity(
    @PrimaryKey val id: String,
    val ownerId: String?,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val createdAtEpochMillis: Long,
    val attemptCount: Int = 0,
    val lastError: String? = null,
)

@Entity(tableName = "nutrition_targets", indices = [Index("effectiveDateEpochDay"), Index(value = ["ownerId", "effectiveDateEpochDay"])])
data class NutritionTargetEntity(
    @PrimaryKey val id: String,
    val ownerId: String?,
    val effectiveDateEpochDay: Long,
    val calories: String,
    val proteinGrams: String,
    val carbohydrateGrams: String,
    val fatGrams: String,
    @ColumnInfo(defaultValue = "'2.5'") val waterLiters: String,
    val ageYears: Int?,
    val formulaSex: String?,
    val heightCentimeters: String?,
    val weightKilograms: String?,
    val activityMultiplier: String?,
    val surplusCalories: String?,
    val updatedAtEpochMillis: Long,
    val revision: Long,
    val syncState: String,
)

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val ownerId: String,
    val email: String?,
    val ageYears: Int,
    val formulaSex: String,
    val heightCentimeters: String,
    val weightKilograms: String,
    val activityMultiplier: String,
    val surplusCalories: String,
    val unitSystem: String,
    val timeZone: String,
    val updatedAtEpochMillis: Long,
    val revision: Long,
    val syncState: String,
)
