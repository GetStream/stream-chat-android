package com.getstream.sdk.chat.utils

import org.threeten.bp.LocalDateTime
import java.util.Date

public abstract class LegacyDateFormatter : DateFormatter {
    public abstract fun format(date: Date?): String
    final override fun formatDate(localDateTime: LocalDateTime?): String =
        format(localDateTime?.let(DateConverter::toDate))
}
