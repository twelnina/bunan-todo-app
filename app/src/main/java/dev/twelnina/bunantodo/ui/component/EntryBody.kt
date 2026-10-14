package dev.twelnina.bunantodo.ui.component

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.twelnina.bunantodo.R
import dev.twelnina.bunantodo.model.TodoTag
import dev.twelnina.bunantodo.ui.edit.component.TagSelector
import dev.twelnina.bunantodo.ui.edit.component.PlannedDatePicker
import java.time.LocalDate

@Composable
fun TodoEntryBody(
    today: LocalDate?,
    title: String,
    description: String,
    plannedDate: LocalDate?,
    selectedTag: TodoTag?,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateChange: (LocalDate?) -> Unit,
    onTagChange: (TodoTag) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TextField(
            value = title,
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
            value = description,
            onValueChange = onDescriptionChange,
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
        if (today == null) {
            CircularProgressIndicator()
        } else {
            PlannedDatePicker(
                today = today,
                onDateChange = onDateChange,
                selectedDate = plannedDate
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.todo_editor_section_tags),
            style = MaterialTheme.typography.titleSmall
        )
        TagSelector(
            onTagChange = onTagChange,
            selectedTag = selectedTag,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
