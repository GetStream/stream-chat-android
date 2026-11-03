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

package io.getstream.chat.android.ai.a2ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.getstream.chat.android.ai.a2ui.R
import io.getstream.chat.android.ai.a2ui.compose.icons.A2UIIcons
import io.getstream.chat.android.ai.a2ui.core.A2UIDates
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

/**
 * `DateTimeInput`: a field that opens a date picker, a time picker, or both, as `enableDate` and
 * `enableTime` say, within `min` and `max`. `value` is a two-way binding in ISO 8601: a date
 * (`2026-10-02`), a time (`19:00:00`), or a date and time with its offset. It shows in the
 * device's own format.
 *
 * The whole field is one button: it opens the picker on a tap, with the keyboard, TalkBack and
 * Switch Access.
 */
@Composable
internal fun A2UIDateTimeInput(scope: A2UIComponentScope) {
    val raw = scope.string("value")
    val enableTime = scope.boolean("enableTime") == true
    // With neither flag set, the input picks a date.
    val enableDate = scope.boolean("enableDate") == true || !enableTime
    val kind = when {
        enableDate && enableTime -> DateTimeKind.DateTime
        enableTime -> DateTimeKind.Time
        else -> DateTimeKind.Date
    }
    val label = scope.string("label")
    val deviceZone = remember { TimeZone.getDefault() }
    val parsed = remember(raw) { raw?.let { A2UIDates.parse(it, deviceZone) } }
    val zone = parsed?.timeZone ?: deviceZone
    val shown = parsed?.let { DateTimeValues.display(it.millis, zone, kind) } ?: raw.orEmpty()
    val initial = Calendar.getInstance(zone).apply { parsed?.let { timeInMillis = it.millis } }
    var picker by rememberSaveable { mutableStateOf(Picker.None) }
    var pickedDay by rememberSaveable { mutableStateOf<Long?>(null) }

    Column(modifier = scope.modifier.fillMaxWidth().padding(LeafMargin)) {
        DateTimeField(label, shown, kind) { picker = if (kind == DateTimeKind.Time) Picker.Time else Picker.Date }
        ValidationMessages(scope.validationErrors)
    }
    fun close() {
        pickedDay = null
        picker = Picker.None
    }
    when (picker) {
        Picker.Date -> DateDialog(
            initialDay = DateTimeValues.utcMidnight(initial),
            min = scope.string("min")?.let { A2UIDates.parse(it, deviceZone) },
            max = scope.string("max")?.let { A2UIDates.parse(it, deviceZone) },
            onDismiss = ::close,
        ) { day ->
            if (kind == DateTimeKind.DateTime) {
                pickedDay = day
                picker = Picker.Time
            } else {
                scope.write("value", DateTimeValues.format(day, 0, 0, zone, kind))
                close()
            }
        }
        Picker.Time -> TimeDialog(label, initial, onDismiss = ::close) { hour, minute ->
            val day = if (kind == DateTimeKind.DateTime) pickedDay ?: DateTimeValues.utcMidnight(initial) else null
            scope.write("value", DateTimeValues.format(day, hour, minute, zone, kind))
            close()
        }
        Picker.None -> Unit
    }
}

