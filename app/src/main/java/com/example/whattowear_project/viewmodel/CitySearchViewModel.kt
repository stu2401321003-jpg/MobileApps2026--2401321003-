package com.example.whattowear_project.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whattowear_project.data.repository.WeatherRepository
import com.example.whattowear_project.weather.model.City
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Какво показва екранът за търсене. */
sealed interface CitySearchState {
    /** Още не е написано достатъчно. */
    data object Idle : CitySearchState
    data object Loading : CitySearchState
    data class Results(val cities: List<City>) : CitySearchState
    data object NothingFound : CitySearchState
    data object Error : CitySearchState
}

@HiltViewModel
class CitySearchViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")

    /**
     * debounce(400) = търсим чак когато потребителят спре да пише за 400 ms,
     * а не при всяка буква. flatMapLatest отказва старата заявка, ако дойде нова.
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val state: StateFlow<CitySearchState> = query
        .map { it.trim() }
        .distinctUntilChanged()
        .debounce(DEBOUNCE_MS)
        .flatMapLatest { text -> search(text) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CitySearchState.Idle)

    private fun search(text: String) = flow {
        if (text.length < MIN_QUERY_LENGTH) {
            emit(CitySearchState.Idle)
            return@flow
        }
        emit(CitySearchState.Loading)
        try {
            val cities = repository.searchCities(text)
            emit(if (cities.isEmpty()) CitySearchState.NothingFound else CitySearchState.Results(cities))
        } catch (e: CancellationException) {
            throw e // новата заявка отменя старата - това не е грешка
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            emit(CitySearchState.Error) // няма интернет, сървърът не отговаря...
        }
    }

    fun onQueryChanged(text: String) {
        savedStateHandle[KEY_QUERY] = text
    }

    fun selectCity(city: City) = repository.selectCity(city)

    private companion object {
        const val KEY_QUERY = "query"
        const val DEBOUNCE_MS = 400L
        const val MIN_QUERY_LENGTH = 2
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
