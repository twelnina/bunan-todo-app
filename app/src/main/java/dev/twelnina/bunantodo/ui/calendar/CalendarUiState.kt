package dev.twelnina.bunantodo.ui.calendar

import dev.twelnina.bunantodo.model.Todo
import java.time.LocalDate

data class CalendarUiState(
    val today: LocalDate? = null,
    val selectedDate: LocalDate? = null,
    val todos: List<Todo> = emptyList(),
    val selectedDateTodos: List<Todo> = emptyList(),
    val todoCountsByDate: Map<LocalDate, Int> = emptyMap()
)
