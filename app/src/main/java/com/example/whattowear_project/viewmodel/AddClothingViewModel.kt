package com.example.whattowear_project.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whattowear_project.data.entity.ClothingItem
import com.example.whattowear_project.data.entity.ClothingSubtype
import com.example.whattowear_project.data.entity.ClothingType
import com.example.whattowear_project.data.repository.ClothingRepository
import com.example.whattowear_project.ui.common.NavArgs
import com.example.whattowear_project.validation.ClothingFormError
import com.example.whattowear_project.validation.ClothingValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel за формата „Нова дреха“ / „Редактиране“.
 * Без itemId - създава нова дреха (insert); с itemId - редактира съществуваща (update).
 */
@HiltViewModel
class AddClothingViewModel @Inject constructor(
    private val repository: ClothingRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /** id от навигацията; NO_ID = нова дреха. */
    private val itemId: Long = savedStateHandle[NavArgs.ITEM_ID] ?: NavArgs.NO_ID
    val isEditing: Boolean = itemId != NavArgs.NO_ID

    val selectedType: StateFlow<ClothingType?> =
        savedStateHandle.getStateFlow(KEY_TYPE, null)

    val selectedSubtype: StateFlow<ClothingSubtype?> =
        savedStateHandle.getStateFlow(KEY_SUBTYPE, null)

    /** Грешките от последния опит за запис (празно = няма грешки). */
    private val _errors = MutableStateFlow<Set<ClothingFormError>>(emptySet())
    val errors: StateFlow<Set<ClothingFormError>> = _errors.asStateFlow()

    /** Става true след успешен запис - екранът тогава се затваря. */
    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    /** При редакция: дрехата, с която да попълним полетата (еднократно). */
    private val _itemToFill = MutableStateFlow<ClothingItem?>(null)
    val itemToFill: StateFlow<ClothingItem?> = _itemToFill.asStateFlow()

    /** Оригиналната дреха при редакция - пазим id-то и снимката ѝ. */
    private var originalItem: ClothingItem? = null

    /**
     * Дали формата вече е попълнена. Пазим го в SavedStateHandle, за да НЕ я попълним
     * наново след завъртане - иначе ще изтрием промените на потребителя.
     */
    private var formFilled: Boolean
        get() = savedStateHandle[KEY_FORM_FILLED] ?: false
        set(value) {
            savedStateHandle[KEY_FORM_FILLED] = value
        }

    init {
        if (isEditing) {
            viewModelScope.launch {
                val item = repository.getById(itemId).first() ?: return@launch
                originalItem = item
                if (!formFilled) {
                    savedStateHandle[KEY_TYPE] = item.type
                    savedStateHandle[KEY_SUBTYPE] = item.subtype
                    _itemToFill.value = item
                }
            }
        }
    }

    /** Фрагментът извиква това, след като е попълнил полетата. */
    fun onFormFilled() {
        formFilled = true
        _itemToFill.value = null
    }

    fun selectType(type: ClothingType?) {
        if (type == selectedType.value) return
        savedStateHandle[KEY_SUBTYPE] = null // подвидът на стария тип вече не важи
        savedStateHandle[KEY_TYPE] = type
        _errors.update { it - ClothingFormError.TYPE_MISSING - ClothingFormError.SUBTYPE_MISMATCH }
    }

    fun selectSubtype(subtype: ClothingSubtype?) {
        savedStateHandle[KEY_SUBTYPE] = subtype
    }

    /** Махаме грешката за името, щом потребителят започне да пише. */
    fun onNameChanged() {
        _errors.update { it - ClothingFormError.NAME_EMPTY - ClothingFormError.NAME_TOO_LONG }
    }

    fun save(name: String, minTemp: Int, maxTemp: Int, waterproof: Boolean) {
        val type = selectedType.value
        val subtype = selectedSubtype.value
        val errors = ClothingValidator.validate(name, type, subtype, minTemp, maxTemp)
        _errors.value = errors
        if (errors.isNotEmpty() || type == null) return

        // Записът в базата е suspend - пускаме го в корутина на ViewModel-а.
        viewModelScope.launch {
            val original = originalItem
            if (isEditing && original != null) {
                // Update: същото id и снимка, нови стойности.
                repository.update(
                    original.copy(
                        name = name.trim(),
                        type = type,
                        subtype = subtype,
                        minTemp = minTemp,
                        maxTemp = maxTemp,
                        waterproof = waterproof,
                    ),
                )
            } else {
                // Create: нова дреха.
                repository.add(
                    ClothingItem(
                        name = name.trim(),
                        type = type,
                        subtype = subtype,
                        minTemp = minTemp,
                        maxTemp = maxTemp,
                        waterproof = waterproof,
                    ),
                )
            }
            _saved.value = true
        }
    }

    private companion object {
        const val KEY_TYPE = "type"
        const val KEY_SUBTYPE = "subtype"
        const val KEY_FORM_FILLED = "form_filled"
    }
}
