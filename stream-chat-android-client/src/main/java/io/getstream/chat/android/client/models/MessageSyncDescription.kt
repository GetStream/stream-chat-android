package io.getstream.chat.android.client.models

public data class MessageSyncDescription(
    val type: MessageSyncType,
    val content: MessageSyncContent
)