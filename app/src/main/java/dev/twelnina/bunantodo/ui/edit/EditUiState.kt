package dev.twelnina.bunantodo.ui.edit

import dev.twelnina.bunantodo.data.local.TodoEntity
import dev.twelnina.bunantodo.model.TodoTag
import java.time.LocalDate

data class EditUiState(
    val today: LocalDate? = null,
    val isLoaded: Boolean = false,
    val originalTodo: TodoEntity? = null,

    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val plannedDate: LocalDate? = null,
    val selectedTag: TodoTag? = null,

    val isCompleted: Boolean = false
) {
    val hasRequiredFields: Boolean get() = title.isNotBlank() && description.isNotBlank()

    val hasChanges: Boolean
        get() = originalTodo != null && (
                title != originalTodo.title ||
                        description != originalTodo.detail ||
                        plannedDate != originalTodo.plannedDate ||
                        selectedTag != originalTodo.tag
                )
}
