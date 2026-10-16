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
import java.time.LocalDate

sealed interface DayUiState {
    data object Loading : DayUiState
    data class Success(val date: LocalDate, val day: TibetanDay) : DayUiState
    data class Error(val message: String) : DayUiState
}

class DayViewModel(
    private val cache: TibetanDayCache = TibetanDayCache(Tyme4KtRepository())
) : ViewModel() {

    private val _state = MutableStateFlow<DayUiState>(DayUiState.Loading)
    val state: StateFlow<DayUiState> = _state.asStateFlow()

    fun load(date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            _state.value = DayUiState.Loading
            _state.value = try {
                DayUiState.Success(date, cache.get(date))
            } catch (e: Exception) {
                DayUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}