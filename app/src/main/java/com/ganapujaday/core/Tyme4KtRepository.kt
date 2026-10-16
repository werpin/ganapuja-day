package com.ganapujaday.core

import android.util.Log
import com.tyme.rabbyung.RabByungDay
import com.tyme.solar.SolarDay
import java.time.LocalDate

private const val TAG = "Tyme4KtRepository"

/**
 * Реализация на tyme4kt.
 *
 * API RabByungDay (из tyme4ts v1.5.0):
 * -
 * - отрицательные month/day = високосные
 * - getName() — строковое представление
 */
class Tyme4KtRepository : TibetanDayRepository {

    override suspend fun get(date: LocalDate): TibetanDay {
        return try {
            val solar = SolarDay.fromYmd(date.year, date.monthValue, date.dayOfMonth)
            val rab = solar.getRabByungDay()

            val rawMonth = rab.month
            val rawDay = rab.day

            TibetanDay(
                solarDate = date,
                year = rab.year,
                month = kotlin.math.abs(rawMonth),
                day = kotlin.math.abs(rawDay),
                isLeapMonth = rawMonth < 0,
                isLeapDay = rawDay < 0,
                raw = rab.toString()
            )
        } catch (e: Exception) {
            Log.e(TAG, "tyme4kt failed for $date", e)
            throw TibetanCalendarException("Cannot compute Tibetan day for $date", e)
        }
    }
}

class TibetanCalendarException(message: String, cause: Throwable? = null) : Exception(message, cause)