package com.example.whattowear_project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whattowear_project.data.repository.ClothingRepository
import com.example.whattowear_project.data.repository.WeatherRepository
import com.example.whattowear_project.outfit.Outfit
import com.example.whattowear_project.outfit.OutfitRecommender
import com.example.whattowear_project.weather.model.City
import com.example.whattowear_project.weather.model.Weather
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** Четирите състояния на картата за времето. */
sealed interface WeatherUiState {
    data object NoCity : WeatherUiState
    data class Loading(val city: City) : WeatherUiState
    data class Success(val weather: Weather) : WeatherUiState
    data class Error(val city: City) : WeatherUiState
}

/** Секцията „Какво да облечеш“. */
sealed interface OutfitUiState {
    /** Още нямаме времето (няма град, зарежда или грешка). */
    data object WaitingForWeather : OutfitUiState

    /** Имаме времето, но гардеробът е празен. */
    data object EmptyWardrobe : OutfitUiState

    data class Ready(val outfit: Outfit, val weather: Weather) : OutfitUiState
}

/**
 * ViewModel за началния екран: времето в избрания град + препоръчан тоалет.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    clothingRepository: ClothingRepository,
) : ViewModel() {

    /** Брояч за „Обнови“ - всяка промяна пуска нова заявка. */
    private val refreshTrigger = MutableStateFlow(0)

    /**
     * Смяна на града ИЛИ натискане на „Обнови“ → нова заявка към Open-Meteo.
     * flatMapLatest отказва старата заявка, ако междувременно дойде нова.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val weather: StateFlow<WeatherUiState> =
        combine(weatherRepository.selectedCity, refreshTrigger) { city, _ -> city }
            .flatMapLatest { city -> if (city == null) flowOf(WeatherUiState.NoCity) else loadWeather(city) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = weatherRepository.selectedCity.value
                    ?.let { WeatherUiState.Loading(it) } ?: WeatherUiState.NoCity,
            )

    /**
     * Тоалетът: combine слива времето (от интернет) и дрехите (от Room).
     * Промяна в КОЕТО И ДА Е от двете (напр. нова дреха) → тоалетът се пресмята наново.
     */
    val outfit: StateFlow<OutfitUiState> =
        combine(weather, clothingRepository.getClothes()) { weatherState, clothes ->
            when {
                weatherState !is WeatherUiState.Success -> OutfitUiState.WaitingForWeather
                clothes.isEmpty() -> OutfitUiState.EmptyWardrobe
                else -> {
                    val current = weatherState.weather
                    OutfitUiState.Ready(
                        outfit = OutfitRecommender.recommend(clothes, current.temperature, current.isRainy),
                        weather = current,
                    )
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), OutfitUiState.WaitingForWeather)

    private fun loadWeather(city: City): Flow<WeatherUiState> = flow {
        emit(WeatherUiState.Loading(city))
        try {
            emit(WeatherUiState.Success(weatherRepository.getWeather(city)))
        } catch (e: CancellationException) {
            throw e // отказана заявка - не е грешка
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            emit(WeatherUiState.Error(city)) // няма интернет, сървърът не отговаря...
        }
    }

    fun refresh() = refreshTrigger.update { it + 1 }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
