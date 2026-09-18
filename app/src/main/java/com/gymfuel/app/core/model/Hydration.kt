package com.gymfuel.app.core.model

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

data class WaterEntry(
    val id: String,
    val localDate: LocalDate,
    val liters: BigDecimal,
    val loggedAt: Instant,
    val syncState: SyncState = SyncState.Pending,
) {
    init {
        require(liters.signum() > 0) { "Water amount must be positive" }
        require(liters <= BigDecimal("20")) { "Water amount cannot exceed 20 liters" }
    }
}

data class DailyGoalProgress(
    val caloriesReached: Boolean,
    val proteinReached: Boolean,
    val carbohydratesReached: Boolean,
    val fatReached: Boolean,
    val waterReached: Boolean,
) {
    val reachedCount: Int
        get() = listOf(caloriesReached, proteinReached, carbohydratesReached, fatReached, waterReached).count { it }

    val summary: String
        get() = when (reachedCount) {
            5 -> "All goals reached"
            0 -> "Goals in progress"
            else -> "$reachedCount of 5 goals reached"
        }

    companion object {
        fun from(consumed: NutritionTotals, waterLiters: BigDecimal, target: NutritionTarget?) =
            if (target == null) DailyGoalProgress(false, false, false, false, false)
            else DailyGoalProgress(
                caloriesReached = consumed.calories >= target.calories,
                proteinReached = consumed.proteinGrams >= target.proteinGrams,
                carbohydratesReached = consumed.carbohydrateGrams >= target.carbohydrateGrams,
                fatReached = consumed.fatGrams >= target.fatGrams,
                waterReached = waterLiters >= target.waterLiters,
            )
    }
}
