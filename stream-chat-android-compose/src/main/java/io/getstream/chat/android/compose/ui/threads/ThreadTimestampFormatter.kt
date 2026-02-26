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

package io.getstream.chat.android.compose.ui.threads

import android.content.Context
import android.text.format.DateUtils
import io.getstream.chat.android.compose.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Formats [io.getstream.chat.android.models.Thread.updatedAt] timestamps for display in [ThreadItem].
 *
 * Formatting rules:
 * - Within the last minute → "Just now"
 * - Same calendar day → "Today at <time>"
 * - Previous calendar day → "Yesterday at <time>"
 * - Within the last 7 days → "<Day of week> at <time>" (e.g. "Monday at 2:30 PM")
 * - Older → "<Locale date> at <time>" (e.g. "Jan 1 at 9:00 AM")
 *
 * All labels are sourced from string resources to support localization.
 * The time format respects the device's 12/24-hour setting via [DateFormat.getTimeFormat].
 * Day and date strings use Android's [DateUtils] for full locale awareness.
 *
 * The [now] parameter defaults to the current [Date] and can be overridden in tests.
 */
internal object ThreadTimestampFormatter {

    private const val JUST_NOW_THRESHOLD_MS = 60_000L
    private const val WEEK_IN_MS = 7L * 24 * 60 * 60 * 1000

    fun format(date: Date, context: Context, now: Date = Date()): String {
        val nowCal = Calendar.getInstance().apply { time = now }
        val dateCal = Calendar.getInstance().apply { time = date }
        val elapsedMs = now.time - date.time

        val time = formatTime(date)
        return when {
            abs(elapsedMs) < JUST_NOW_THRESHOLD_MS ->
                context.getString(R.string.stream_compose_thread_timestamp_just_now)
            elapsedMs < 0 ->
                time // genuinely far future — show just the time (should not happen)
            isSameDay(nowCal, dateCal) ->
                context.getString(R.string.stream_compose_thread_timestamp_today, time)
            isYesterday(nowCal, dateCal) ->
                context.getString(R.string.stream_compose_thread_timestamp_yesterday, time)
            elapsedMs < WEEK_IN_MS -> {
                val dayName = dayOfWeek(date)
                context.getString(R.string.stream_compose_thread_timestamp_day_of_week, dayName, time)
            }
            else -> {
                // TODO: Consider using localized months
                val dateStr = DateUtils.formatDateTime(
                    context,
                    date.time,
                    DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH,
                )
                context.getString(R.string.stream_compose_thread_timestamp_date, dateStr, time)
            }
        }
    }

    private fun formatTime(date: Date): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)

    private fun isSameDay(a: Calendar, b: Calendar): Boolean =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

    private fun isYesterday(now: Calendar, date: Calendar): Boolean {
        val yesterday = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        return isSameDay(yesterday, date)
    }

    /** Full locale-aware day name (e.g. "Monday", "Montag", "lundi"). */
    // TODO: Consider using localized days of week
    private fun dayOfWeek(date: Date): String =
        SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
}
