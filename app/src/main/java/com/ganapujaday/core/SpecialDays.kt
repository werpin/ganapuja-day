package com.ganapujaday.core

import java.time.LocalDate

/**
 * Логика особых дней практики (Ганапуджа / Цог).
 *
 * Особые дни — 10, 15, 25, 30 лунные дни. Поиск ближайшего дня
 * идёт вперёд по григорианскому календарю, потому что лунный
 * месяц не совпадает с григорианским и «через 5 дней» может
 * дать другой лунный день.
 *
 * Ограничение поиска: 60 дней вперёд. Этого достаточно, потому
 * что между двумя особыми днями не больше 15 лунных дней,
 * а лунный день длится ~1 солнечный.
 */
object SpecialDays {

    const val MAX_LOOKAHEAD_DAYS = 60

    /**
     * Возвращает ближайший особый день строго ПОСЛЕ [from].
     * Если не найден за [MAX_LOOKAHEAD_DAYS] — null.
     */
    suspend fun findNext(
        from: LocalDate,
        repository: TibetanDayRepository,
    ): TibetanDay? {
        var cursor = from.plusDays(1)
        repeat(MAX_LOOKAHEAD_DAYS) {
            val day = repository.get(cursor)
            if (day.isSpecial) return day
            cursor = cursor.plusDays(1)
        }
        return null
    }
}