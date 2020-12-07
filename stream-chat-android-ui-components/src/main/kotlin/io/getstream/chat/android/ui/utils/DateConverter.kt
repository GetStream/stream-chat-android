package io.getstream.chat.android.ui.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

internal object DateConverter {

    fun toLocalDateTime(date: Date): LocalDateTime {
        return Instant.ofEpochMilli(date.time)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }
}
