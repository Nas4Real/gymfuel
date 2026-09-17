package com.gymfuel.app.core.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

data class DailyNutritionSummary(val date: LocalDate, val nutrition: DailyNutrition)

data class WeeklyNutrition(
    val days: List<DailyNutritionSummary>,
    val averageConsumed: NutritionTotals,
) {
    companion object {
        fun from(entries: List<FoodEntry>, endDate: LocalDate): WeeklyNutrition {
            val dates = (6L downTo 0L).map(endDate::minusDays)
            val days = dates.map { date -> DailyNutritionSummary(date, DailyNutrition.from(entries.filter { it.localDate == date })) }
            val total = days.fold(NutritionTotals.Zero) { acc, day -> acc + day.nutrition.consumed }
            fun average(value: BigDecimal) = value.divide(BigDecimal(days.size), 2, RoundingMode.HALF_UP)
            return WeeklyNutrition(
                days,
                NutritionTotals(average(total.calories), average(total.proteinGrams), average(total.carbohydrateGrams), average(total.fatGrams)),
            )
        }
    }
}
