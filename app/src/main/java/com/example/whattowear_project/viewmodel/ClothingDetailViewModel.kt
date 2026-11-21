package com.example.whattowear_project.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whattowear_project.data.entity.ClothingItem
import com.example.whattowear_project.data.repository.ClothingRepository
import com.example.whattowear_project.ui.common.NavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel за екрана „Детайли“: показва една дреха и може да я изтрие.
 */
@HiltViewModel
class ClothingDetailViewModel @Inject constructor(
    private val repository: ClothingRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /** id-то идва от навигацията (аргумент itemId в nav_graph.xml). */
    val itemId: Long = checkNotNull(savedStateHandle[NavArgs.ITEM_ID])

    /** Дрехата. Flow = след редакция екранът се обновява сам. */
    val item: StateFlow<ClothingItem?> = repository.getById(itemId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    /** Става true след изтриване - екранът тогава се затваря. */
    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    fun delete() {
        val current = item.value ?: return
        viewModelScope.launch {
            repository.delete(current)
            _deleted.value = true
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
