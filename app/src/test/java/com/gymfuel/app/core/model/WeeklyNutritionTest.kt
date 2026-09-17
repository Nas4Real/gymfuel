package com.gymfuel.app.core.model

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklyNutritionTest {
    @Test
    fun sevenDayAverage_includesDaysWithNoEntries() {
        val food = Food("food", "Chicken", Preparation.Cooked, NutritionPer100g(BigDecimal("100"), BigDecimal("20"), BigDecimal.ZERO, BigDecimal.ZERO))
        val entry = FoodEntry.fromFood("entry", food, BigDecimal("200"), EntryStatus.Consumed, LocalDate.of(2026, 9, 16), Instant.parse("2026-09-16T12:00:00Z"))

        val week = WeeklyNutrition.from(listOf(entry), LocalDate.of(2026, 9, 16))

        assertEquals(7, week.days.size)
        assertEquals(BigDecimal("28.57"), week.averageConsumed.calories)
        assertEquals(BigDecimal("5.71"), week.averageConsumed.proteinGrams)
    }
}
