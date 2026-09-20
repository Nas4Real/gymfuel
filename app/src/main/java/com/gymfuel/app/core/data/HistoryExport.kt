package com.gymfuel.app.core.data

import com.gymfuel.app.core.model.FoodEntry
import java.math.RoundingMode
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class HistoryExportRange(val label: String) {
    Last7Days("Last 7 days"),
    CurrentMonth("Current month"),
    Last30Days("Last 30 days"),
    AllTime("All history"),
}

data class DateBounds(val start: LocalDate?, val end: LocalDate?)

object HistoryExport {
    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH)

    fun bounds(range: HistoryExportRange, today: LocalDate): DateBounds = when (range) {
        HistoryExportRange.Last7Days -> DateBounds(today.minusDays(6), today)
        HistoryExportRange.CurrentMonth -> DateBounds(today.withDayOfMonth(1), today)
        HistoryExportRange.Last30Days -> DateBounds(today.minusDays(29), today)
        HistoryExportRange.AllTime -> DateBounds(null, null)
    }

    fun toCsv(entries: List<FoodEntry>, zoneId: ZoneId = ZoneId.systemDefault()): String = buildString {
        appendLine("Date,Time,Food,Quantity (g),Status,Calories (kcal),Protein (g),Carbohydrates (g),Fat (g)")
        entries.sortedWith(compareBy<FoodEntry> { it.localDate }.thenBy { it.consumedAt }).forEach { entry ->
            val totals = entry.nutritionPer100gSnapshot.forQuantity(entry.quantityGrams)
            val localTimestamp = entry.consumedAt?.atZone(zoneId)?.format(timestampFormatter)
            append(
                listOf(
                    entry.localDate.toString(),
                    localTimestamp?.substringAfter(' ') ?: "",
                    entry.foodNameSnapshot,
                    entry.quantityGrams.display(),
                    entry.status.wireValue,
                    totals.calories.display(),
                    totals.proteinGrams.display(),
                    totals.carbohydrateGrams.display(),
                    totals.fatGrams.display(),
                ).joinToString(",", transform = ::escapeCsv),
            )
            appendLine()
        }
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return if (escaped.any { it == ',' || it == '\n' || it == '\r' || it == '\"' }) "\"$escaped\"" else escaped
    }

    private fun java.math.BigDecimal.display(): String = setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
}
