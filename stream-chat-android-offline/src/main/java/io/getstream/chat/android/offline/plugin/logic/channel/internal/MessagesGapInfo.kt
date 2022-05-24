package io.getstream.chat.android.offline.plugin.logic.channel.internal

public data class MessagesGapInfo(
    val messagesAboveGap: List<Long>,
    val messagesBellowGap: List<Long>
)
