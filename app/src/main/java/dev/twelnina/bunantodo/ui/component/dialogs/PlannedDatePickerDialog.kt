package dev.twelnina.bunantodo.ui.component.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.getSelectedDate
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.twelnina.bunantodo.R
import dev.twelnina.bunantodo.ui.rememberYearMonthDayFormatter
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

@Composable
internal fun PlannedDatePickerDialog(
    today: LocalDate,
    previousDate: LocalDate?,
    onConfirmRequest: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDate = null,
        initialDisplayedMonth = YearMonth.from(today),
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

    val newDate = datePickerState.getSelectedDate()

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                enabled = newDate != null && newDate >= today,
                onClick = {
                    newDate?.let(onConfirmRequest)
                }
            ) { Text(stringResource(R.string.common_action_ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.common_action_cancel))
            }
        }
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                DateChangeSummary(
                    previousDate = previousDate,
                    newDate = newDate,
                )
                HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
            }
            DatePicker(
                state = datePickerState,
                modifier = Modifier.fillMaxWidth(),
                title = null,
                headline = null,
                showModeToggle = false
            )
        }
    }
}

@Composable
private fun DateChangeSummary(
    previousDate: LocalDate?,
    newDate: LocalDate?,
    modifier: Modifier = Modifier,
) {
    val yearMonthDayFormatter = rememberYearMonthDayFormatter()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DateSummaryItem(
            label = stringResource(R.string.planned_date_dialog_label_previous),
            date = previousDate?.format(yearMonthDayFormatter)
                ?: stringResource(R.string.planned_date_dialog_status_unscheduled),
            modifier = Modifier.weight(1f),
        )

        Icon(
            painter = painterResource(R.drawable.arrow_right_alt_24px),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        DateSummaryItem(
            label = stringResource(R.string.planned_date_dialog_label_new),
            date = newDate?.format(yearMonthDayFormatter)
                ?: stringResource(R.string.planned_date_dialog_status_unscheduled),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DateSummaryItem(
    label: String,
    date: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = date,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
