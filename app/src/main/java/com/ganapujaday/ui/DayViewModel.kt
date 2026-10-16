package com.ganapujaday.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganapujaday.cache.TibetanDayCache
import com.ganapujaday.core.SpecialDays
import com.ganapujaday.core.TibetanDay
import com.ganapujaday.core.TibetanDayRepository
import com.ganapujaday.core.Tyme4KtRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface DayUiState {
    data object Loading : DayUiState
    data class Success(
        val date: LocalDate,
        val day: TibetanDay,
        val nextSpecial: TibetanDay?,
    ) : DayUiState
    data class Error(val message: String) : DayUiState
}

class DayViewModel(
    private val cache: TibetanDayCache = TibetanDayCache(Tyme4KtRepository()),
) : ViewModel() {

    /**
     * Адаптер кэша под интерфейс TibetanDayRepository.
     * Нужен, потому что TibetanDayCache — не реализация интерфейса,
     * а обёртка над ним. Для SpecialDays.findNext семантика та же:
     * получить TibetanDay по дате.
     */
    private val repository: TibetanDayRepository = object : TibetanDayRepository {
        override suspend fun get(date: LocalDate): TibetanDay = cache.get(date)
    }

    private val _state = MutableStateFlow<DayUiState>(DayUiState.Loading)
    val state: StateFlow<DayUiState> = _state.asStateFlow()

    fun load(date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            _state.value = DayUiState.Loading
            _state.value = try {
                val day = cache.get(date)
                val nextSpecial = SpecialDays.findNext(date, repository)
                DayUiState.Success(date, day, nextSpecial)
            } catch (e: Exception) {
                DayUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}