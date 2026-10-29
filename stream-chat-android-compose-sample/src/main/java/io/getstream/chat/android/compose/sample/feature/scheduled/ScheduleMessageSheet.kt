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

package io.getstream.chat.android.compose.sample.feature.scheduled

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import io.getstream.chat.android.compose.R as ComposeR

/**
 * A bottom sheet with quick times to schedule a message, and a row that opens the custom time.
 *
 * @param onDateSelected Called with the picked date. The sheet is dismissed first.
 * @param onCustomTimeClick Called to pick a custom date and time instead, in the sheet that replaces this one.
 * @param onDismiss Called when the sheet is dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduleMessageSheet(
    onDateSelected: (Date) -> Unit,
    onCustomTimeClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val presets = remember { scheduleMessagePresets(Date()) }
    ModalBottomSheet(
        modifier = Modifier.testTag("Stream_ScheduleMessageSheet"),
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = ChatTheme.colors.backgroundCoreElevation1,
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            SheetTitle(stringResource(R.string.scheduled_messages_schedule_title))
            presets.forEachIndexed { index, date ->
                SheetItem(
                    modifier = Modifier.testTag("Stream_ScheduleMessagePreset$index"),
                    icon = painterResource(ComposeR.drawable.stream_design_ic_clock),
                    text = context.formatScheduledDate(date),
                    onClick = {
                        onDismiss()
                        onDateSelected(date)
                    },
                )
            }
            SheetItem(
                modifier = Modifier.testTag("Stream_ScheduleMessageCustomTime"),
                icon = painterResource(ComposeR.drawable.stream_design_ic_clock),
                text = stringResource(R.string.scheduled_messages_custom_time),
                trailingIcon = painterResource(ComposeR.drawable.stream_design_ic_chevron_right),
                onClick = onCustomTimeClick,
            )
        }
    }
}

/**
 * A bottom sheet to pick the date and the time when a message is sent, for a custom time or a reschedule. The confirm
 * button is disabled while the picked time is less than a minute or more than 120 days away.
 *
 * @param title The title of the sheet.
 * @param confirmLabel The label of the confirm button.
 * @param previewTitle The first line of the message preview, for example who the message is for.
 * @param previewText The text of the message.
 * @param initialDate The date the pickers start at.
 * @param note The first line of the note under the pickers, or null to show only the time zone.
 * @param isOffline Whether the device is offline, to tell that the message is scheduled once online.
 * @param onConfirm Called with the picked date. The sheet is dismissed first.
 * @param onDismiss Called when the sheet is dismissed.
 */
