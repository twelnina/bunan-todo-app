package dev.twelnina.bunantodo.ui.edit

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

class EditViewModel(
    private val todoRepository: TodoRepository,
    private val currentDateProvider: CurrentDateProvider
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditUiState())
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            currentDateProvider.observeDate().collect { today ->
                _uiState.update { currentState ->
                    currentState.copy(today = today)
                }
            }
        }
    }

    fun loadItem(id: Int) {
        viewModelScope.launch {
            todoRepository.getItem(id)?.run {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoaded = true,
                        id = id,
                        title = title,
                        description = detail,
                        plannedDate = plannedDate,
                        selectedTag = tag,
                        isCompleted = isCompleted,
                        originalTodo = this
                    )
                }
            }
        }
    }

    fun updateTodo(onUpdated: () -> Unit) {
        uiState.value.run {
            viewModelScope.launch {
                todoRepository.update(
                    TodoEntity(
                        id = id,
                        title = title,
                        detail = description,
                        plannedDate = plannedDate,
                        tag = selectedTag,
                        isCompleted = isCompleted
                    )
                )
                onUpdated()
            }
        }
    }

    fun deleteTodo(onDeleted: (TodoEntity) -> Unit) {
        val deletedTodo = uiState.value.run {
            TodoEntity(
                id = id,
                title = title,
                detail = description,
                plannedDate = plannedDate,
                tag = selectedTag,
                isCompleted = isCompleted
            )
        }

        viewModelScope.launch {
            todoRepository.delete(deletedTodo)
            onDeleted(deletedTodo)
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.update { currentState ->
            currentState.copy(title = newTitle)
        }
    }

    fun updateDescription(newDescription: String) {
        _uiState.update { currentState ->
            currentState.copy(description = newDescription)
        }
    }

    fun updatePlannedDate(newPlannedDate: LocalDate?) {
        _uiState.update { currentState ->
            currentState.copy(plannedDate = newPlannedDate)
        }
    }

    fun updateSelectedTag(newSelectedTag: TodoTag?) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedTag = if (currentState.selectedTag == newSelectedTag) null else newSelectedTag
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TodoApplication)
                val repository = application.repository
                EditViewModel(
                    todoRepository = repository,
                    currentDateProvider = application.currentDateProvider
                )
            }
        }
    }
}
