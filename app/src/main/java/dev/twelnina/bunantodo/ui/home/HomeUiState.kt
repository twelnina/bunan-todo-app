package dev.twelnina.bunantodo.ui.home

import dev.twelnina.bunantodo.model.Todo
import java.time.LocalDate

data class HomeUiState(
    val today: LocalDate? = null,
    val todayItems: List<Todo> = emptyList(),
    val overdueItems: List<Todo> = emptyList()
)