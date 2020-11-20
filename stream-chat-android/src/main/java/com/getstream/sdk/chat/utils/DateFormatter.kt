package com.getstream.sdk.chat.utils

import android.annotation.SuppressLint
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@InternalStreamChatApi
public object DateFormatter {

    @SuppressLint("ConstantLocale")
    private val DEFAULT_LOCALE = Locale.getDefault()
    private val DATE_FORMAT: DateFormat = SimpleDateFormat("MMM d", DEFAULT_LOCALE)
    private val TIME_FORMAT: DateFormat = SimpleDateFormat("HH:mm", DEFAULT_LOCALE)

    /**
     * Formats the given [date] as 24h time if it's on the current day, and
     * as a month/day format date otherwise.
     */
    @JvmStatic
    public fun formatAsTimeOrDate(date: Date?): String {
        return when {
            date == null -> ""
            isFromToday(date) -> TIME_FORMAT.format(date)
            else -> DATE_FORMAT.format(date)
        }
    }

    private fun isFromToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val dateCalendar = Calendar.getInstance().apply {
            time = date
        }
        return today[Calendar.DAY_OF_YEAR] == dateCalendar[Calendar.DAY_OF_YEAR] &&
            today[Calendar.YEAR] == dateCalendar[Calendar.YEAR]
    }
}
