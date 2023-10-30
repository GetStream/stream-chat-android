/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.common.helper

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import io.getstream.chat.android.ui.common.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * An interface that allows to format date-time objects as strings.
 */
public interface DateFormatter {

    /**
     * Formats the given date as a String.
     *
     * @param date The [Date] to format as a String.
     * @return The formatted date-time string.
     */
    public fun formatDate(date: Date?): String

    /**
     * Formats the given time as a String.
     *
     * @param date The [Date] object to format as a String.
     * @return The formatted time string.
     */
    public fun formatTime(date: Date?): String

    public companion object {
        /**
         * Builds the default date formatter.
         *
         * @param context The context of the application.
         * @param locale The locale to use for formatting.
         * @return The default implementation of [DateFormatter].
         */
        @JvmStatic
        @JvmOverloads
        public fun from(context: Context, locale: Locale = Locale.getDefault()): DateFormatter = DefaultDateFormatter(
            context,
            locale,
        )
    }
}

/**
 * The default implementation of [DateFormatter].
 */
internal class DefaultDateFormatter(
    private val dateContext: DateContext,
    private val locale: Locale,
) : DateFormatter {

    constructor(context: Context, locale: Locale) : this(DefaultDateContext(context, locale), locale)

    private val timeFormatter12h: SimpleDateFormat = SimpleDateFormat("h:mm a", locale)
    private val timeFormatter24h: SimpleDateFormat = SimpleDateFormat("HH:mm", locale)
    private val dateFormatterDayOfWeek: SimpleDateFormat = SimpleDateFormat("EEEE", locale)
    private val dateFormatterFullDate: SimpleDateFormat
        // Re-evaluated every time to account for runtime Locale changes
        get() = SimpleDateFormat(dateContext.dateTimePattern(), locale)

    /**
     * Formats the given date as a String.
     *
     * @param date The [Date] to format as a String.
     * @return The formatted date-time string.
     */
    override fun formatDate(date: Date?): String {
        date ?: return ""

        return when {
            date.isToday() -> formatTime(date)
            date.isYesterday() -> dateContext.yesterdayString()
            date.isWithinLastWeek() -> dateFormatterDayOfWeek.format(date)
            else -> dateFormatterFullDate.format(date)
        }
    }

    /**
     * Formats the given time as a String.
     *
     * @param date The [Date] object to format as a String.
     * @return The formatted time string.
     */
    override fun formatTime(date: Date?): String {
        date ?: return ""

        val dateFormat = if (dateContext.is24Hour()) timeFormatter24h else timeFormatter12h
        return dateFormat.format(date)
    }

    /**
     * Checks if the supplied day is today.
     *
     * @return true if the date is today.
     */
    private fun Date.isToday(): Boolean {
        val calendar1 = Calendar.getInstance().also { it.time = dateContext.now() }
        val calendar2 = Calendar.getInstance().also { it.time = this }

        return (calendar1[Calendar.YEAR] == calendar2[Calendar.YEAR]) &&
            calendar1[Calendar.DAY_OF_YEAR] == calendar2[Calendar.DAY_OF_YEAR]
    }

    /**
     * Checks if the supplied date is yesterday.
     *
     * @return True if the date is yesterday.
     */
    private fun Date.isYesterday(): Boolean {
        return Date(time + DateUtils.DAY_IN_MILLIS).isToday()
    }

    /**
     * Checks if the supplied date is within last week.
     *
     * @return True is the date is within last week.
     */
    private fun Date.isWithinLastWeek(): Boolean {
        return isWithinDays(DAYS_IN_WEEK - 1)
    }

    /**
     * Checks if the supplied date is before today and within a number of days in the past.
     *
     * @param days The number of days before the current date.
     * @return True is the date is within x days in the past.
     */
    private fun Date.isWithinDays(days: Int): Boolean {
        val calendar: Calendar = Calendar.getInstance().also { it.time = this }

        val currentDate = dateContext.now()
        val start: Calendar = Calendar.getInstance().also {
            it.time = currentDate
            it.add(Calendar.DAY_OF_YEAR, -days)
        }
        val end: Calendar = Calendar.getInstance().also { it.time = currentDate }

        return calendar.isBeforeDay(end) && !calendar.isBeforeDay(start)
    }

    /**
     * Checks if the calendar date is before another calendar date ignoring time.
     *
     * @return True if the calendar date is before another calendar date ignoring time.
     */
    private fun Calendar.isBeforeDay(calendar: Calendar): Boolean {
        return when {
            this[Calendar.YEAR] < calendar[Calendar.YEAR] -> true
            this[Calendar.YEAR] > calendar[Calendar.YEAR] -> false
            else -> this[Calendar.DAY_OF_YEAR] < calendar[Calendar.DAY_OF_YEAR]
        }
    }

    interface DateContext {
        fun now(): Date
        fun yesterdayString(): String
        fun is24Hour(): Boolean
        fun dateTimePattern(): String
    }

    private class DefaultDateContext(
        private val context: Context,
        private val locale: Locale,
    ) : DateContext {
        override fun now(): Date = Date()

        override fun yesterdayString(): String {
            return context.getString(R.string.stream_ui_yesterday)
        }

        override fun is24Hour(): Boolean {
            return DateFormat.is24HourFormat(context)
        }

        override fun dateTimePattern(): String {
            // Gets a localized pattern that contains 2 digit representations of
            // the year, month, and day of month
            return DateFormat.getBestDateTimePattern(locale, "yy MM dd")
        }
    }
}

/**
 * The number of days in a week.
 */
private const val DAYS_IN_WEEK = 7
