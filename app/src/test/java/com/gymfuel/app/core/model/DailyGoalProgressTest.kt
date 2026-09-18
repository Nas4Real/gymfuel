package com.gymfuel.app.core.model

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyGoalProgressTest {
    @Test
    fun progress_reportsEveryReachedGoalAndReadableSummary() {
        val progress = DailyGoalProgress.from(
            consumed = NutritionTotals(
                calories = BigDecimal("2500"),
                proteinGrams = BigDecimal("150"),
                carbohydrateGrams = BigDecimal("280"),
                fatGrams = BigDecimal("60"),
            ),
            waterLiters = BigDecimal("2.4"),
            target = NutritionTarget(
                calories = BigDecimal("2400"),
                proteinGrams = BigDecimal("160"),
                carbohydrateGrams = BigDecimal("280"),
                fatGrams = BigDecimal("70"),
                waterLiters = BigDecimal("2.4"),
            ),
        )

        assertTrue(progress.caloriesReached)
        assertFalse(progress.proteinReached)
        assertTrue(progress.carbohydratesReached)
        assertFalse(progress.fatReached)
        assertTrue(progress.waterReached)
        assertEquals(3, progress.reachedCount)
        assertEquals("3 of 5 goals reached", progress.summary)
    }
}