@Suppress("LongMethod", "LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduleTimeSheet(
    title: String,
    confirmLabel: String,
    previewTitle: String,
    previewText: String,
    initialDate: Date,
    note: String?,
    isOffline: Boolean,
    onConfirm: (Date) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var date by remember { mutableStateOf(initialDate) }
    var isDatePickerVisible by remember { mutableStateOf(false) }
    var isTimePickerVisible by remember { mutableStateOf(false) }
    var now by remember { mutableStateOf(Date()) }
    // The picked time can become too soon while the sheet is open.
    LaunchedEffect(Unit) {
        while (true) {
            delay(NowRefreshMs)
            now = Date()
        }
    }
    val error = scheduleDateError(date, now)
    ModalBottomSheet(
        modifier = Modifier.testTag("Stream_ScheduleTimeSheet"),
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = ChatTheme.colors.backgroundCoreElevation1,
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SheetTitle(title)
            MessagePreview(title = previewTitle, text = previewText)
            when {
                error != null -> SheetNote(
                    modifier = Modifier.testTag("Stream_ScheduleTimeError"),
                    icon = painterResource(ComposeR.drawable.stream_design_ic_exclamation_circle),
                    title = stringResource(error.message),
                    color = ChatTheme.colors.accentError,
                )
                isOffline -> SheetNote(
                    icon = painterResource(ComposeR.drawable.stream_design_ic_info),
                    title = stringResource(R.string.scheduled_messages_offline_note),
                    color = ChatTheme.colors.accentPrimary,
                )
            }
            PickerRow(
                modifier = Modifier.testTag("Stream_ScheduleTimeDate"),
                icon = rememberVectorPainter(Icons.Outlined.CalendarToday),
                label = stringResource(R.string.scheduled_messages_date),
                value = context.formatPickerDate(date),
                onClick = { isDatePickerVisible = true },
            )
            PickerRow(
                modifier = Modifier.testTag("Stream_ScheduleTimeTime"),
                icon = painterResource(ComposeR.drawable.stream_design_ic_clock),
                label = stringResource(R.string.scheduled_messages_time),
                value = DateFormat.getTimeFormat(context).format(date),
                onClick = { isTimePickerVisible = true },
            )
            SheetNote(
                icon = painterResource(ComposeR.drawable.stream_design_ic_info),
                title = note ?: stringResource(R.string.scheduled_messages_time_zone, timeZoneName(date)),
                subtitle = note?.let { stringResource(R.string.scheduled_messages_time_zone, timeZoneName(date)) },
                color = ChatTheme.colors.accentPrimary,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                DialogCancelButton(onDismiss)
                Button(
                    modifier = Modifier.testTag("Stream_ScheduleTimeConfirm"),
                    enabled = error == null,
                    colors = ButtonDefaults.buttonColors(containerColor = ChatTheme.colors.accentPrimary),
                    onClick = {
                        onDismiss()
                        onConfirm(date)
                    },
                ) {
                    Text(text = confirmLabel)
                }
            }
        }
    }
    if (isDatePickerVisible) {
        PickDateDialog(
            date = date,
            onDatePicked = { date = it },
            onDismiss = { isDatePickerVisible = false },
        )
    }
    if (isTimePickerVisible) {
        PickTimeDialog(
            date = date,
            onTimePicked = { date = it },
            onDismiss = { isTimePickerVisible = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickDateDialog(date: Date, onDatePicked: (Date) -> Unit, onDismiss: () -> Unit) {
    val utc = TimeZone.getTimeZone("UTC")
    val local = Calendar.getInstance().apply { time = date }
    // The date picker works in UTC days.
    val selected = Calendar.getInstance(utc).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }
    val today = Calendar.getInstance()
    val todayInUtc = Calendar.getInstance(utc).apply {
        clear()
        set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
    val state = rememberDatePickerState(
        initialSelectedDateMillis = selected.timeInMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= todayInUtc
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
        onDismissRequest = onDismiss,
        colors = colors,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val picked = Calendar.getInstance(utc).apply { timeInMillis = millis }
                        local.set(
                            picked.get(Calendar.YEAR),
                            picked.get(Calendar.MONTH),
                            picked.get(Calendar.DAY_OF_MONTH),
                        )
                        onDatePicked(local.time)
                    }
                    onDismiss()
                },
            ) {
                Text(text = stringResource(R.string.scheduled_messages_ok), color = ChatTheme.colors.accentPrimary)
            }
        },
        dismissButton = { DialogCancelButton(onDismiss) },
    ) {
        DatePicker(state = state, colors = colors)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickTimeDialog(date: Date, onTimePicked: (Date) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { time = date }
    val state = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = DateFormat.is24HourFormat(context),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.scheduled_messages_enter_time)) },
        text = {
            TimeInput(
                state = state,
                colors = TimePickerDefaults.colors(
                    timeSelectorSelectedContainerColor = ChatTheme.colors.accentPrimary.copy(alpha = 0.12f),
                    timeSelectorSelectedContentColor = ChatTheme.colors.accentPrimary,
                    periodSelectorSelectedContainerColor = ChatTheme.colors.accentPrimary.copy(alpha = 0.12f),
                    periodSelectorSelectedContentColor = ChatTheme.colors.accentPrimary,
                ),
            )
        },
        containerColor = ChatTheme.colors.backgroundCoreElevation1,
        titleContentColor = ChatTheme.colors.textPrimary,
        confirmButton = {
            TextButton(
                onClick = {
                    calendar.set(Calendar.HOUR_OF_DAY, state.hour)
                    calendar.set(Calendar.MINUTE, state.minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    onTimePicked(calendar.time)
                    onDismiss()
                },
            ) {
                Text(text = stringResource(R.string.scheduled_messages_ok), color = ChatTheme.colors.accentPrimary)
            }
        },
        dismissButton = { DialogCancelButton(onDismiss) },
    )
}

