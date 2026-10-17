package com.ganapujaday.ui

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Хранилище темы в SharedPreferences.
 *
 * DataStore был бы правильнее (асинхронный, thread-safe, поддерживает Flow),
 * но требует ещё одну зависимость ради одного enum. Для MVP достаточно
 * SharedPreferences — операция синхронная, применяется только при смене темы.
 *
 * [DEBT-low]: перейти на DataStore при первой же реконфигурации хранилища.
 */
class ThemePreference(context: Context) {
    private val prefs = context.getSharedPreferences("theme", Context.MODE_PRIVATE)

    var mode: ThemeMode
        get() = runCatching {
            ThemeMode.valueOf(prefs.getString("mode", ThemeMode.SYSTEM.name)!!)
        }.getOrDefault(ThemeMode.SYSTEM)
        set(value) = prefs.edit().putString("mode", value.name).apply()
}

/**
 * Текущий режим темы и setter — доступны любому @Composable через CompositionLocal.
 *
 * Заменяет временный ThemeSetterHolder. CompositionLocal — идиоматичный
 * Compose-способ шаринга состояния между экранами без проброса параметров.
 */
data class ThemeController(
    val mode: ThemeMode,
    val setMode: (ThemeMode) -> Unit,
)

val LocalThemeController = staticCompositionLocalOf<ThemeController> {
    error("LocalThemeController not provided")
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

    val controller = remember(mode) {
        ThemeController(
            mode = mode,
            setMode = { newMode ->
                themePreference.mode = newMode
                mode = newMode
            },
        )
    }

    CompositionLocalProvider(LocalThemeController provides controller) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}