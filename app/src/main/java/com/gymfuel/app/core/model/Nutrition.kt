package com.gymfuel.app.core.model

import java.math.BigDecimal
import java.math.RoundingMode

private val HUNDRED = BigDecimal("100")
private val MAX_CALORIES_PER_100G = BigDecimal("1000")
private val MAX_MACRO_GRAMS_PER_100G = BigDecimal("100")
private val MAX_PORTION_GRAMS = BigDecimal("100000")

data class NutritionPer100g(
    val calories: BigDecimal,
    val proteinGrams: BigDecimal,
    val carbohydrateGrams: BigDecimal,
    val fatGrams: BigDecimal,
) {
    init {
        require(calories.signum() >= 0) { "Calories cannot be negative" }
        require(proteinGrams.signum() >= 0) { "Protein cannot be negative" }
        require(carbohydrateGrams.signum() >= 0) { "Carbohydrates cannot be negative" }
        require(fatGrams.signum() >= 0) { "Fat cannot be negative" }
        require(calories <= MAX_CALORIES_PER_100G) { "Calories cannot exceed 1000 per 100 g" }
        require(proteinGrams <= MAX_MACRO_GRAMS_PER_100G) { "Protein cannot exceed 100 g per 100 g" }
        require(carbohydrateGrams <= MAX_MACRO_GRAMS_PER_100G) { "Carbohydrates cannot exceed 100 g per 100 g" }
        require(fatGrams <= MAX_MACRO_GRAMS_PER_100G) { "Fat cannot exceed 100 g per 100 g" }
    }

    fun forQuantity(quantityGrams: BigDecimal): NutritionTotals {
        require(quantityGrams.signum() > 0) { "Quantity must be positive" }
        require(quantityGrams <= MAX_PORTION_GRAMS) { "Quantity cannot exceed 100000 g" }
        fun scale(value: BigDecimal) =
            value.multiply(quantityGrams).divide(HUNDRED, 2, RoundingMode.HALF_UP)

        return NutritionTotals(
            calories = scale(calories),
            proteinGrams = scale(proteinGrams),
            carbohydrateGrams = scale(carbohydrateGrams),
            fatGrams = scale(fatGrams),
        )
    }
}

data class NutritionTotals(
    val calories: BigDecimal,
    val proteinGrams: BigDecimal,
    val carbohydrateGrams: BigDecimal,
    val fatGrams: BigDecimal,
) {
    operator fun plus(other: NutritionTotals) = NutritionTotals(
        calories = calories + other.calories,
        proteinGrams = proteinGrams + other.proteinGrams,
        carbohydrateGrams = carbohydrateGrams + other.carbohydrateGrams,
        fatGrams = fatGrams + other.fatGrams,
    )

    companion object {
        val Zero = NutritionTotals(
            calories = BigDecimal("0.00"),
            proteinGrams = BigDecimal("0.00"),
            carbohydrateGrams = BigDecimal("0.00"),
            fatGrams = BigDecimal("0.00"),
        )
    }
}

data class DailyNutrition(
    val consumed: NutritionTotals,
    val forecast: NutritionTotals,
) {
    companion object {
        fun from(entries: List<FoodEntry>): DailyNutrition {
            val consumedEntries = entries.filter { it.status == EntryStatus.Consumed }
            val plannedEntries = entries.filter { it.status == EntryStatus.Planned }
            val consumed = consumedEntries.fold(NutritionTotals.Zero) { total, entry ->
                total + entry.nutritionPer100gSnapshot.forQuantity(entry.quantityGrams)
            }
            val planned = plannedEntries.fold(NutritionTotals.Zero) { total, entry ->
                total + entry.nutritionPer100gSnapshot.forQuantity(entry.quantityGrams)
            }
            return DailyNutrition(consumed = consumed, forecast = consumed + planned)
        }
    }
}
