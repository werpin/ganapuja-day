package com.ganapujaday.cache

import com.ganapujaday.core.TibetanDay
import com.ganapujaday.core.TibetanDayRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class TibetanDayCacheTest {

    private class FakeRepo : TibetanDayRepository {
        var callCount = 0
        override suspend fun get(date: LocalDate): TibetanDay {
            callCount++
            return TibetanDay(date, 1, 1, 1, false, false, "fake")
        }
    }

    @Test
    fun `cache hit avoids second call`() = runTest {
        val repo = FakeRepo()
        val cache = TibetanDayCache(repo)
        val date = LocalDate.of(2026, 10, 16)

        cache.get(date)
        cache.get(date)

        assertEquals(1, repo.callCount)
    }

    @Test
    fun `cache evicts when over maxSize`() = runTest {
        val repo = FakeRepo()
        val cache = TibetanDayCache(repo, maxSize = 2)
        val d1 = LocalDate.of(2026, 10, 1)
        val d2 = LocalDate.of(2026, 10, 2)
        val d3 = LocalDate.of(2026, 10, 3)

        cache.get(d1)
        cache.get(d2)
        cache.get(d3) // вытеснит d1

        assertEquals(2, cache.size())
        cache.get(d1) // повторный вызов → ещё один вызов репозитория
        assertEquals(4, repo.callCount)
    }

    @Test
    fun `concurrent access is safe`() = runTest {
        val repo = FakeRepo()
        val cache = TibetanDayCache(repo)
        val date = LocalDate.of(2026, 10, 16)

        val results = (1..10).map { async { cache.get(date) } }.map { it.await() }
        results.forEach { assertNotNull(it) }

        // С Mutex-защитой весь доступ сериализован: ровно 1 вызов репозитория
        assertEquals(1, repo.callCount)
    }
}