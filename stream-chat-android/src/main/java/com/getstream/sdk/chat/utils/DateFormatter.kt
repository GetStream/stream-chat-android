package com.getstream.sdk.chat.utils

import android.content.Context
import org.threeten.bp.LocalDateTime
import java.util.Date

public interface DateFormatter {
    public fun format(localDateTime: LocalDateTime?): String

    public companion object {
        @JvmStatic
        public fun getDefault(context: Context): DateFormatter = DefaultDateFormatter(context)
    }
}

/**
 * Extension to be able to format objects of the deprecated [Date] type.
 */
internal fun DateFormatter.format(date: Date?): String {
    return format(date?.let(DateConverter::toLocalDateTime))
}
