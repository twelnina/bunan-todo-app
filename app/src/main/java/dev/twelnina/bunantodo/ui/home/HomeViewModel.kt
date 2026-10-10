package dev.twelnina.bunantodo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.twelnina.bunantodo.TodoApplication
import dev.twelnina.bunantodo.data.local.TodoEntity
import dev.twelnina.bunantodo.data.repository.TodoRepository
import dev.twelnina.bunantodo.data.time.CurrentDateProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class HomeViewModel(
    private val repository: TodoRepository,
    private val currentDateProvider: CurrentDateProvider
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> =
        currentDateProvider.observeDate().flatMapLatest { today ->
            combine(
                repository.observeTodayItems(today),
                repository.observePastIncompleteItems(today)
            ) { todayItems, pastIncompleteItems ->
                HomeUiState(
                    today = today,
                    todayItems = todayItems,
                    pastIncompleteItems = pastIncompleteItems.map { entity ->
                        val plannedDate = entity.plannedDate
                        PastIncompleteItem(
                            todo = entity,
                            daysSincePlannedDate = ChronoUnit.DAYS.between(plannedDate, today)
                        )
                    }
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )

    fun updateCompleted(todo: TodoEntity, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.update(
                todo.copy(isCompleted = isCompleted)
            )
        }
    }

    fun updatePlannedDate(todo: TodoEntity, newPlannedDate: LocalDate) {
        viewModelScope.launch {
            repository.update(
                todo.copy(plannedDate = newPlannedDate)
            )
        }
    }

    fun deleteTodo(todo: TodoEntity, onDeleted: (TodoEntity) -> Unit) {
        viewModelScope.launch {
            repository.delete(todo)
            onDeleted(todo)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TodoApplication)
                val repository = application.repository
                HomeViewModel(
                    repository = repository,
                    currentDateProvider = application.currentDateProvider
                )
            }
        }
    }
}
