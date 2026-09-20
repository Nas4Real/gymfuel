package com.gymfuel.app.core.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryDatesTest {
    @Test
    fun currentMonthThrough_startsOnFirstAndEndsToday() {
        val today = LocalDate.of(2026, 9, 19)

        val dates = HistoryDates.currentMonthThrough(today)

        assertEquals(19, dates.size)
        assertEquals(LocalDate.of(2026, 9, 1), dates.first())
        assertEquals(today, dates.last())
    }
}
