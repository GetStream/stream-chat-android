package com.getstream.sdk.chat.utils

import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.util.Date

public abstract class LegacyDateFormatter : DateFormatter {
    /**
     * Formats the given date as a String.
     */
    public abstract fun formatDate(date: Date?): String

    /**
     * Formats the given time as a String.
     *
     * Note that only the hour, minute, and hour values contained by the parameter
     * [Date] object are valid.
     */
    public abstract fun formatTime(time: Date?): String

    final override fun formatDate(localDateTime: LocalDateTime?): String =
        formatDate(localDateTime?.let(DateConverter::toDate))
    final override fun formatTime(localTime: LocalTime?): String =
        formatTime(localTime?.let(::toTime))

    private fun toTime(localTime: LocalTime): Date {
        @Suppress("DEPRECATION")
        return Date(0L).apply {
            hours = localTime.hour
            minutes = localTime.minute
            seconds = localTime.second
        }
    }
}
