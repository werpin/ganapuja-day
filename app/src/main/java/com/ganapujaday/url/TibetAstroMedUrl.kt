package com.ganapujaday.url

import java.time.LocalDate

/**
 * Формирует URL описания дня на tibetastromed.ru.
 *
 * Формат даты: M/d/yyyy (месяц/день/год), подтверждён вручную 2026-10-16:
 * https://tibetastromed.ru/docom.php?tdat=9/13/2026&tipv=0&type=new&lang=ru
 *
 * Все компоненты URL формируются из Int-полей LocalDate, без строковых
 * параметров из внешних источников — защита от SSRF-подобных инъекций
 * через deep links / sharing в будущем (см. ADR-004).
 */
object TibetAstroMedUrl {

    private const val BASE = "https://tibetastromed.ru/docom.php"
    private const val QUERY_SUFFIX = "&tipv=0&type=new&lang=ru"

    private val MIN_YEAR = 1900
    private val MAX_YEAR = 2100

    /**
     * @throws IllegalArgumentException если год вне диапазона [MIN_YEAR, MAX_YEAR].
     *         Диапазон соответствует ограничениям tyme4kt RabByungDay
     *         (1951-01-08 … 2051-02-11), расширен с запасом.
     */
    fun from(date: LocalDate): String {
        val year = date.year
        require(year in MIN_YEAR..MAX_YEAR) {
            "Year $year out of supported range $MIN_YEAR..$MAX_YEAR"
        }

        // M/d/yyyy — порядок критичен, подтверждён на живом сайте
        val tdat = "${date.monthValue}/${date.dayOfMonth}/$year"

        return "$BASE?tdat=$tdat$QUERY_SUFFIX"
    }
}