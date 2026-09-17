package com.gymfuel.app.core.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

enum class FormulaSex { Male, Female }

data class TargetProfile(
    val ageYears: Int,
    val sex: FormulaSex,
    val heightCentimeters: BigDecimal,
    val weightKilograms: BigDecimal,
    val activityMultiplier: BigDecimal,
    val surplusCalories: BigDecimal,
) {
    init {
        require(ageYears in 13..100) { "Age must be between 13 and 100" }
        require(heightCentimeters in BigDecimal("100")..BigDecimal("250")) { "Height is outside the supported range" }
        require(weightKilograms in BigDecimal("30")..BigDecimal("350")) { "Weight is outside the supported range" }
        require(activityMultiplier in BigDecimal("1.2")..BigDecimal("2.5")) { "Activity multiplier is outside the supported range" }
        require(surplusCalories in BigDecimal.ZERO..BigDecimal("1000")) { "Surplus is outside the supported range" }
    }
}

data class NutritionTarget(
    val calories: BigDecimal,
    val proteinGrams: BigDecimal,
    val carbohydrateGrams: BigDecimal,
    val fatGrams: BigDecimal,
)

data class EffectiveNutritionTarget(
    val id: String,
    val effectiveFrom: LocalDate,
    val nutrition: NutritionTarget,
    val profile: TargetProfile? = null,
    val syncState: SyncState = SyncState.Pending,
)

object TargetCalculator {
    private val TEN = BigDecimal.TEN
    private val HEIGHT_FACTOR = BigDecimal("6.25")
    private val AGE_FACTOR = BigDecimal("5")
    private val MALE_OFFSET = BigDecimal("5")
    private val FEMALE_OFFSET = BigDecimal("-161")
    private val PROTEIN_PER_KG = BigDecimal("1.8")
    private val FAT_PER_KG = BigDecimal("0.8")
    private val PROTEIN_OR_CARB_KCAL = BigDecimal("4")
    private val FAT_KCAL = BigDecimal("9")

    fun calculate(profile: TargetProfile): NutritionTarget {
        val sexOffset = if (profile.sex == FormulaSex.Male) MALE_OFFSET else FEMALE_OFFSET
        val basalCalories = TEN * profile.weightKilograms + HEIGHT_FACTOR * profile.heightCentimeters -
            AGE_FACTOR * profile.ageYears.toBigDecimal() + sexOffset
        val calories = (basalCalories * profile.activityMultiplier + profile.surplusCalories)
            .setScale(0, RoundingMode.HALF_UP)
        val protein = (profile.weightKilograms * PROTEIN_PER_KG).setScale(1, RoundingMode.HALF_UP)
        val fat = (profile.weightKilograms * FAT_PER_KG).setScale(1, RoundingMode.HALF_UP)
        val remainingCalories = (calories - protein * PROTEIN_OR_CARB_KCAL - fat * FAT_KCAL).max(BigDecimal.ZERO)
        val carbohydrates = remainingCalories.divide(PROTEIN_OR_CARB_KCAL, 1, RoundingMode.HALF_UP)
        return NutritionTarget(calories, protein, carbohydrates, fat)
    }
}
