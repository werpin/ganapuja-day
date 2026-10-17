package com.ganapujaday.ui

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ganapujaday.R
import com.ganapujaday.core.TibetanDay
import com.ganapujaday.core.moonIconFor
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Today

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthScreen(
    onOpenDay: () -> Unit,
    onDayClick: (LocalDate) -> Unit,
    viewModel: MonthViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = onOpenDay) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = stringResource(R.string.open_day),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.prevMonth() }) {
                        Icon(
                            imageVector = Icons.Filled.ChevronLeft,
                            contentDescription = "Предыдущий месяц",
                        )
                    }
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(
                            imageVector = Icons.Filled.Today,
                            contentDescription = "Текущий месяц",
                        )
                    }
                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Следующий месяц",
                        )
                    }
                    ThemeMenuButton()
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val s = state) {
                is MonthUiState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
                is MonthUiState.Error -> Text(
                    text = s.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center),
                )
                is MonthUiState.Success -> CalendarGrid(
                    yearMonth = s.yearMonth,
                    days = s.days,
                    onDayClick = onDayClick,
                )
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    days: List<TibetanDay>,
    onDayClick: (LocalDate) -> Unit,
) {
    val offset = days.first().solarDate.dayOfWeek.value - 1
    val today = LocalDate.now()

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(4.dp),
    ) {
        item(span = { GridItemSpan(7) }) { MonthHeader(yearMonth) }
        item(span = { GridItemSpan(7) }) { WeekdayHeader() }
        items(offset) { Box(modifier = Modifier.aspectRatio(0.85f)) }
        items(days) { day ->
            DayCell(
                day = day,
                isToday = day.solarDate == today,
                onClick = { onDayClick(day.solarDate) },
            )
        }
    }
}

@Composable
private fun MonthHeader(yearMonth: YearMonth) {
    val title = yearMonth.month
        .getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("ru"))
        .replaceFirstChar { it.uppercase() } + " ${yearMonth.year}"
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun WeekdayHeader() {
    val days = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    Row(modifier = Modifier.fillMaxWidth()) {
        days.forEach { name ->
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DayCell(day: TibetanDay, isToday: Boolean, onClick: () -> Unit) {
    val bgColor = if (day.isSpecial) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        Color.Transparent
    }

    val border = if (isToday) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        null
    }

    // Цвет иконки берем из текущей темы (onSurfaceVariant, чтобы не сливаться с текстом)
    val iconColor = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .aspectRatio(0.85f)
            .padding(2.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = border,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = day.solarDate.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.width(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = moonIconFor(day.day)
                if (icon != null) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = iconColor,
                    )
                }
                Spacer(Modifier.width(2.dp))
                Text(
                    text = day.day.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}