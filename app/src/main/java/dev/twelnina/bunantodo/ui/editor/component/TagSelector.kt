package dev.twelnina.bunantodo.ui.editor.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.twelnina.bunantodo.R
import dev.twelnina.bunantodo.model.TodoTag
import dev.twelnina.bunantodo.ui.theme.TodoAppTheme

@Composable
fun TagSelector(
    selectedTag: TodoTag?,
    onTagChange: (TodoTag) -> Unit,
    modifier: Modifier = Modifier
) {
    val tagColors = TodoAppTheme.tagColors

    Column(modifier = modifier) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TodoTag.entries.forEach { tag ->
                val tagColor = tagColors.colorFor(tag)
                FilterChip(
                    selected = selectedTag == tag,
                    onClick = { onTagChange(tag) },
                    label = { Text(stringResource(tag.labelRes)) },
                    leadingIcon = if (selectedTag == tag) {
                        {
                            Icon(
                                painter = painterResource(R.drawable.check_24px),
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = tagColor.copy(alpha = 0.2f)
                    )
                )
            }
        }
    }
}