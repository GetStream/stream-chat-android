package com.getstream.sdk.chat.utils

import org.threeten.bp.LocalDateTime
import java.util.Date

public abstract class LegacyDateFormatter : DateFormatter {
    public abstract fun formatDate(date: Date?): String
    public abstract fun formatTime(date: Date?): String
    final override fun formatDate(localDateTime: LocalDateTime?): String =
        formatDate(localDateTime?.let(DateConverter::toDate))

    final override fun formatTime(localDateTime: LocalDateTime?): String =
        formatTime(localDateTime?.let(DateConverter::toDate))
}
