package com.getstream.sdk.chat.utils

import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import java.util.Date

internal object DateConverter {

    fun toLocalDateTime(date: Date): LocalDateTime {
        return Instant.ofEpochMilli(date.time)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    fun toDate(localDateTime: LocalDateTime): Date {
        return Date(
            localDateTime.atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
    }
}
