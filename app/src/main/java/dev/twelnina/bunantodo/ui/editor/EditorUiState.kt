package dev.twelnina.bunantodo.ui.editor

import dev.twelnina.bunantodo.model.TodoTag
import java.time.LocalDate

data class EditorUiState(
    val isLoading: Boolean = false,
    val isTodoMissing: Boolean = false,
    val today: LocalDate? = null,
    val title: String = "",
    val detail: String = "",
    val plannedDate: LocalDate? = null,
    val selectedTag: TodoTag? = null,
) {
    val isEntryValid: Boolean
        get() = !isLoading && !isTodoMissing && today != null && title.isNotBlank()
}
