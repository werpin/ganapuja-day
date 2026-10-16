package com.ganapujaday.core

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate

class SpecialDaysTest {

    private class FakeRepo(private val special: Set<LocalDate>) : TibetanDayRepository {
        override suspend fun get(date: LocalDate): TibetanDay {
            val day = if (date in special) 10 else 1
            return TibetanDay(date, date.year, 1, day, false, false, "")
        }
    }

    @Test
    fun `finds next special day`() = runTest {
        val from = LocalDate.of(2026, 10, 16)
        val special = setOf(LocalDate.of(2026, 10, 20))
        val repo = FakeRepo(special)

        val result = SpecialDays.findNext(from, repo)
        assertNotNull(result)
        assertEquals(LocalDate.of(2026, 10, 20), result!!.solarDate)
        assertEquals(10, result.day)
    }

    @Test
    fun `returns null when no special day within lookahead`() = runTest {
        val from = LocalDate.of(2026, 10, 16)
        val repo = FakeRepo(emptySet())

        val result = SpecialDays.findNext(from, repo)
        assertEquals(null, result)
    }

    @Test
    fun `finds special day on day plus one`() = runTest {
        val from = LocalDate.of(2026, 10, 16)
        val repo = FakeRepo(setOf(LocalDate.of(2026, 10, 17)))

        val result = SpecialDays.findNext(from, repo)
        assertEquals(LocalDate.of(2026, 10, 17), result?.solarDate)
    }

    @Test
    fun `does not return today even if today is special`() = runTest {
        val from = LocalDate.of(2026, 10, 16)
        val repo = FakeRepo(setOf(from))

        val result = SpecialDays.findNext(from, repo)
        // сегодня не считаем — «ближайший после»
        assertEquals(null, result)
    }
}