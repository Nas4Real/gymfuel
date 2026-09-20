package com.gymfuel.app.core.model

import java.time.LocalDate

object HistoryDates {
    fun currentMonthThrough(today: LocalDate): List<LocalDate> =
        (1..today.dayOfMonth).map(today::withDayOfMonth)
}
