package com.getstream.sdk.chat.utils

import android.content.Context
import android.text.format.DateFormat
import com.getstream.sdk.chat.R
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

internal class DefaultDateFormatter(private val dateContext: DateContext) : DateFormatter {

    constructor(context: Context) : this(DefaultDateContext(context))
    private val defaultLocale = Locale.getDefault()
    private val defaultDateFormat = DateTimeFormatter.ofPattern("MMM d", defaultLocale)
    private val timeFormatter12h = DateTimeFormatter.ofPattern("h:mm a", defaultLocale)
    private val timeFormatter24h = DateTimeFormatter.ofPattern("HH:mm", defaultLocale)

    override fun format(localDateTime: LocalDateTime?): String {
        localDateTime ?: return ""

        val date = localDateTime.toLocalDate()
        return when {
            date.isToday() -> getFormatter().format(localDateTime)
            else -> defaultDateFormat.format(date)
        }
    }

    private fun getFormatter(): DateTimeFormatter {
        return if (dateContext.is24Hour()) timeFormatter24h else timeFormatter12h
    }

    private fun LocalDate.isToday(): Boolean {
        return this == LocalDate.now()
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
            return context.getString(R.string.stream_yesterday)
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
