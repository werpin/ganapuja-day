package com.ganapujaday.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganapujaday.cache.TibetanDayCache
import com.ganapujaday.core.TibetanDay
import com.ganapujaday.core.Tyme4KtRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth

sealed interface MonthUiState {
    data object Loading : MonthUiState
    data class Success(val yearMonth: YearMonth, val days: List<TibetanDay>) : MonthUiState
    data class Error(val message: String) : MonthUiState
}

class MonthViewModel(
    private val cache: TibetanDayCache = TibetanDayCache(Tyme4KtRepository()),
) : ViewModel() {

    private val _state = MutableStateFlow<MonthUiState>(MonthUiState.Loading)
    val state: StateFlow<MonthUiState> = _state.asStateFlow()

    /**
     * Загружает дни указанного месяца. Сохраняет yearMonth в Success,
     * чтобы UI знал, какой месяц отрисован (для стрелок навигации).
     */
    fun load(yearMonth: YearMonth = YearMonth.now()) {
        viewModelScope.launch {
            _state.value = MonthUiState.Loading
            _state.value = try {
                val first = yearMonth.atDay(1)
                val last = yearMonth.atEndOfMonth()
                val days = mutableListOf<TibetanDay>()
                var cursor = first
                while (!cursor.isAfter(last)) {
                    days += cache.get(cursor)
                    cursor = cursor.plusDays(1)
                }
                MonthUiState.Success(yearMonth, days)
            } catch (e: Exception) {
                MonthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun nextMonth() {
        val current = (state.value as? MonthUiState.Success)?.yearMonth ?: YearMonth.now()
        load(current.plusMonths(1))
    }

    fun prevMonth() {
        val current = (state.value as? MonthUiState.Success)?.yearMonth ?: YearMonth.now()
        load(current.minusMonths(1))
    }
}