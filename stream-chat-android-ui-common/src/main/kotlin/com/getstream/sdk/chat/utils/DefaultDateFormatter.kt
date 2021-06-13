package com.getstream.sdk.chat.utils

import android.content.Context
import android.text.format.DateFormat
import io.getstream.chat.android.ui.common.R
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

internal class DefaultDateFormatter(
    private val dateContext: DateContext
) : DateFormatter {

    constructor(context: Context) : this(DefaultDateContext(context))

    private val timeFormatter12h = DateTimeFormatter.ofPattern("h:mm a")
    private val timeFormatter24h = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormatterDayOfWeek = DateTimeFormatter.ofPattern("EEEE")
    private val dateFormatterFullDate: DateTimeFormatter
        // Re-evaluated every time to account for runtime Locale changes
        get() = DateTimeFormatter.ofPattern(dateContext.dateTimePattern())

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
        return formatter.format(localTime)
    }

    private fun LocalDate.isToday(): Boolean {
        return this == dateContext.now()
    }

    private fun LocalDate.isYesterday(): Boolean {
        return this == dateContext.now().minusDays(1)
    }

    private fun LocalDate.isWithinLastWeek(): Boolean {
        return this >= dateContext.now().minusDays(6)
    }

    interface DateContext {
        fun now(): LocalDate
        fun yesterdayString(): String
        fun is24Hour(): Boolean
        fun dateTimePattern(): String
    }

    private class DefaultDateContext(
        private val context: Context
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
