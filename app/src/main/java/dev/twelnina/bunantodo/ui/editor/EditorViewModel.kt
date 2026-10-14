package dev.twelnina.bunantodo.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.twelnina.bunantodo.TodoApplication
import dev.twelnina.bunantodo.data.repository.TodoRepository
import dev.twelnina.bunantodo.data.time.CurrentDateProvider
import dev.twelnina.bunantodo.model.TodoTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class EditorViewModel(
    private val repository: TodoRepository,
    private val currentDateProvider: CurrentDateProvider
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            currentDateProvider.observeDate().collect { today ->
                _uiState.update { currentState ->
                    currentState.copy(today = today)
                }
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.update { currentState ->
            currentState.copy(title = newTitle)
        }
    }

    fun updateDetail(newDetail: String) {
        _uiState.update { currentState ->
            currentState.copy(detail = newDetail)
        }
    }

    fun updatePlannedDate(newPlanedDate: LocalDate?) {
        _uiState.update { currentState ->
            currentState.copy(plannedDate = newPlanedDate)
        }
    }

    fun updateSelectedTag(newSelectedTag: TodoTag?) {
        _uiState.update { currentState ->
            currentState.copy(selectedTag = newSelectedTag)
        }
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TodoApplication)
                val repository = application.repository
                EditorViewModel(
                    repository = repository,
                    currentDateProvider = application.currentDateProvider
                )
            }
        }
    }
}