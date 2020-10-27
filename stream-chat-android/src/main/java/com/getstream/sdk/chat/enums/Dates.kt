package com.getstream.sdk.chat.enums

import androidx.annotation.StringRes
import com.getstream.sdk.chat.Chat
import com.getstream.sdk.chat.ChatUX
import com.getstream.sdk.chat.R

internal enum class Dates(@StringRes internal val labelId: Int) {
    TODAY(R.string.stream_today),
    YESTERDAY(R.string.stream_yesterday),
    JUST_NOW(R.string.stream_just_now);
}

internal val Dates.label: String
    get() = ChatUX.instance().strings.get(labelId)
