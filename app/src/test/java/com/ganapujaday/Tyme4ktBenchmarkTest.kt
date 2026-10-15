package com.ganapujaday

import com.tyme.solar.SolarDay
import org.junit.Test
import kotlin.system.measureNanoTime

class Tyme4ktBenchmarkTest {

    private val sampleDates = listOf(
        Triple(2026, 10, 16),  // обычная
        Triple(2024, 2, 29),   // високосный
        Triple(2026, 1, 1),    // начало года
        Triple(2026, 12, 31),  // конец года
        Triple(2026, 2, 18),   // Losar 2026
        Triple(2026, 3, 15),   // около вставного месяца
        Triple(2025, 6, 10),
        Triple(2027, 9, 25),
    )

    @Test
    fun benchmarkTibetanDayCalculation() {
        // Warm-up (JIT)
        repeat(20) {
            SolarDay.fromYmd(2026, 10, 16).getRabByungDay()
        }

        val timingsNs = mutableListOf<Long>()
        repeat(100) { iteration ->
            val (y, m, d) = sampleDates[iteration % sampleDates.size]
            val ns = measureNanoTime {
                SolarDay.fromYmd(y, m, d).getRabByungDay()
            }
            timingsNs.add(ns)
        }

        val sorted = timingsNs.sorted()
        val p50 = sorted[sorted.size / 2] / 1_000_000.0
        val p95 = sorted[(sorted.size * 95) / 100] / 1_000_000.0
        val max = sorted.last() / 1_000_000.0
        val avg = sorted.average() / 1_000_000.0

        println("=== tyme4kt benchmark (100 calls) ===")
        println("avg:  %.3f ms".format(avg))
        println("p50:  %.3f ms".format(p50))
        println("p95:  %.3f ms".format(p95))
        println("max:  %.3f ms".format(max))
    }

    @Test
    fun printTibetanDatesForCrossCheck() {
        val datesToCheck = listOf(
            Triple(2026, 2, 17),   // канун Losar
            Triple(2026, 2, 18),   // Losar 2026 (ожидается 1-й день 1-го месяца)
            Triple(2026, 2, 19),   // день после Losar
            Triple(2026, 3, 15),   // около вставного месяца
            Triple(2024, 2, 29),   // високосный
            Triple(2026, 1, 1),    // начало года
            Triple(2026, 12, 31),  // конец года
            Triple(2027, 1, 1),    // начало следующего года
            Triple(2025, 6, 10),   // середина года
            Triple(2026, 6, 15),   // проверка skipped/duplicate day
        )

        println("=== Tibetan dates for cross-check with Lotsawa House ===")
        for ((y, m, d) in datesToCheck) {
            val solarDay = SolarDay.fromYmd(y, m, d)
            val rabByungDay = solarDay.getRabByungDay()
            println("%04d-%02d-%02d → %s".format(y, m, d, rabByungDay.toString()))
        }
    }
}