// The field as a whole is one button that opens the picker; the text field only shows the value.
@Composable
private fun DateTimeField(label: String?, shown: String, kind: DateTimeKind, onClick: () -> Unit) {
    Box {
        // readOnly, rather than disabled, keeps an input's colors.
        OutlinedTextField(
            value = shown,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = label?.let { { Text(it) } },
            trailingIcon = {
                val icon = if (kind == DateTimeKind.Time) "event" else "calendarToday"
                Icon(A2UIIcons.vector(icon), contentDescription = null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusProperties { canFocus = false }
                .clearAndSetSemantics {},
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(role = Role.Button, onClick = onClick)
                .semantics { contentDescription = listOfNotNull(label, shown.ifEmpty { null }).joinToString(", ") },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateDialog(
    initialDay: Long,
    min: A2UIDates.ZonedMillis?,
    max: A2UIDates.ZonedMillis?,
    onDismiss: () -> Unit,
    onPick: (dayUtcMillis: Long) -> Unit,
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDay,
        selectableDates = DateTimeValues.selectable(
            min?.let { DateTimeValues.utcMidnight(it.millis, it.timeZone) },
            max?.let { DateTimeValues.utcMidnight(it.millis, it.timeZone) },
        ),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onPick(state.selectedDateMillis ?: initialDay) }) {
                Text(stringResource(R.string.stream_ai_a2ui_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.stream_ai_a2ui_cancel)) }
        },
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDialog(
    label: String?,
    initial: Calendar,
    onDismiss: () -> Unit,
    onPick: (hour: Int, minute: Int) -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initial.get(Calendar.HOUR_OF_DAY),
        initialMinute = initial.get(Calendar.MINUTE),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = label?.let { { Text(it) } },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onPick(state.hour, state.minute) }) {
                Text(stringResource(R.string.stream_ai_a2ui_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.stream_ai_a2ui_cancel)) }
        },
    )
}

private enum class Picker { None, Date, Time }

/** What a `DateTimeInput` picks: a date, a time, or both. */
internal enum class DateTimeKind { Date, Time, DateTime }

/** Reads and writes the values of a `DateTimeInput`, without `java.time`, which the minimum SDK lacks. */
internal object DateTimeValues {
    private const val MINUTES_PER_HOUR = 60
    private const val MILLIS_PER_MINUTE = 60_000

    /** [millis] as a value of [kind], shown in the device's format. */
    fun display(millis: Long, zone: TimeZone, kind: DateTimeKind): String {
        val format = when (kind) {
            DateTimeKind.DateTime -> DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
            DateTimeKind.Time -> DateFormat.getTimeInstance(DateFormat.SHORT)
            DateTimeKind.Date -> DateFormat.getDateInstance(DateFormat.MEDIUM)
        }
        format.timeZone = zone
        return format.format(Date(millis))
    }

    /**
     * The value of [kind] for a picked day (midnight UTC, as the date picker gives it) and time,
     * in [zone]: a date and time with the zone's offset, a time, or a date.
     */
    fun format(dayUtcMillis: Long?, hour: Int, minute: Int, zone: TimeZone, kind: DateTimeKind): String {
        val day = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = dayUtcMillis ?: 0L }
        val year = day.get(Calendar.YEAR)
        val month = day.get(Calendar.MONTH)
        val dayOfMonth = day.get(Calendar.DAY_OF_MONTH)
        val date = "%04d-%02d-%02d".format(Locale.ROOT, year, month + 1, dayOfMonth)
        val time = "%02d:%02d:00".format(Locale.ROOT, hour, minute)
        return when (kind) {
            DateTimeKind.DateTime -> {
                val local = Calendar.getInstance(zone).apply {
                    clear()
                    set(year, month, dayOfMonth, hour, minute, 0)
                }
                "${date}T$time${offset(zone, local.timeInMillis)}"
            }
            DateTimeKind.Time -> time
            DateTimeKind.Date -> date
        }
    }

    /** Midnight UTC of the day [calendar] is on, in its own zone, as the date picker reads days. */
    fun utcMidnight(calendar: Calendar): Long = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis

    /** Midnight UTC of the day [millis] is on in [zone]. */
    fun utcMidnight(millis: Long, zone: TimeZone): Long =
        utcMidnight(Calendar.getInstance(zone).apply { timeInMillis = millis })

    /** The days a date picker may pick, between [min] and [max] (midnight UTC), when given. */
    @OptIn(ExperimentalMaterial3Api::class)
    fun selectable(min: Long?, max: Long?): SelectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean =
            (min == null || utcTimeMillis >= min) && (max == null || utcTimeMillis <= max)
    }

    private fun offset(zone: TimeZone, millis: Long): String {
        val minutes = zone.getOffset(millis) / MILLIS_PER_MINUTE
        if (minutes == 0) return "Z"
        val sign = if (minutes < 0) "-" else "+"
        return "%s%02d:%02d".format(Locale.ROOT, sign, abs(minutes) / MINUTES_PER_HOUR, abs(minutes) % MINUTES_PER_HOUR)
    }
}
