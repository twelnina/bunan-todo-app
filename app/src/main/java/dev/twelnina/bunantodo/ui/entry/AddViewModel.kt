package dev.twelnina.bunantodo.ui.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.twelnina.bunantodo.TodoApplication
import dev.twelnina.bunantodo.data.local.TodoEntity
import dev.twelnina.bunantodo.data.repository.TodoRepository
import dev.twelnina.bunantodo.data.time.CurrentDateProvider
import dev.twelnina.bunantodo.model.TodoTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddViewModel(
    private val todoRepository: TodoRepository,
    private val currentDateProvider: CurrentDateProvider
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddUiState())
    val uiState: StateFlow<AddUiState> = _uiState.asStateFlow()

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
                .let { it.copy(isEntryValid = isValid(it)) }
        }
    }

    fun updateDescription(newDescription: String) {
        _uiState.update { currentState ->
            currentState.copy(description = newDescription)
                .let { it.copy(isEntryValid = isValid(it)) }
        }
    }

    fun updatePlannedDate(newPlannedDate: LocalDate?) {
        _uiState.update { currentState ->
            currentState.copy(
                plannedDate = newPlannedDate
            )
        }
    }

    fun updateSelectedTag(newTag: TodoTag) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedTag = if (currentState.selectedTag == newTag) null else newTag
            )
        }
    }

    fun saveTodo(onSaved: () -> Unit) {
        uiState.value.run {
            viewModelScope.launch {
                todoRepository.insert(
                    TodoEntity(
                        title = title,
                        detail = description,
                        plannedDate = plannedDate,
                        tag = selectedTag
                    )
                )
                onSaved()
            }
        }
    }

    fun isValid(state: AddUiState) =
        state.title.isNotBlank() && state.description.isNotBlank()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TodoApplication)
                val repository = application.repository
                AddViewModel(
                    todoRepository = repository,
                    currentDateProvider = application.currentDateProvider
                )
            }
        }
    }
}
