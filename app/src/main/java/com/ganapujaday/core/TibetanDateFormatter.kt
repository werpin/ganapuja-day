package com.ganapujaday.core

/**
 * Форматирует тибетскую дату в человекочитаемый вид для ru-локали.
 *
 * Источник данных — raw-строка RabByungDay вида "第十七饶迥火马年正月初一".
 * Числовые поля (month, day) из RabByungDay используются как основа,
 * а цикл/элемент/животное извлекаются парсингом raw — API tyme4kt
 * не экспонирует их отдельными геттерами (см. ADR-009).
 *
 * Согласование рода не делается: "Дерево Дракон", "Огонь Лошадь" —
 * приемлемый компромисс для MVP. [DEBT-low] — вынести в strings.xml
 * с полным согласованием, если понадобится.
 */
object TibetanDateFormatter {

    private val RABJUNG_REGEX = Regex("""第(.+?)饶迥(.+?)年(.+?)月(.+)$""")

    private val ELEMENTS: Map<Char, String> = mapOf(
        '木' to "Дерево",
        '火' to "Огонь",
        '土' to "Земля",
        '金' to "Металл",
        '水' to "Вода",
    )

    private val ANIMALS: Map<Char, String> = mapOf(
        '鼠' to "Крыса",
        '牛' to "Бык",
        '虎' to "Тигр",
        '兔' to "Кролик",
        '龙' to "Дракон",
        '蛇' to "Змея",
        '马' to "Лошадь",
        '羊' to "Овца",
        '猴' to "Обезьяна",
        '鸡' to "Петух",
        '狗' to "Собака",
        '猪' to "Свинья",
    )

    data class Parts(
        val rabjung: Int?,
        val element: String?,
        val animal: String?,
    )

    /**
     * Извлекает цикл/элемент/животное из raw-строки.
     * Возвращает null-поля, если raw не распознан — UI покажет fallback.
     */
    fun parseRaw(raw: String): Parts {
        val match = RABJUNG_REGEX.find(raw) ?: return Parts(null, null, null)
        val rabjungStr = match.groupValues[1]
        val elementAnimalStr = match.groupValues[2]

        val rabjung = chineseNumberToInt(rabjungStr)
        val element = elementAnimalStr.firstOrNull()?.let { ELEMENTS[it] }
        val animal = elementAnimalStr.getOrNull(1)?.let { ANIMALS[it] }

        return Parts(rabjung, element, animal)
    }

    /**
     * Форматирует как "17-й цикл, Огонь Лошадь, 1-й месяц, 1-й день".
     * Fallback — числовой формат без цикла, если raw не распознан.
     */
    fun format(day: TibetanDay): String {
        val parts = parseRaw(day.raw)
        val monthStr = "${day.month}-й месяц"
        val dayStr = "${day.day}-й день"

        if (parts.rabjung != null && parts.element != null && parts.animal != null) {
            return "${parts.rabjung}-й цикл, ${parts.element} ${parts.animal}, $monthStr, $dayStr"
        }

        return "$monthStr, $dayStr"
    }

    /**
     * Китайские числительные для циклов Rabjung (1..99).
     * Форматы: 一=1, 十=10, 十七=17, 二十=20, 廿一=21 (не используется в rabjung).
     */
    private fun chineseNumberToInt(s: String): Int? {
        if (s.isEmpty()) return null

        val digits: Map<Char, Int> = mapOf(
            '一' to 1, '二' to 2, '三' to 3, '四' to 4, '五' to 5,
            '六' to 6, '七' to 7, '八' to 8, '九' to 9,
        )
        val ten = '十'

        return when (s.length) {
            1 -> if (s[0] == ten) 10 else digits[s[0]]
            2 -> when {
                s[0] == ten -> 10 + (digits[s[1]] ?: return null)          // 十七 → 17
                s[1] == ten -> (digits[s[0]] ?: return null) * 10          // 二十 → 20
                else -> null
            }
            3 -> when {
                s[1] == ten -> {
                    val hundreds = digits[s[0]] ?: return null
                    val units = digits[s[2]] ?: return null
                    hundreds * 10 + units                                   // 二十五 → 25
                }
                else -> null
            }
            else -> null
        }
    }
}