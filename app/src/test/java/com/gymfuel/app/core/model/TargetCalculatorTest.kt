package com.gymfuel.app.core.model

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class TargetCalculatorTest {
    @Test
    fun muscleGainTarget_usesMifflinActivityAndTransparentSurplus() {
        val target = TargetCalculator.calculate(
            TargetProfile(
                ageYears = 30,
                sex = FormulaSex.Male,
                heightCentimeters = BigDecimal("180"),
                weightKilograms = BigDecimal("80"),
                activityMultiplier = BigDecimal("1.55"),
                surplusCalories = BigDecimal("250"),
            ),
        )

        assertEquals(BigDecimal("3009"), target.calories)
        assertEquals(BigDecimal("144.0"), target.proteinGrams)
        assertEquals(BigDecimal("464.3"), target.carbohydrateGrams)
        assertEquals(BigDecimal("64.0"), target.fatGrams)
    }

    @Test(expected = IllegalArgumentException::class)
    fun profile_rejectsImpossibleAge() {
        TargetProfile(5, FormulaSex.Female, BigDecimal("160"), BigDecimal("60"), BigDecimal("1.2"), BigDecimal("200"))
    }
}
