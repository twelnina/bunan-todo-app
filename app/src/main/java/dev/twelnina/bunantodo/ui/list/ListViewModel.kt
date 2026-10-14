package dev.twelnina.bunantodo.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.twelnina.bunantodo.TodoApplication
import dev.twelnina.bunantodo.data.local.TodoEntity
import dev.twelnina.bunantodo.data.repository.TodoRepository
import dev.twelnina.bunantodo.data.time.CurrentDateProvider
import dev.twelnina.bunantodo.model.PlannedDateFilter
import dev.twelnina.bunantodo.model.TodoTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class ListViewModel(
    private val todoRepository: TodoRepository,
    private val currentDateProvider: CurrentDateProvider,
    initialPlannedDateFilter: PlannedDateFilter = PlannedDateFilter.ALL
) : ViewModel() {
    private data class ControlsState(
        val searchQuery: String = "",
        val selectedTags: Set<TodoTag> = emptySet(),
        val selectedPlannedDateFilter: PlannedDateFilter,
        val showBottomSheet: Boolean = false
    )

    private val _controlsState = MutableStateFlow(
        ControlsState(
            selectedPlannedDateFilter = initialPlannedDateFilter
        )
    )

    val uiState: StateFlow<ListUiState> = combine(
        currentDateProvider.observeDate(),
        todoRepository.getAllItems(),
        _controlsState
    ) { today, items, controls ->
        val filteredTodos = filterTodos(
            items = items,
            query = controls.searchQuery,
            tags = controls.selectedTags,
            plannedDateFilter = controls.selectedPlannedDateFilter,
            today = today
        )

        ListUiState(
            today = today,
            searchQuery = controls.searchQuery,
            selectedTags = controls.selectedTags,
            selectedPlannedDateFilter = controls.selectedPlannedDateFilter,
            showBottomSheet = controls.showBottomSheet,
            todoGroups = groupTodosByDate(filteredTodos)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ListUiState(
            selectedPlannedDateFilter = initialPlannedDateFilter
        )
    )

    fun onQueryChange(newQuery: String) {
        _controlsState.update { currentState ->
            currentState.copy(searchQuery = newQuery)
        }
    }

    fun onTagSelected(tag: TodoTag) {
        _controlsState.update { currentState ->
            val selectedTags = if (tag in currentState.selectedTags) {
                currentState.selectedTags - tag
            } else {
                currentState.selectedTags + tag
            }

            currentState.copy(selectedTags = selectedTags)
        }
    }

    fun onPlannedDateFilterSelected(filter: PlannedDateFilter) {
        _controlsState.update { currentState ->
            currentState.copy(
                selectedPlannedDateFilter = filter,
                showBottomSheet = false
            )
        }
    }

    fun showBottomSheet() {
        _controlsState.update { currentState ->
            currentState.copy(showBottomSheet = true)
        }
    }

    fun dismissBottomSheet() {
        _controlsState.update { currentState ->
            currentState.copy(showBottomSheet = false)
        }
    }

    fun updateCompleted(todo: TodoEntity, isCompleted: Boolean) {
        viewModelScope.launch {
            todoRepository.update(
                todo.copy(isCompleted = isCompleted)
            )
        }
    }

    fun updatePlannedDate(todo: TodoEntity, newPlannedDate: LocalDate) {
        viewModelScope.launch {
            todoRepository.update(
                todo.copy(plannedDate = newPlannedDate)
            )
        }
    }

    fun deleteTodo(todo: TodoEntity, onDeleted: (TodoEntity) -> Unit) {
        viewModelScope.launch {
            todoRepository.delete(todo)
            onDeleted(todo)
        }
    }

    companion object {
        fun createFactory(
            initialPlannedDateFilter: PlannedDateFilter = PlannedDateFilter.ALL
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TodoApplication)
                val repository = application.repository

                ListViewModel(
                    todoRepository = repository,
                    currentDateProvider = application.currentDateProvider,
                    initialPlannedDateFilter = initialPlannedDateFilter
                )
            }
        }
    }
}


internal fun filterTodos(
    items: List<TodoEntity>,
    query: String,
    tags: Set<TodoTag>,
    plannedDateFilter: PlannedDateFilter,
    today: LocalDate
): List<TodoEntity> {
    return items.filter { item ->
        val matchesQuery =
            item.title.contains(query, ignoreCase = true) ||
                    item.detail.contains(query, ignoreCase = true)
        val matchesTag = if (tags.isEmpty()) {
            true
        } else {
            item.tag in tags
        }
        val matchesDate = when (plannedDateFilter) {
            PlannedDateFilter.ALL -> true
            PlannedDateFilter.TODAY -> item.plannedDate == today
            PlannedDateFilter.TOMORROW -> item.plannedDate == today.plusDays(1)
            PlannedDateFilter.THIS_WEEK -> {
                val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                item.plannedDate?.let { date ->
                    (date.isEqual(today) || date.isAfter(today)) &&
                            (date.isEqual(endOfWeek) || date.isBefore(endOfWeek))
                } ?: false
            }

            PlannedDateFilter.PAST_INCOMPLETE -> !item.isCompleted &&
                    (item.plannedDate?.isBefore(today) ?: false)

            PlannedDateFilter.UNSCHEDULED -> item.plannedDate == null
        }

        matchesQuery && matchesTag && matchesDate
    }
}

internal fun groupTodosByDate(todos: List<TodoEntity>): List<TodoDateGroup> {
    return todos
        .groupBy { todo -> todo.plannedDate }
        .map { (date, todoForDate) ->
            TodoDateGroup(date, todoForDate)
        }
        .sortedWith { firstGroup, secondGroup ->
            when {
                firstGroup.date == null && secondGroup.date == null -> 0
                firstGroup.date == null -> 1
                secondGroup.date == null -> -1
                else -> firstGroup.date.compareTo(secondGroup.date)
            }
        }
}