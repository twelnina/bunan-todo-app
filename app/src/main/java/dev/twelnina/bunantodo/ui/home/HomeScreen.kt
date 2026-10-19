package dev.twelnina.bunantodo.ui.home

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.twelnina.bunantodo.R
import dev.twelnina.bunantodo.model.Todo
import dev.twelnina.bunantodo.ui.component.TodoItem
import dev.twelnina.bunantodo.ui.component.dialogs.PlannedDatePickerDialog
import dev.twelnina.bunantodo.ui.rememberYearMonthDayFormatter
import dev.twelnina.bunantodo.ui.theme.RobotoFlexExpanded
import dev.twelnina.bunantodo.ui.theme.TodoAppTheme
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random

private const val PAST_INCOMPLETE_PREVIEW_LIMIT = 3

@Composable
fun HomeScreen(
    onEditTodo: (Int) -> Unit,
    onDeleted: (Todo) -> Unit,
    onSeeAllPastIncomplete: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreenContent(
        uiState = uiState,
        onCompletedChange = viewModel::updateCompleted,
        onPlannedDateChange = viewModel::updatePlannedDate,
        onEditTodo = onEditTodo,
        onDelete = { todo ->
            viewModel.deleteTodo(
                todo = todo,
                onDeleted = onDeleted
            )
        },
        onSeeAllPastIncomplete = onSeeAllPastIncomplete
    )
}

