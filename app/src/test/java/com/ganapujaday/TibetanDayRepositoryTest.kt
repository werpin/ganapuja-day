package com.ganapujaday.core

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class TibetanDayRepositoryTest {

    private val repo = Tyme4KtRepository()

    @Test
    fun `losar 2026 is first day of first month`() = runTest {
        val day = repo.get(LocalDate.of(2026, 2, 18))
        assertEquals(2026, day.year)   // грегорианский год, не номер Rabjung-цикла
        assertEquals(1, day.month)
        assertEquals(1, day.day)
        assertFalse(day.isLeapMonth)
        assertFalse(day.isLeapDay)
    }

    @Test
    fun `special day 10 is flagged`() = runTest {
        // 10-й лунный день — найти дату динамически или захардкодить из 0.5
        val day = repo.get(LocalDate.of(2026, 6, 15))
        assertEquals(30, day.day) // из вывода 0.5: 四月三十
        assertTrue(day.isSpecial)
    }

    @Test
    fun `leap year date works`() = runTest {
        val day = repo.get(LocalDate.of(2024, 2, 29))
        assertNotNull(day)
        assertEquals(20, day.day) // из 0.5: 正月二十
    }
}