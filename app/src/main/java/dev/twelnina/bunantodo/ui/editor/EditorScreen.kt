package dev.twelnina.bunantodo.ui.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.twelnina.bunantodo.R
import dev.twelnina.bunantodo.model.TodoTag
import dev.twelnina.bunantodo.ui.editor.component.PlannedDatePicker
import dev.twelnina.bunantodo.ui.editor.component.TagSelector
import dev.twelnina.bunantodo.ui.theme.TodoAppTheme
import java.time.LocalDate

@Composable
fun EditorScreen(
    mode: EditorMode,
    viewModel: EditorViewModel = viewModel(factory = EditorViewModel.factory(mode)),
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EditorScreenContent(
        uiState = uiState,
        mode = mode,
        onBack = onBack,
        onDone = { viewModel.saveTodo(onSaved = onSaved) },
        onTitleChange = viewModel::updateTitle,
        onDetailChange = viewModel::updateDetail,
        onPlannedDateChange = viewModel::updatePlannedDate,
        onSelectedTagChange = viewModel::updateSelectedTag
    )
}

@Composable
private fun EditorScreenContent(
    uiState: EditorUiState,
    mode: EditorMode,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDetailChange: (String) -> Unit,
    onPlannedDateChange: (LocalDate?) -> Unit,
    onSelectedTagChange: (TodoTag) -> Unit
) {
    Scaffold(
        topBar = {
            val titleRes = when (mode) {
                EditorMode.Add -> R.string.todo_add_screen_title
                is EditorMode.Edit -> R.string.todo_edit_screen_title
            }
            TopAppBar(
                title = { Text(stringResource(titleRes)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource((R.drawable.arrow_back_24px)),
                            contentDescription = stringResource(R.string.common_action_back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = onDone,
                enabled = uiState.isEntryValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
            ) {
                Text(stringResource(R.string.todo_editor_action_save))
            }
        }
    ) { innerPadding ->
        val today = uiState.today

        if (uiState.isTodoMissing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.todo_editor_error_not_found))
            }
            return@Scaffold
        }

        if (uiState.isLoading || today == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            var hasEditedTitle by rememberSaveable { mutableStateOf(false) }
            val isTitleError = hasEditedTitle && uiState.title.isBlank()

            val requiredTitleLabel = stringResource(R.string.todo_editor_field_title_required)

            TextField(
                value = uiState.title,
                onValueChange = { newTitle ->
                    hasEditedTitle = true
                    onTitleChange(newTitle)
                },
                label = {
                    Text(
                        text = stringResource(R.string.todo_editor_field_title),
                        modifier = Modifier.clearAndSetSemantics {
                            text = AnnotatedString(requiredTitleLabel)
                        }
                    )
                },
                supportingText = {
                    Text(
                        text = stringResource(
                            if (isTitleError) R.string.todo_editor_error_title_required
                            else R.string.todo_editor_required_fields_hint
                        )
                    )
                },
                isError = isTitleError,
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
                label = { Text(text = stringResource(R.string.todo_editor_field_detail)) },
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

            PlannedDatePicker(
                today = today,
                onDateChange = onPlannedDateChange,
                selectedDate = uiState.plannedDate
            )

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
}


@Preview
@Composable
private fun EditorScreenContentPreview() {
    TodoAppTheme {
        EditorScreenContent(
            uiState = EditorUiState(today = LocalDate.of(2026, 9, 23)),
            mode = EditorMode.Add,
            onBack = {},
            onDone = {},
            onTitleChange = {},
            onDetailChange = {},
            onPlannedDateChange = {},
            onSelectedTagChange = {}
        )
    }
}
