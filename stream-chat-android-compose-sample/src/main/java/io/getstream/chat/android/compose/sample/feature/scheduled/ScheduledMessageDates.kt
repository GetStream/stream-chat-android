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

import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Why a date can't be picked to schedule a message.
 */
internal enum class ScheduleDateError {
    /** Less than a minute away: the server would send the message right away. */
    TooSoon,

    /** Further away than the server allows by default. */
    TooFar,
}

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
 * The quick times to schedule a message: tomorrow at 9:00 and at 13:00, and the next Monday at 9:00 after tomorrow.
 *
 * @param now The current date.
 * @param timeZone The time zone of the dates.
 */
internal fun scheduleMessagePresets(now: Date, timeZone: TimeZone = TimeZone.getDefault()): List<Date> {
    val tomorrowMorning = Calendar.getInstance(timeZone).apply {
        time = now
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, MorningHour)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val tomorrowAfternoon = (tomorrowMorning.clone() as Calendar).apply { set(Calendar.HOUR_OF_DAY, AfternoonHour) }
    val nextMondayMorning = (tomorrowMorning.clone() as Calendar).apply {
        do {
            add(Calendar.DAY_OF_YEAR, 1)
        } while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
    }
    return listOf(tomorrowMorning.time, tomorrowAfternoon.time, nextMondayMorning.time)
}

/**
 * Why [date] can't be picked to schedule a message, or null when it can. The server sends a message with a past date
 * right away, and by default rejects a date more than 120 days away.
 *
 * @param now The current date.
 */
internal fun scheduleDateError(date: Date, now: Date): ScheduleDateError? = when {
    date.time < now.time + TimeUnit.MINUTES.toMillis(1) -> ScheduleDateError.TooSoon
    date.time > now.time + TimeUnit.DAYS.toMillis(MaxLeadDays) -> ScheduleDateError.TooFar
    else -> null
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

private const val MorningHour = 9
private const val AfternoonHour = 13
private const val MaxLeadDays = 120L
private const val DaysInWeek = 7
