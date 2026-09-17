package com.gymfuel.app.core.model

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class NutritionTest {
    @Test
    fun `nutrition per 100 grams scales to eaten quantity`() {
        val chicken = NutritionPer100g(
            calories = BigDecimal("165"),
            proteinGrams = BigDecimal("31"),
            carbohydrateGrams = BigDecimal.ZERO,
            fatGrams = BigDecimal("3.6"),
        )

        val portion = chicken.forQuantity(BigDecimal("150"))

        assertEquals(BigDecimal("247.50"), portion.calories)
        assertEquals(BigDecimal("46.50"), portion.proteinGrams)
        assertEquals(BigDecimal("0.00"), portion.carbohydrateGrams)
        assertEquals(BigDecimal("5.40"), portion.fatGrams)
    }

    @Test
    fun `nutrition rejects negative values`() {
        assertThrows(IllegalArgumentException::class.java) {
            NutritionPer100g(
                calories = BigDecimal("-1"),
                proteinGrams = BigDecimal.ZERO,
                carbohydrateGrams = BigDecimal.ZERO,
                fatGrams = BigDecimal.ZERO,
            )
        }
    }

    @Test
    fun `quantity must be positive`() {
        val oats = NutritionPer100g(
            calories = BigDecimal("389"),
            proteinGrams = BigDecimal("16.9"),
            carbohydrateGrams = BigDecimal("66.3"),
            fatGrams = BigDecimal("6.9"),
        )

        assertThrows(IllegalArgumentException::class.java) {
            oats.forQuantity(BigDecimal.ZERO)
        }
    }

    @Test
    fun `nutrition rejects values outside database limits`() {
        assertThrows(IllegalArgumentException::class.java) {
            NutritionPer100g(
                calories = BigDecimal("1000.001"),
                proteinGrams = BigDecimal.ZERO,
                carbohydrateGrams = BigDecimal.ZERO,
                fatGrams = BigDecimal.ZERO,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            NutritionPer100g(
                calories = BigDecimal.ZERO,
                proteinGrams = BigDecimal("100.001"),
                carbohydrateGrams = BigDecimal.ZERO,
                fatGrams = BigDecimal.ZERO,
            )
        }
    }

    @Test
    fun `portion rejects quantities outside database limits`() {
        val chicken = NutritionPer100g(
            calories = BigDecimal("165"),
            proteinGrams = BigDecimal("31"),
            carbohydrateGrams = BigDecimal.ZERO,
            fatGrams = BigDecimal("3.6"),
        )

        assertThrows(IllegalArgumentException::class.java) {
            chicken.forQuantity(BigDecimal("100000.001"))
        }
    }
}
