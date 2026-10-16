package com.ganapujaday.cache

import com.ganapujaday.core.TibetanDay
import com.ganapujaday.core.TibetanDayRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate

/**
 * Потокобезопасный LRU-кэш тибетских дней.
 *
 * Почему не android.util.LruCache:
 * - Android SDK не доступен в JVM unit-тестах (методы бросают RuntimeException).
 * - LinkedHashMap с accessOrder=true + removeEldestEntry даёт тот же LRU-эффект,
 *   но работает и в JVM, и на Android без mock-обвязки.
 *
 * Почему весь доступ через Mutex:
 * - LinkedHashMap не потокобезопасен.
 * - Быстрый путь без lock (cache[date] до mutex) создал бы гонку с put.
 * - Расчёт одной даты ~6 мс (см. README/Performance), поэтому стоимость
 *   lock-а пренебрежима на фоне риска race condition.
 *
 * Размер по умолчанию 32: месячный вид (30 дней) + текущий день + запас.
 */
class TibetanDayCache(
    private val repository: TibetanDayRepository,
    maxSize: Int = 32
) {
    private val maxSize: Int = maxSize
    private val mutex = Mutex()

    private val cache = object : LinkedHashMap<LocalDate, TibetanDay>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(
            eldest: MutableMap.MutableEntry<LocalDate, TibetanDay>?
        ): Boolean = size > this@TibetanDayCache.maxSize
    }

    suspend fun get(date: LocalDate): TibetanDay = mutex.withLock {
        cache[date]?.let { return@withLock it }

        val day = repository.get(date)
        cache[date] = day
        day
    }

    /** Полная очистка кэша. Нужна при смене часового пояса/даты или в тестах. */
    suspend fun clear() = mutex.withLock {
        cache.clear()
    }

    /** Текущий размер кэша. Для тестов и отладки. */
    suspend fun size(): Int = mutex.withLock { cache.size }
}