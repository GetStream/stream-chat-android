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

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.annotation.RequiresApi
import io.getstream.chat.android.core.utils.date.isWithinDurationFromNow
import io.getstream.chat.android.models.TimeDuration
import io.getstream.chat.android.ui.common.R
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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

    /**
     * Formats the given date as a relative time string.
     *
     * @param date The [Date] to format as a relative time string.
     * @return The formatted relative time string.
     */
    public fun formatRelativeTime(date: Date?): String

    /**
     * Returns a relative date string for the given date.
     *
     * @param date The [Date] to format as a relative date string.
     * @return The formatted relative date string.
     */
    public fun formatRelativeDate(date: Date): String

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

    private companion object {
        const val TIME_FORMAT_12H = "h:mm a"
        const val TIME_FORMAT_24H = "HH:mm"
        const val DATE_FORMAT_DAY_OF_WEEK = "EEEE"
    }

    private val timeFormatter12h: SimpleDateFormat = SimpleDateFormat(TIME_FORMAT_12H, locale)
    private val timeFormatter24h: SimpleDateFormat = SimpleDateFormat(TIME_FORMAT_24H, locale)
    private val dateFormatterDayOfWeek: SimpleDateFormat = SimpleDateFormat(DATE_FORMAT_DAY_OF_WEEK, locale)
    private val dateFormatterFullDate: SimpleDateFormat
        // Re-evaluated every time to account for runtime Locale changes
        get() = SimpleDateFormat(dateContext.dateTimePattern(), locale)

    @delegate:RequiresApi(Build.VERSION_CODES.O)
    private val timeFormatter12hNew: DateTimeFormatter by lazy {
        DateTimeFormatter.ofPattern(TIME_FORMAT_12H)
            .withLocale(locale)
            .withZone(ZoneId.systemDefault())
    }

    @delegate:RequiresApi(Build.VERSION_CODES.O)
    private val timeFormatter24hNew: DateTimeFormatter by lazy {
        DateTimeFormatter.ofPattern(TIME_FORMAT_24H)
            .withLocale(locale)
            .withZone(ZoneId.systemDefault())
    }

    @delegate:RequiresApi(Build.VERSION_CODES.O)
    private val dateFormatterDayOfWeekNew: DateTimeFormatter by lazy {
        DateTimeFormatter.ofPattern(DATE_FORMAT_DAY_OF_WEEK)
            .withLocale(locale)
            .withZone(ZoneId.systemDefault())
    }

    private val dateFormatterFullDateNew: DateTimeFormatter
        @SuppressLint("NewApi")
        get() = DateTimeFormatter.ofPattern(dateContext.dateTimePattern())
            .withLocale(locale)
            .withZone(ZoneId.systemDefault())

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
            date.isWithinLastWeek() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    dateFormatterDayOfWeekNew.format(date.toInstant())
                } else {
                    dateFormatterDayOfWeek.format(date)
                }
            }
            else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dateFormatterFullDateNew.format(date.toInstant())
            } else {
                dateFormatterFullDate.format(date)
            }
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

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val dateFormat = if (dateContext.is24Hour()) timeFormatter24hNew else timeFormatter12hNew
            dateFormat.format(date.toInstant())
        } else {
            val dateFormat = if (dateContext.is24Hour()) timeFormatter24h else timeFormatter12h
            dateFormat.format(date)
        }
    }

    /**
     * Formats the given date as a relative time string.
     *
     * @param date The [Date] to format as a relative time string.
     * @return The formatted relative time string.
     */
    override fun formatRelativeTime(date: Date?): String {
        date ?: return ""

        return when (dateContext.isWithinLastMinute(date)) {
            true -> dateContext.justNowString()
            else -> dateContext.relativeTime(date)
        }
    }

    /**
     * Returns a relative date string for the given date.
     *
     * @param date The [Date] to format as a relative date string.
     * @return The formatted relative date string.
     */
    override fun formatRelativeDate(date: Date): String {
        return dateContext.relativeDate(date)
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
        /**
         * Returns the current date-time.
         */
        fun now(): Date

        /**
         * Returns true if the [date] is within the last minute.
         */
        fun isWithinLastMinute(date: Date?): Boolean

        /**
         * Returns the string representation of yesterday.
         */
        fun yesterdayString(): String

        /**
         * Returns the string representation of just now.
         */
        fun justNowString(): String

        /**
         * Returns true if the device is set to 24-hour time format.
         */
        fun is24Hour(): Boolean

        /**
         * Returns the date-time pattern for the current locale.
         */
        fun dateTimePattern(): String

        /**
         * Returns the relative time string for the given date.
         */
        fun relativeTime(date: Date): String

        /**
         * Returns the relative date string for the given date.
         */
        fun relativeDate(date: Date): String
    }

    private class DefaultDateContext(
        private val context: Context,
        private val locale: Locale,
    ) : DateContext {

        private val oneMinuteDuration = TimeDuration.minutes(1)

        private val dateTimePatternLazy by lazy {
            DateFormat.getBestDateTimePattern(locale, "yy MM dd")
        }

        override fun now(): Date = Date()

        override fun yesterdayString(): String {
            return context.getString(R.string.stream_ui_yesterday)
        }

        override fun justNowString(): String {
            return context.getString(R.string.stream_ui_message_list_footnote_edited_now)
        }

        override fun is24Hour(): Boolean {
            return DateFormat.is24HourFormat(context)
        }

        override fun isWithinLastMinute(date: Date?): Boolean {
            return date.isWithinDurationFromNow(oneMinuteDuration)
        }

        override fun dateTimePattern(): String {
            // Gets a localized pattern that contains 2 digit representations of
            // the year, month, and day of month
            return dateTimePatternLazy
        }

        override fun relativeTime(date: Date): String {
            return DateUtils.getRelativeDateTimeString(
                context,
                date.time,
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.WEEK_IN_MILLIS,
                0,
            ).toString()
        }

        override fun relativeDate(date: Date): String {
            return DateUtils.getRelativeTimeSpanString(
                date.time,
                now().time,
                DateUtils.DAY_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE,
            ).toString()
        }
    }
}

/**
 * The number of days in a week.
 */
private const val DAYS_IN_WEEK = 7
