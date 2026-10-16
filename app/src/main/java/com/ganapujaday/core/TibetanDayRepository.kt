package com.ganapujaday.core

import java.time.LocalDate

interface TibetanDayRepository {
    /**
     * Возвращает тибетский день для [date].
     * Не бросает исключений при корректной дате в диапазоне 1900..2100.
     */
    suspend fun get(date: LocalDate): TibetanDay
}