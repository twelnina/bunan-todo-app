package dev.twelnina.bunantodo.ui.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.twelnina.bunantodo.R
import dev.twelnina.bunantodo.model.TodoTag
import dev.twelnina.bunantodo.ui.edit.component.PlannedDatePicker
import dev.twelnina.bunantodo.ui.edit.component.TagSelector
import java.time.LocalDate

@Composable
fun EditorScreen(
    mode: EditorMode,
    viewModel: EditorViewModel = viewModel(factory = EditorViewModel.factory(mode))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EditorScreenContent(
        uiState = uiState,
        onTitleChange = viewModel::updateTitle,
        onDetailChange = viewModel::updateDetail,
        onPlannedDateChange = viewModel::updatePlannedDate,
        onSelectedTagChange = viewModel::updateSelectedTag
    )
}

@Composable
private fun EditorScreenContent(
    uiState: EditorUiState,
    onTitleChange: (String) -> Unit,
    onDetailChange: (String) -> Unit,
    onPlannedDateChange: (LocalDate?) -> Unit,
    onSelectedTagChange: (TodoTag) -> Unit
) {
    val today = uiState.today

    Column(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            label = { Text(text = stringResource(R.string.todo_editor_field_title)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = uiState.detail,
            onValueChange = onDetailChange,
            label = { Text(text = stringResource(R.string.todo_editor_field_description)) },
            minLines = 5,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.todo_editor_section_planned_date),
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (today == null) {
            CircularProgressIndicator()
        } else {
            PlannedDatePicker(
                today = today,
                onDateChange = onPlannedDateChange,
                selectedDate = uiState.plannedDate
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.todo_editor_section_tags),
            style = MaterialTheme.typography.titleSmall
        )
        TagSelector(
            onTagChange = onSelectedTagChange,
            selectedTag = uiState.selectedTag,
            modifier = Modifier.fillMaxWidth()
        )
    }
}