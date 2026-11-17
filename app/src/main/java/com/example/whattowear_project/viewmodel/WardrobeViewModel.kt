package com.example.whattowear_project.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whattowear_project.data.entity.ClothingItem
import com.example.whattowear_project.data.entity.ClothingSubtype
import com.example.whattowear_project.data.entity.ClothingType
import com.example.whattowear_project.data.repository.ClothingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel за екрана „Гардероб“.
 * Живее по-дълго от фрагмента - оцелява при завъртане на екрана.
 */
@HiltViewModel
class WardrobeViewModel @Inject constructor(
    private val repository: ClothingRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /**
     * Филтрите (null = „Всички“).
     * SavedStateHandle ги запазва дори ако Android убие приложението във фонов режим.
     */
    val selectedType: StateFlow<ClothingType?> =
        savedStateHandle.getStateFlow(KEY_SELECTED_TYPE, null)

    val selectedSubtype: StateFlow<ClothingSubtype?> =
        savedStateHandle.getStateFlow(KEY_SELECTED_SUBTYPE, null)

    /**
     * Дрехите според филтрите. combine слива двата филтъра в един поток,
     * а flatMapLatest „превключва“ към новата заявка при всяка промяна.
     * WhileSubscribed(5000) = спира да слуша базата 5 сек. след като екранът изчезне
     * (но не и при кратко завъртане на телефона).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val clothes: StateFlow<List<ClothingItem>?> =
        combine(selectedType, selectedSubtype) { type, subtype -> type to subtype }
            .flatMapLatest { (type, subtype) -> repository.getClothes(type, subtype) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = null, // null = още зареждаме
            )

    /** Смяна на основния тип. Подвидът се нулира - старият вече не е от този тип. */
    fun selectType(type: ClothingType?) {
        if (type == selectedType.value) return
        savedStateHandle[KEY_SELECTED_SUBTYPE] = null
        savedStateHandle[KEY_SELECTED_TYPE] = type
    }

    fun selectSubtype(subtype: ClothingSubtype?) {
        savedStateHandle[KEY_SELECTED_SUBTYPE] = subtype
    }

    private companion object {
        const val KEY_SELECTED_TYPE = "selected_type"
        const val KEY_SELECTED_SUBTYPE = "selected_subtype"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
