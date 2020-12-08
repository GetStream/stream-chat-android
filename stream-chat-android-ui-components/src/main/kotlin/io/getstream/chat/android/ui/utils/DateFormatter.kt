package io.getstream.chat.android.ui.utils

import android.content.Context
import org.threeten.bp.LocalDateTime
import java.util.Date

public interface DateFormatter {
    public fun formatMessageDate(localDateTime: LocalDateTime?): String

    public companion object {
        public fun from(context: Context): DateFormatter = DefaultDateFormatter(context)
    }
}

/**
 * Extension to be able to format objects of the deprecated [Date] type.
 */
internal fun DateFormatter.formatMessageDate(date: Date?): String {
    return formatMessageDate(date?.let(DateConverter::toLocalDateTime))
}
