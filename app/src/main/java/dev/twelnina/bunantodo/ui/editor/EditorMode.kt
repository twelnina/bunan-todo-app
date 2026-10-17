package dev.twelnina.bunantodo.ui.editor

sealed interface EditorMode {
    data object Add : EditorMode
    data class Edit(val id: Int) : EditorMode
}