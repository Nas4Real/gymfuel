package com.gymfuel.app.core.data

import com.gymfuel.app.core.model.EntryStatus
import com.gymfuel.app.core.model.FoodEntry
import com.gymfuel.app.core.model.NutritionPer100g
import com.gymfuel.app.core.model.Preparation
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryExportTest {
    @Test
    fun rangesIncludeTodayAndUseExpectedInclusiveStart() {
        val today = LocalDate.of(2026, 9, 19)

        assertEquals(LocalDate.of(2026, 9, 13), HistoryExport.bounds(HistoryExportRange.Last7Days, today).start)
        assertEquals(LocalDate.of(2026, 9, 1), HistoryExport.bounds(HistoryExportRange.CurrentMonth, today).start)
        assertEquals(LocalDate.of(2026, 8, 21), HistoryExport.bounds(HistoryExportRange.Last30Days, today).start)
        assertEquals(DateBounds(null, null), HistoryExport.bounds(HistoryExportRange.AllTime, today))
    }

    @Test
    fun csvUsesEnglishHeadersEscapesNamesAndCalculatesSnapshotTotals() {
        val entry = FoodEntry(
            id = "entry-1",
            foodId = null,
            localDate = LocalDate.of(2026, 9, 19),
            quantityGrams = BigDecimal("150"),
            status = EntryStatus.Consumed,
            consumedAt = Instant.parse("2026-09-19T18:30:00Z"),
            foodNameSnapshot = "Chicken, grilled",
            preparationSnapshot = Preparation.Cooked,
            imageReferenceSnapshot = null,
            nutritionPer100gSnapshot = NutritionPer100g(
                BigDecimal("200"), BigDecimal("20"), BigDecimal("10"), BigDecimal("5"),
            ),
        )

        val csv = HistoryExport.toCsv(listOf(entry), ZoneOffset.UTC)

        assertTrue(csv.startsWith("Date,Time,Food,Quantity (g),Status"))
        assertTrue(csv.contains("2026-09-19,18:30,\"Chicken, grilled\",150,consumed,300,30,15,7.5"))
    }
}
