package com.gymfuel.app.core.model

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyNutritionTest {
    private val food = Food(
        id = "food-1",
        name = "White rice",
        preparation = Preparation.Cooked,
        nutritionPer100g = NutritionPer100g(
            calories = BigDecimal("130"),
            proteinGrams = BigDecimal("2.7"),
            carbohydrateGrams = BigDecimal("28.2"),
            fatGrams = BigDecimal("0.3"),
        ),
    )

    @Test
    fun `consumed total excludes planned and skipped entries`() {
        val entries = listOf(
            FoodEntry.fromFood("1", food, BigDecimal("200"), EntryStatus.Consumed, LocalDate.parse("2026-09-16"), Instant.EPOCH),
            FoodEntry.fromFood("2", food, BigDecimal("100"), EntryStatus.Planned, LocalDate.parse("2026-09-16"), null),
            FoodEntry.fromFood("3", food, BigDecimal("300"), EntryStatus.Skipped, LocalDate.parse("2026-09-16"), null),
        )

        val summary = DailyNutrition.from(entries)

        assertEquals(BigDecimal("260.00"), summary.consumed.calories)
        assertEquals(BigDecimal("390.00"), summary.forecast.calories)
    }

    @Test
    fun `entry keeps food snapshot when food changes`() {
        val entry = FoodEntry.fromFood(
            id = "entry-1",
            food = food,
            quantityGrams = BigDecimal("100"),
            status = EntryStatus.Consumed,
            localDate = LocalDate.parse("2026-09-16"),
            consumedAt = Instant.EPOCH,
        )
        val editedFood = food.copy(name = "Rice edited")

        assertEquals("White rice", entry.foodNameSnapshot)
        assertEquals("Rice edited", editedFood.name)
    }
}
