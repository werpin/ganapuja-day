package com.ganapujaday.widget

import androidx.annotation.DrawableRes
import com.ganapujaday.R

/**
 * Выбор иконки фазы луны по тибетскому лунному дню.
 *
 * Классификация Phugpa:
 * - 1, 30 — новолуние
 * - 2..14 — растущая
 * - 15    — полнолуние
 * - 16..29 — убывающая
 */
@DrawableRes
internal fun moonIconFor(day: Int?): Int? = when (day) {
    null -> null
    1, 30 -> R.drawable.ic_moon_new
    in 2..14 -> R.drawable.ic_moon_waxing
    15 -> R.drawable.ic_moon_full
    in 16..29 -> R.drawable.ic_moon_waning
    else -> null
}