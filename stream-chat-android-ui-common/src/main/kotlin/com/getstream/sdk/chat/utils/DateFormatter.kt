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

package com.getstream.sdk.chat.utils

import android.content.Context
import android.text.format.DateFormat
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.R
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * An interface that allows to format date-time objects as strings.
 */
public interface DateFormatter {

    /**
     * Formats the given date as a String.
     *
     * @param localDateTime The [LocalDateTime] to format as a String.
     * @return The formatted date-time string.
     */
    public fun formatDate(localDateTime: LocalDateTime?): String

    /**
     * Formats the given time as a String.
     *
     * @param localTime The [LocalTime] object to format as a String.
     * @return The formatted time string.
     */
    public fun formatTime(localTime: LocalTime?): String

    /**
     * Formats the given date-time as a String.
     *
     * @param localDateTime The [LocalDateTime] object to format as a String.
     * @return The formatted date-time string.
     */
    public fun formatTime(localDateTime: LocalDateTime?): String {
        return formatTime(localDateTime?.toLocalTime())
    }

    public companion object {
        /**
         * Builds the default date formatter.
         *
         * @param context The context of the application.
         * @return The default implementation of [DateFormatter].
         */
        @JvmStatic
        public fun from(context: Context): DateFormatter = DefaultDateFormatter(context)
    }
}

/**
 * Extension to be able to format objects of the deprecated [Date] type.
 */
@InternalStreamChatApi
public fun DateFormatter.formatDate(date: Date?): String {
    return formatDate(date?.let(DateConverter::toLocalDateTime))
}

/**
 * Extension to be able to format objects of the deprecated [Date] type.
 */
@InternalStreamChatApi
public fun DateFormatter.formatTime(date: Date?): String {
    return formatTime(date?.let(DateConverter::toLocalDateTime))
}

internal class DefaultDateFormatter(
    private val dateContext: DateContext,
    private val locale: Locale = Locale.getDefault(),
) : DateFormatter {

    constructor(context: Context) : this(DefaultDateContext(context))

    private val timeFormatter12h = DateTimeFormatter.ofPattern("h:mm a", locale)
    private val timeFormatter24h = DateTimeFormatter.ofPattern("HH:mm", locale)
    private val dateFormatterDayOfWeek = DateTimeFormatter.ofPattern("EEEE", locale)
    private val dateFormatterFullDate: DateTimeFormatter
        // Re-evaluated every time to account for runtime Locale changes
        get() = DateTimeFormatter.ofPattern(dateContext.dateTimePattern(), locale)

    override fun formatDate(localDateTime: LocalDateTime?): String {
        localDateTime ?: return ""

        val localDate = localDateTime.toLocalDate()
        return when {
            localDate.isToday() -> formatTime(localDateTime.toLocalTime())
            localDate.isYesterday() -> dateContext.yesterdayString()
            localDate.isWithinLastWeek() -> dateFormatterDayOfWeek.format(localDate)
            else -> dateFormatterFullDate.format(localDate)
        }
    }

    override fun formatTime(localTime: LocalTime?): String {
        localTime ?: return ""
        val formatter = if (dateContext.is24Hour()) timeFormatter24h else timeFormatter12h
        return formatter.format(localTime).uppercase(Locale.getDefault())
    }

    private fun LocalDate.isToday(): Boolean {
        return this == dateContext.now()
    }

    private fun LocalDate.isYesterday(): Boolean {
        return this == dateContext.now().minusDays(1)
    }

    private fun LocalDate.isWithinLastWeek(): Boolean {
        return this > dateContext.now().minusDays(DAYS_IN_A_WEEK)
    }

    interface DateContext {
        fun now(): LocalDate
        fun yesterdayString(): String
        fun is24Hour(): Boolean
        fun dateTimePattern(): String
    }

    private class DefaultDateContext(
        private val context: Context,
    ) : DateContext {
        override fun now(): LocalDate = LocalDate.now()

        override fun yesterdayString(): String {
            return context.getString(R.string.stream_ui_yesterday)
        }

        override fun is24Hour(): Boolean {
            return DateFormat.is24HourFormat(context)
        }

        override fun dateTimePattern(): String {
            // Gets a localized pattern that contains 2 digit representations of
            // the year, month, and day of month
            return DateFormat.getBestDateTimePattern(Locale.getDefault(), "yy MM dd")
        }
    }
}

private const val DAYS_IN_A_WEEK = 7L
