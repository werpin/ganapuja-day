package com.ganapujaday.url

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TibetAstroMedUrlTest {

    @Test
    fun `matches confirmed live example`() {
        // Проверено вручную: 13 сентября 2026 → tdat=9/13/2026
        val url = TibetAstroMedUrl.from(LocalDate.of(2026, 9, 13))
        assertEquals(
            "https://tibetastromed.ru/docom.php?tdat=9/13/2026&tipv=0&type=new&lang=ru",
            url
        )
    }

    @Test
    fun `single-digit month and day`() {
        val url = TibetAstroMedUrl.from(LocalDate.of(2026, 2, 8))
        assertTrue(url.contains("tdat=2/8/2026"))
    }

    @Test
    fun `double-digit month and day`() {
        val url = TibetAstroMedUrl.from(LocalDate.of(2026, 12, 25))
        assertTrue(url.contains("tdat=12/25/2026"))
    }

    @Test
    fun `losar 2026 formats correctly`() {
        val url = TibetAstroMedUrl.from(LocalDate.of(2026, 2, 18))
        assertTrue(url.contains("tdat=2/18/2026"))
    }

    @Test
    fun `lower boundary 1900-01-01 accepted`() {
        val url = TibetAstroMedUrl.from(LocalDate.of(1900, 1, 1))
        assertTrue(url.contains("tdat=1/1/1900"))
    }

    @Test
    fun `upper boundary 2100-12-31 accepted`() {
        val url = TibetAstroMedUrl.from(LocalDate.of(2100, 12, 31))
        assertTrue(url.contains("tdat=12/31/2100"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `year below 1900 rejected`() {
        TibetAstroMedUrl.from(LocalDate.of(1899, 12, 31))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `year above 2100 rejected`() {
        TibetAstroMedUrl.from(LocalDate.of(2101, 1, 1))
    }

    @Test
    fun `always https and correct host`() {
        val url = TibetAstroMedUrl.from(LocalDate.of(2026, 10, 16))
        assertTrue(url.startsWith("https://tibetastromed.ru/docom.php?"))
    }

    @Test
    fun `query parameters are stable`() {
        val url = TibetAstroMedUrl.from(LocalDate.of(2026, 6, 15))
        assertTrue(url.endsWith("&tipv=0&type=new&lang=ru"))
    }
}