package com.ganapujaday.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class TibetanDateFormatterTest {

    private fun day(raw: String, month: Int, day: Int) = TibetanDay(
        solarDate = LocalDate.of(2026, 2, 18),
        year = 2026,
        month = month,
        day = day,
        isLeapMonth = false,
        isLeapDay = false,
        raw = raw,
    )

    @Test
    fun `losar 2026 fire horse`() {
        val d = day("第十七饶迥火马年正月初一", month = 1, day = 1)
        assertEquals("17-й цикл, Огонь Лошадь, 1-й месяц, 1-й день", TibetanDateFormatter.format(d))
    }

    @Test
    fun `wood snake 12th month`() {
        val d = day("第十七饶迥木蛇年十二月三十", month = 12, day = 30)
        assertEquals("17-й цикл, Дерево Змея, 12-й месяц, 30-й день", TibetanDateFormatter.format(d))
    }

    @Test
    fun `wood dragon 1st month day 20`() {
        val d = day("第十七饶迥木龙年正月二十", month = 1, day = 20)
        assertEquals("17-й цикл, Дерево Дракон, 1-й месяц, 20-й день", TibetanDateFormatter.format(d))
    }

    @Test
    fun `parseRaw extracts parts`() {
        val parts = TibetanDateFormatter.parseRaw("第十七饶迥火马年正月初一")
        assertEquals(17, parts.rabjung)
        assertEquals("Огонь", parts.element)
        assertEquals("Лошадь", parts.animal)
    }

    @Test
    fun `malformed raw returns nulls`() {
        val parts = TibetanDateFormatter.parseRaw("garbage")
        assertNull(parts.rabjung)
        assertNull(parts.element)
        assertNull(parts.animal)
    }

    @Test
    fun `malformed raw falls back to numeric format`() {
        val d = day("garbage", month = 5, day = 12)
        assertEquals("5-й месяц, 12-й день", TibetanDateFormatter.format(d))
    }
}