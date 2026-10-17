package com.ganapujaday.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Кнопка в TopAppBar: иконка яркости → выпадающее меню с выбором темы.
 *
 * Текущий режим подсвечивается галочкой в меню.
 */
@Composable
fun ThemeMenuButton() {
    val controller = LocalThemeController.current
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }) {
        Icon(
            imageVector = Icons.Filled.Brightness6,
            contentDescription = "Тема",
            modifier = Modifier.size(24.dp),
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
        ThemeMenuItem("Системная", ThemeMode.SYSTEM, controller.mode) {
            controller.setMode(ThemeMode.SYSTEM)
            expanded = false
        }
        ThemeMenuItem("Светлая", ThemeMode.LIGHT, controller.mode) {
            controller.setMode(ThemeMode.LIGHT)
            expanded = false
        }
        ThemeMenuItem("Тёмная", ThemeMode.DARK, controller.mode) {
            controller.setMode(ThemeMode.DARK)
            expanded = false
        }
    }
}

@Composable
private fun ThemeMenuItem(
    label: String,
    target: ThemeMode,
    current: ThemeMode,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(if (target == current) "✓ $label" else "   $label")
        },
        onClick = onClick,
    )
}