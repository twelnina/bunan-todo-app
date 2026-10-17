package dev.twelnina.bunantodo.ui.editor.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.getSelectedDate
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.twelnina.bunantodo.R
import dev.twelnina.bunantodo.ui.theme.TodoAppTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun PlannedDatePicker(
    today: LocalDate,
    selectedDate: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDate = selectedDate,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = Instant
                    .ofEpochMilli(utcTimeMillis)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()

                return date >= today
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year >= today.year
            }
        }
    )
    val pickedDate = datePickerState.getSelectedDate()

    LaunchedEffect(pickedDate) {
        if (pickedDate != selectedDate) {
            onDateChange(pickedDate)
        }
    }


    DatePicker(
        state = datePickerState,
        modifier = modifier.clip(MaterialTheme.shapes.large),
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        title = null,
        headline = {
            ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                val headlineModifier = Modifier.padding(
                    start = 24.dp, top = 16.dp, end = 12.dp, bottom = 12.dp
                )

                if (datePickerState.selectedDateMillis == null) {
                    Text(
                        text = stringResource(R.string.planned_date_picker_status_unscheduled),
                        modifier = headlineModifier,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    DatePickerDefaults.DatePickerHeadline(
                        selectedDateMillis = datePickerState.selectedDateMillis,
                        displayMode = datePickerState.displayMode,
                        dateFormatter = DatePickerDefaults.dateFormatter(),
                        modifier = Modifier.padding(
                            start = 24.dp,
                            top = 16.dp,
                            end = 12.dp,
                            bottom = 12.dp
                        ),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        showModeToggle = false
    )
}


@Preview
@Composable
private fun PlannedDatePickerPreview() {
    TodoAppTheme {
        PlannedDatePicker(
            today = LocalDate.of(2026, 9, 23),
            selectedDate = LocalDate.of(2026, 9, 23),
            onDateChange = {}
        )
    }
}
