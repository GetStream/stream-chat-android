package io.getstream.chat.android.ui.common.utils

import kotlin.time.DurationUnit
import kotlin.time.toDuration

public object DurationParser {

    public fun durationInMilliToReadableTime(millis: Int): String {
        val duration = millis.toDuration( DurationUnit.MILLISECONDS)
        val seconds = duration.inWholeSeconds.rem(60).toString().padStart(2, '0')
        val minutes = duration.inWholeMinutes.rem(60).toString().padStart(2, '0')

        return "$minutes:$seconds"
    }
}
