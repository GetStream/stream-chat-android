package com.getstream.sdk.chat.utils

import android.content.Context
import android.text.format.DateFormat
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

internal class DefaultDateFormatter(val context: Context) : DateFormatter {

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
        return if (DateFormat.is24HourFormat(context)) timeFormatter24h else timeFormatter12h
    }

    private fun LocalDate.isToday(): Boolean {
        return this == LocalDate.now()
    }
}
