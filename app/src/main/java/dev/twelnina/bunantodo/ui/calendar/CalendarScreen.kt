package dev.twelnina.bunantodo.ui.calendar

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import dev.twelnina.bunantodo.R
import dev.twelnina.bunantodo.data.local.TodoEntity
import dev.twelnina.bunantodo.ui.component.dialogs.PlannedDatePickerDialog
import dev.twelnina.bunantodo.ui.elapsedDaysSincePlannedDate
import dev.twelnina.bunantodo.ui.component.TodoItem
import dev.twelnina.bunantodo.ui.rememberMonthDayFormatter
import dev.twelnina.bunantodo.ui.rememberYearMonthFormatter
import dev.twelnina.bunantodo.ui.theme.TodoAppTheme
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarScreen(
    onEdit: (Int) -> Unit,
    onDeleted: (TodoEntity) -> Unit,
    viewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CalendarScreenContent(
        uiState = uiState,
        onDateSelected = viewModel::onDateSelected,
        onPreviousDate = viewModel::onPreviousDate,
        onNextDate = viewModel::onNextDate,
        onCompletedChange = viewModel::updateCompleted,
        onPlannedDateChange = viewModel::updatePlannedDate,
        onEdit = onEdit,
        onDelete = { todo ->
            viewModel.deleteTodo(todo) {
                onDeleted(todo)
            }
        }
    )
}

@Composable
private fun CalendarScreenContent(
    uiState: CalendarUiState,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousDate: (LocalDate) -> Unit,
    onNextDate: (LocalDate) -> Unit,
    onCompletedChange: (TodoEntity, Boolean) -> Unit,
    onPlannedDateChange: (TodoEntity, LocalDate) -> Unit,
    onEdit: (Int) -> Unit,
    onDelete: (TodoEntity) -> Unit
) {
    val today = uiState.today ?: return
    val currentMonth = remember { YearMonth.from(today) }
    val startMonth = remember(currentMonth) { currentMonth.minusMonths(12) }
    val endMonth = remember(currentMonth) { currentMonth.plusMonths(12) }
    val minDate = startMonth.atDay(1)
    val maxDate = endMonth.atEndOfMonth()
    val selectedDate = uiState.selectedDate
    val canGoPrevious = selectedDate?.isAfter(minDate) == true
    val canGoNext = selectedDate?.isBefore(maxDate) == true

    var expandedTodoId by rememberSaveable { mutableStateOf<Int?>(null) }

    var dateChangeTodoId by rememberSaveable { mutableStateOf<Int?>(null) }
    val todoForDateChange = dateChangeTodoId?.let { id ->
        uiState.todos.firstOrNull { it.id == id }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        MonthCalendar(
            selectedDate = selectedDate,
            startMonth = startMonth,
            endMonth = endMonth,
            todoCountsByDate = uiState.todoCountsByDate,
            onDateSelected = onDateSelected
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onPreviousDate(minDate) },
                enabled = canGoPrevious
            ) {
                Icon(
                    painter = painterResource(R.drawable.chevron_left_24px),
                    contentDescription = stringResource(R.string.calendar_action_previous_date)
                )
            }
            Text(
                text = uiState.selectedDate?.format(rememberMonthDayFormatter()) ?: "",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = { onNextDate(maxDate) },
                enabled = canGoNext
            ) {
                Icon(
                    painter = painterResource(R.drawable.chevron_right_24px),
                    contentDescription = stringResource(R.string.calendar_action_next_date)
                )
            }
        }

        if (uiState.selectedDateTodos.isNotEmpty()) {
            LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)) {
                itemsIndexed(
                    items = uiState.selectedDateTodos,
                    key = { _, todo -> todo.id }
                ) { index, todo ->
                    val expanded = expandedTodoId == todo.id

                    val daysSincePlannedDate = today.let { today ->
                        todo.elapsedDaysSincePlannedDate(today)
                    }

                    TodoItem(
                        todoItemInfo = todo,
                        daysSincePlannedDate = daysSincePlannedDate,
                        index = index,
                        count = uiState.selectedDateTodos.size,
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
                            onEdit(todo.id)
                        },
                        onDelete = {
                            expandedTodoId = null
                            onDelete(todo)
                        }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 100.dp)
            ) {
                Text(
                    text = stringResource(R.string.calendar_empty_message_selected_date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    todoForDateChange?.let {
        PlannedDatePickerDialog(
            today = today,
            previousDate = it.plannedDate,
            onDismissRequest = {
                dateChangeTodoId = null
            },
            onConfirmRequest = { newDate ->
                dateChangeTodoId = null
                onPlannedDateChange(it, newDate)
            }
        )
    }
}

@Composable
private fun MonthCalendar(
    selectedDate: LocalDate?,
    startMonth: YearMonth,
    endMonth: YearMonth,
    todoCountsByDate: Map<LocalDate, Int>,
    onDateSelected: (LocalDate) -> Unit
) {
    val currentMonth = remember { YearMonth.now() }
    val daysOfWeek = remember { daysOfWeek() }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth.coerceIn(startMonth, endMonth),
        firstDayOfWeek = daysOfWeek.first()
    )

    LaunchedEffect(state, selectedDate) {
        val date = selectedDate ?: return@LaunchedEffect
        val selectedMonth = YearMonth.from(date)

        if (selectedMonth in state.startMonth..state.endMonth) {
            state.scrollToMonth(selectedMonth)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        val visibleMonth by remember(state) {
            derivedStateOf { state.firstVisibleMonth.yearMonth }
        }

        Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
            Text(
                text = visibleMonth.format(rememberYearMonthFormatter()),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalCalendar(
                modifier = Modifier.animateContentSize(
                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                    alignment = Alignment.TopStart
                ),
                state = state,
                dayContent = { day ->
                    CalendarItem(
                        day = day,
                        isSelected = day.date == selectedDate,
                        todoCount = todoCountsByDate[day.date] ?: 0,
                        onClick = { onDateSelected(day.date) }
                    )
                }
            )
        }
    }
}

@Composable
private fun CalendarItem(
    day: CalendarDay,
    isSelected: Boolean,
    todoCount: Int,
    onClick: () -> Unit
) {
    val isMonthDate = day.position == DayPosition.MonthDate
    val highlighted = isMonthDate && isSelected

    val backgroundColor = if (highlighted) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    val textColor = when {
        highlighted -> MaterialTheme.colorScheme.onPrimary
        day.date == LocalDate.now() -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val indicatorColor = if (highlighted) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.primary
    }
    val indicatorAlpha = todoCount.coerceIn(0, 5) / 5f

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(6.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (day.position == DayPosition.MonthDate) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    color = textColor
                )

                Spacer(modifier = Modifier.height(2.dp))

                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            color = indicatorColor.copy(alpha = indicatorAlpha),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MonthCalendarPreview() {
    val currentMonth = YearMonth.now()

    val sampleCounts = mapOf(
        currentMonth.atDay(2) to 1,
        currentMonth.atDay(3) to 2,
        currentMonth.atDay(4) to 3,
        currentMonth.atDay(5) to 4,
        currentMonth.atDay(6) to 5,
        currentMonth.atDay(7) to 6
    )

    TodoAppTheme {
        MonthCalendar(
            selectedDate = currentMonth.atDay(22),
            startMonth = currentMonth.minusMonths(12),
            endMonth = currentMonth.plusMonths(12),
            todoCountsByDate = sampleCounts,
            onDateSelected = {}
        )
    }
}
