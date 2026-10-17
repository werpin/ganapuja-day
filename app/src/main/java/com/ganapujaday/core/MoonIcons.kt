package com.ganapujaday.core

import androidx.annotation.DrawableRes
import com.ganapujaday.R

/**
 * Выбор иконки фазы луны по тибетскому лунному дню.
 */
@DrawableRes
fun moonIconFor(day: Int?): Int? = when (day) {
    null -> null
    1, 30 -> R.drawable.ic_moon_new
    in 2..14 -> R.drawable.ic_moon_waxing
    15 -> R.drawable.ic_moon_full
    in 16..29 -> R.drawable.ic_moon_waning
    else -> null
}