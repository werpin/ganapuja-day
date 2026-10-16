package com.ganapujaday.ui

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Тема приложения с переключением светлая/тёмная/системная.
 * Хранится в SharedPreferences — DataStore был бы правильнее, но требует
 * ещё одну зависимость ради одного enum. [DEBT-low]: перейти на DataStore
 * при первой же реконфигурации хранилища.
 */
class ThemePreference(context: Context) {
    private val prefs = context.getSharedPreferences("theme", Context.MODE_PRIVATE)

    var mode: ThemeMode
        get() = ThemeMode.valueOf(prefs.getString("mode", ThemeMode.SYSTEM.name)!!)
        set(value) = prefs.edit().putString("mode", value.name).apply()
}

@Composable
fun GanapujaDayTheme(
    themePreference: ThemePreference,
    content: @Composable () -> Unit,
) {
    var mode by remember { mutableStateOf(themePreference.mode) }

    val darkTheme = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()

    MaterialTheme(colorScheme = colorScheme) {
        content()
        // Передаём setter вниз через CompositionLocal или параметр, если нужен переключатель в UI
        ThemeSetterHolder.setter = { newMode ->
            themePreference.mode = newMode
            mode = newMode
        }
    }
}

/** Хак для MVP: без CompositionLocal. Позже вынести в CompositionLocalProvider. */
object ThemeSetterHolder {
    var setter: ((ThemeMode) -> Unit)? = null
}