@Composable
private fun DialogCancelButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(text = stringResource(R.string.scheduled_messages_cancel), color = ChatTheme.colors.accentPrimary)
    }
}

@Composable
private fun SheetTitle(text: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        text = text,
        style = ChatTheme.typography.headingSmall,
        color = ChatTheme.colors.textPrimary,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun SheetItem(
    icon: Painter,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: Painter? = null,
    color: Color = ChatTheme.colors.textPrimary,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = null,
                indication = ripple(),
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(modifier = Modifier.size(20.dp), painter = icon, contentDescription = null, tint = color)
        Text(modifier = Modifier.weight(1f), text = text, style = ChatTheme.typography.bodyDefault, color = color)
        trailingIcon?.let {
            Icon(modifier = Modifier.size(20.dp), painter = it, contentDescription = null, tint = color)
        }
    }
}

/**
 * The message a time is picked for: who it is for, or when it is scheduled, and its text.
 */
@Composable
internal fun MessagePreview(title: String, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ChatTheme.colors.backgroundCoreSurfaceDefault)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(32.dp)
                .background(ChatTheme.colors.accentPrimary),
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = title,
                style = ChatTheme.typography.captionEmphasis,
                color = ChatTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = text,
                style = ChatTheme.typography.captionDefault,
                color = ChatTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PickerRow(
    icon: Painter,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = null,
                indication = ripple(),
                onClick = onClick,
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = icon,
            contentDescription = null,
            tint = ChatTheme.colors.textPrimary,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = label,
            style = ChatTheme.typography.bodyDefault,
            color = ChatTheme.colors.textPrimary,
        )
        Text(text = value, style = ChatTheme.typography.bodyDefault, color = ChatTheme.colors.textSecondary)
    }
}

@Composable
private fun SheetNote(
    icon: Painter,
    title: String,
    color: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(modifier = Modifier.size(16.dp), painter = icon, contentDescription = null, tint = color)
        Column {
            Text(text = title, style = ChatTheme.typography.captionEmphasis, color = ChatTheme.colors.textPrimary)
            subtitle?.let {
                Text(text = it, style = ChatTheme.typography.captionDefault, color = ChatTheme.colors.textSecondary)
            }
        }
    }
}

private val ScheduleDateError.message: Int
    get() = when (this) {
        ScheduleDateError.TooSoon -> R.string.scheduled_messages_time_too_soon
        ScheduleDateError.TooFar -> R.string.scheduled_messages_time_too_far
    }

/**
 * Formats [date] as "Today at 9:00", "Tomorrow at 9:00", "Monday at 9:00" within a week, or with the month and day.
 */
internal fun Context.formatScheduledDate(date: Date, now: Date = Date()): String =
    getString(
        R.string.scheduled_messages_date_at_time,
        formatScheduledDay(date, now),
        DateFormat.getTimeFormat(this).format(date),
    )

/**
 * Formats the day of [date] as "Today", "Tomorrow", the name of the day within a week, or with the month and day.
 */
internal fun Context.formatScheduledDay(date: Date, now: Date = Date()): String {
    val locale = Locale.getDefault()
    return when (scheduledDay(date, now)) {
        ScheduledDay.Today -> getString(R.string.scheduled_messages_today)
        ScheduledDay.Tomorrow -> getString(R.string.scheduled_messages_tomorrow)
        ScheduledDay.ThisWeek -> SimpleDateFormat("EEEE", locale).format(date)
        ScheduledDay.ThisYear -> SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "EEEMMMd"), locale)
            .format(date)
        ScheduledDay.OtherYear -> SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "EEEMMMdy"), locale)
            .format(date)
    }
}

private fun Context.formatPickerDate(date: Date): String {
    val locale = Locale.getDefault()
    return SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "EEEMMMd"), locale).format(date)
}

private fun timeZoneName(date: Date): String {
    val timeZone = TimeZone.getDefault()
    return timeZone.getDisplayName(timeZone.inDaylightTime(date), TimeZone.SHORT, Locale.getDefault())
}

private const val NowRefreshMs = 1_000L