@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onCompletedChange: (Todo, Boolean) -> Unit,
    onPlannedDateChange: (Todo, LocalDate) -> Unit,
    onEditTodo: (Int) -> Unit,
    onDelete: (Todo) -> Unit,
    onSeeAllPastIncomplete: () -> Unit
) {
    val today = uiState.today

    if (today == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    var playInitialAnimation by rememberSaveable { mutableStateOf(true) }

    val visiblePastIncompleteItems = uiState.pastIncompleteItems.take(PAST_INCOMPLETE_PREVIEW_LIMIT)
    val hasMorePastIncompleteItems =
        uiState.pastIncompleteItems.size > PAST_INCOMPLETE_PREVIEW_LIMIT

    val hasItems = uiState.todayItems.isNotEmpty() || uiState.pastIncompleteItems.isNotEmpty()

    var expandedTodoId by rememberSaveable { mutableStateOf<Int?>(null) }

    var dateChangeTodoId by rememberSaveable { mutableStateOf<Int?>(null) }
    val todoForDateChange = dateChangeTodoId?.let { id ->
        uiState.todayItems.firstOrNull { it.id == id }
            ?: uiState.pastIncompleteItems.firstOrNull { it.id == id }
    }

    LaunchedEffect(hasItems) {
        if (hasItems && playInitialAnimation) {
            withFrameNanos { }
            playInitialAnimation = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = if (hasItems) 200.dp else 0.dp
            )
        ) {
            item(key = "home-header") {
                val now = LocalDateTime.now()
                Column(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = today.format(rememberYearMonthDayFormatter()),
                        fontFamily = RobotoFlexExpanded,
                        fontSize = 40.sp,
                        fontWeight = FontWeight(900),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = rememberHomeMessage(
                            hour = now.hour,
                            dayKey = today.toEpochDay()
                        ),
                        fontFamily = RobotoFlexExpanded,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item(key = "today-header") {
                Text(
                    text = stringResource(
                        R.string.home_section_today_title,
                        uiState.todayItems.size
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (uiState.todayItems.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.home_empty_message_today),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                itemsIndexed(
                    items = uiState.todayItems, key = { _, todo -> todo.id }) { index, todo ->

                    val visibleState = remember {
                        MutableTransitionState(!playInitialAnimation).apply {
                            targetState = true
                        }
                    }

                    val expanded = expandedTodoId == todo.id

                    AnimatedVisibility(
                        visibleState = visibleState, enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 300, delayMillis = index * 50
                            )
                        ) + slideInVertically(
                            animationSpec = tween(
                                durationMillis = 300, delayMillis = index * 50
                            ), initialOffsetY = { height ->
                                height / 4
                            })
                    ) {
                        TodoItem(
                            todoItemInfo = todo,
                            index = index,
                            count = uiState.todayItems.size,
                            expanded = expanded,
                            onCheckedChange = onCompletedChange,
                            onMoreClick = {
                                expandedTodoId = if (expanded) null else todo.id
                            },
                            onChangePlannedDate = {
                                expandedTodoId = null
                                dateChangeTodoId = todo.id
                            },
                            onEdit = {
                                expandedTodoId = null
                                onEditTodo(todo.id)
                            },
                            onDelete = {
                                expandedTodoId = null
                                onDelete(todo)
                            },
                        )
                    }
                }
            }

            item(key = "section-space") {
                Spacer(modifier = Modifier.height(32.dp))
            }

            item(key = "past-incomplete-header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            R.string.home_section_past_incomplete_title,
                            uiState.pastIncompleteItems.size
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )

                    if (hasMorePastIncompleteItems) {
                        TextButton(onClick = onSeeAllPastIncomplete) {
                            Text(text = stringResource(R.string.home_action_see_all))
                        }
                    }
                }
            }

            if (uiState.pastIncompleteItems.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.home_empty_message_past_incomplete),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                itemsIndexed(
                    items = visiblePastIncompleteItems,
                    key = { _, todo -> todo.id }) { index, todo ->

                    val visibleState = remember {
                        MutableTransitionState(!playInitialAnimation).apply {
                            targetState = true
                        }
                    }

                    val expanded = expandedTodoId == todo.id

                    AnimatedVisibility(
                        visibleState = visibleState, enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 300, delayMillis = index * 50
                            )
                        ) + slideInVertically(
                            animationSpec = tween(
                                durationMillis = 300, delayMillis = index * 50
                            ),
                            initialOffsetY = { height ->
                                height / 4
                            }
                        )
                    ) {
                        TodoItem(
                            todoItemInfo = todo,
                            daysSincePlannedDate = todo.calculateDaysSincePlannedDate(today),
                            index = index,
                            count = visiblePastIncompleteItems.size,
                            expanded = expanded,
                            onCheckedChange = onCompletedChange,
                            onMoreClick = {
                                expandedTodoId = if (expanded) null else todo.id
                            },
                            onChangePlannedDate = {
                                expandedTodoId = null
                                dateChangeTodoId = todo.id
                            },
                            onEdit = {
                                expandedTodoId = null
                                onEditTodo(todo.id)
                            },
                            onDelete = {
                                expandedTodoId = null
                                onDelete(todo)
                            },
                        )
                    }
                }
            }
        }

        todoForDateChange?.let { todo ->
            PlannedDatePickerDialog(
                today = today,
                previousDate = todo.plannedDate,
                onDismissRequest = { dateChangeTodoId = null },
                onConfirmRequest = { newDate ->
                    onPlannedDateChange(todo, newDate)
                    dateChangeTodoId = null
                }
            )
        }
    }
}


@StringRes
private fun greetingResource(hour: Int): Int = when (hour) {
    in 5..11 -> R.string.home_greeting_morning
    in 12..17 -> R.string.home_greeting_afternoon
    else -> R.string.home_greeting_evening
}

@Composable
private fun rememberHomeMessage(
    hour: Int, dayKey: Long
): String {
    val greeting = stringResource(greetingResource(hour))
    val phrases = stringArrayResource(R.array.home_motivation_phrases)

    val phraseIndex = rememberSaveable(dayKey, phrases.size) {
        Random.nextInt(phrases.size)
    }

    return stringResource(
        R.string.home_greeting_message_format,
        greeting,
        phrases[phraseIndex]
    )
}


@Preview(showBackground = true)
@Composable
private fun EmptyHomeScreenPreview() {
    TodoAppTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                today = LocalDate.of(2026, 9, 21)
            ),
            onCompletedChange = { _, _ -> },
            onPlannedDateChange = { _, _ -> },
            onEditTodo = {},
            onDelete = {},
            onSeeAllPastIncomplete = {}
        )
    }
}
