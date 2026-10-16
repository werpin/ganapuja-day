package com.ganapujaday.core

import java.time.LocalDate

/**
 * Тибетский лунный день (традиция Phugpa).
 *
 * Поля [year], [month], [day] — числовые значения из RabByungDay.
 * Признак високосности выводится из знака: отрицательные значения
 * означают високосный месяц/день (см. API tyme4kt).
 */
data class TibetanDay(
    val solarDate: LocalDate,
    val year: Int,
    val month: Int,
    val day: Int,
    val isLeapMonth: Boolean,
    val isLeapDay: Boolean,
    val raw: String
) {
    /** Особый день практики: 10, 15, 25, 30. */
    val isSpecial: Boolean
        get() = day in SPECIAL_DAYS

    companion object {
        val SPECIAL_DAYS = setOf(10, 15, 25, 30)
    }
}