package com.gymfuel.app.core.model

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

enum class Preparation(val wireValue: String) {
    Raw("raw"),
    Cooked("cooked"),
    Dry("dry"),
    Drained("drained"),
    Custom("custom"),
}

enum class EntryStatus(val wireValue: String) {
    Planned("planned"),
    Consumed("consumed"),
    Skipped("skipped"),
}

enum class SyncState {
    Synced,
    Pending,
    Failed,
}

data class Food(
    val id: String,
    val name: String,
    val preparation: Preparation,
    val nutritionPer100g: NutritionPer100g,
    val brand: String? = null,
    val imageReference: String? = null,
    val sourceTemplateId: String? = null,
    val isFavorite: Boolean = false,
    val syncState: SyncState = SyncState.Pending,
) {
    init {
        require(name.isNotBlank()) { "Food name is required" }
        require(name.length <= 120) { "Food name cannot exceed 120 characters" }
    }
}

data class FoodEntry(
    val id: String,
    val foodId: String?,
    val localDate: LocalDate,
    val quantityGrams: BigDecimal,
    val status: EntryStatus,
    val consumedAt: Instant?,
    val foodNameSnapshot: String,
    val preparationSnapshot: Preparation,
    val imageReferenceSnapshot: String?,
    val nutritionPer100gSnapshot: NutritionPer100g,
    val syncState: SyncState = SyncState.Pending,
) {
    init {
        require(quantityGrams.signum() > 0) { "Quantity must be positive" }
        require(quantityGrams <= BigDecimal("100000")) { "Quantity cannot exceed 100000 g" }
        require(status != EntryStatus.Consumed || consumedAt != null) {
            "Consumed entries require a consumption time"
        }
    }

    companion object {
        fun fromFood(
            id: String,
            food: Food,
            quantityGrams: BigDecimal,
            status: EntryStatus,
            localDate: LocalDate,
            consumedAt: Instant?,
        ) = FoodEntry(
            id = id,
            foodId = food.id,
            localDate = localDate,
            quantityGrams = quantityGrams,
            status = status,
            consumedAt = consumedAt,
            foodNameSnapshot = food.name,
            preparationSnapshot = food.preparation,
            imageReferenceSnapshot = food.imageReference,
            nutritionPer100gSnapshot = food.nutritionPer100g,
        )
    }
}
