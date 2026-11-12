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
import io.getstream.chat.android.compose.sample.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * How far a scheduled date is from today, which decides how the day is written.
 */
internal enum class ScheduledDay {
    Today,
    Tomorrow,

    /** From 2 to 6 days from today: the name of the day is enough. */
    ThisWeek,
    ThisYear,
    OtherYear,
}

/**
 * How far [date] is from [now], in calendar days of [timeZone].
 */
internal fun scheduledDay(date: Date, now: Date, timeZone: TimeZone = TimeZone.getDefault()): ScheduledDay {
    val day = Calendar.getInstance(timeZone).apply { time = date }
    val today = Calendar.getInstance(timeZone).apply { time = now }
    val days = TimeUnit.MILLISECONDS.toDays(day.startOfDay() - today.startOfDay())
    return when {
        days == 0L -> ScheduledDay.Today
        days == 1L -> ScheduledDay.Tomorrow
        days in 2 until DaysInWeek -> ScheduledDay.ThisWeek
        day.get(Calendar.YEAR) == today.get(Calendar.YEAR) -> ScheduledDay.ThisYear
        else -> ScheduledDay.OtherYear
    }
}

/**
 * The start of the day in milliseconds, shifted by the zone offset so that days differ by whole multiples of 24 hours,
 * also across a daylight saving change.
 */
private fun Calendar.startOfDay(): Long {
    val start = (clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return start.timeInMillis + start.get(Calendar.ZONE_OFFSET) + start.get(Calendar.DST_OFFSET)
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

private const val DaysInWeek = 7
