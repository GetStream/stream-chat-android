package com.getstream.sdk.chat.utils

import android.content.Context
import org.threeten.bp.LocalDateTime
import java.util.Date

public interface DateFormatter {
    public fun formatDate(localDateTime: LocalDateTime?): String

    public companion object {
        @JvmStatic
        public fun from(context: Context): DateFormatter = DefaultDateFormatter(context)
    }
}

/**
 * Extension to be able to format objects of the deprecated [Date] type.
 */
public fun DateFormatter.formatDate(date: Date?): String {
    return formatDate(date?.let(DateConverter::toLocalDateTime))
}
