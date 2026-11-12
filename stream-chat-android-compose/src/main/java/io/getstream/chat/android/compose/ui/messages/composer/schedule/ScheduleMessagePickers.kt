/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.compose.ui.messages.composer.schedule

import android.text.format.DateFormat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * The Material date picker, to pick the day a message is scheduled at. The time of [date] is kept. Days before today
 * and after [maxDate] can't be picked.
 *
 * @param date The date the picker starts at.
 * @param maxDate The last date a message can be scheduled at.
 * @param onDatePicked Called with [date] moved to the picked day.
 * @param onDismiss Called when the picker is closed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduleMessageDatePicker(
    date: Date,
    maxDate: Date,
    onDatePicked: (Date) -> Unit,
    onDismiss: () -> Unit,
) {
    // The date picker works with days at midnight UTC.
    val firstDay = remember { Date().toUtcDay() }
    val lastDay = remember(maxDate) { maxDate.toUtcDay() }
    val state = rememberDatePickerState(
        initialSelectedDateMillis = date.toUtcDay(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis in firstDay..lastDay

            override fun isSelectableYear(year: Int): Boolean =
                year in Date(firstDay).utcYear()..Date(lastDay).utcYear()
        },
    )
    val colors = DatePickerDefaults.colors(
        containerColor = ChatTheme.colors.backgroundCoreElevation1,
        selectedDayContainerColor = ChatTheme.colors.accentPrimary,
        selectedDayContentColor = ChatTheme.colors.textOnAccent,
        todayDateBorderColor = ChatTheme.colors.accentPrimary,
        todayContentColor = ChatTheme.colors.accentPrimary,
        selectedYearContainerColor = ChatTheme.colors.accentPrimary,
        selectedYearContentColor = ChatTheme.colors.textOnAccent,
    )
    DatePickerDialog(
        modifier = Modifier.testTag("Stream_ScheduleMessageDatePicker"),
        onDismissRequest = onDismiss,
        colors = colors,
        confirmButton = {
            DialogButton(text = stringResource(R.string.stream_compose_schedule_message_ok)) {
                state.selectedDateMillis?.let { utcDay -> onDatePicked(date.withDay(utcDay)) }
                onDismiss()
            }
        },
        dismissButton = {
            DialogButton(text = stringResource(R.string.stream_compose_schedule_message_cancel), onClick = onDismiss)
        },
    ) {
        DatePicker(state = state, colors = colors)
    }
}

/**
 * The Material time input, to pick the time a message is scheduled at, on the day of [date].
 *
 * @param date The date the picker starts at.
 * @param onTimePicked Called with [date] moved to the picked time.
 * @param onDismiss Called when the picker is closed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduleMessageTimePicker(
    date: Date,
    onTimePicked: (Date) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { time = date }
    val state = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = DateFormat.is24HourFormat(context),
    )
    AlertDialog(
        modifier = Modifier.testTag("Stream_ScheduleMessageTimePicker"),
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.stream_compose_schedule_message_enter_time)) },
        text = {
            TimeInput(
                state = state,
                colors = TimePickerDefaults.colors(
                    timeSelectorSelectedContainerColor = ChatTheme.colors.backgroundUtilitySelected,
                    timeSelectorSelectedContentColor = ChatTheme.colors.accentPrimary,
                    periodSelectorSelectedContainerColor = ChatTheme.colors.backgroundUtilitySelected,
                    periodSelectorSelectedContentColor = ChatTheme.colors.accentPrimary,
                ),
            )
        },
        containerColor = ChatTheme.colors.backgroundCoreElevation1,
        titleContentColor = ChatTheme.colors.textPrimary,
        confirmButton = {
            DialogButton(text = stringResource(R.string.stream_compose_schedule_message_ok)) {
                calendar.set(Calendar.HOUR_OF_DAY, state.hour)
                calendar.set(Calendar.MINUTE, state.minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                onTimePicked(calendar.time)
                onDismiss()
            }
        },
        dismissButton = {
            DialogButton(text = stringResource(R.string.stream_compose_schedule_message_cancel), onClick = onDismiss)
        },
    )
}

@Composable
private fun DialogButton(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(text = text, color = ChatTheme.colors.accentPrimary)
    }
}

/**
 * The local day of the date, as midnight UTC of the same calendar day.
 */
private fun Date.toUtcDay(): Long {
    val local = Calendar.getInstance().apply { time = this@toUtcDay }
    return Calendar.getInstance(Utc).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

private fun Date.utcYear(): Int = Calendar.getInstance(Utc).apply { time = this@utcYear }.get(Calendar.YEAR)

/**
 * The date moved to the calendar day of [utcDay], keeping its local time.
 */
private fun Date.withDay(utcDay: Long): Date {
    val picked = Calendar.getInstance(Utc).apply { timeInMillis = utcDay }
    return Calendar.getInstance().apply {
        time = this@withDay
        set(picked.get(Calendar.YEAR), picked.get(Calendar.MONTH), picked.get(Calendar.DAY_OF_MONTH))
    }.time
}

private val Utc = TimeZone.getTimeZone("UTC")
