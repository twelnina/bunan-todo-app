package dev.twelnina.bunantodo.ui.list

import dev.twelnina.bunantodo.model.PlannedDateFilter
import dev.twelnina.bunantodo.model.Todo
import dev.twelnina.bunantodo.model.TodoTag
import java.time.LocalDate

data class TodoDateGroup(
    val date: LocalDate?,
    val todos: List<Todo>
)

data class ListUiState(
    val today: LocalDate? = null,
    val searchQuery: String = "",
    val selectedTags: Set<TodoTag> = emptySet(),
    val selectedPlannedDateFilter: PlannedDateFilter = PlannedDateFilter.ALL,
    val showBottomSheet: Boolean = false,
    val todoGroups: List<TodoDateGroup> = emptyList()
)
