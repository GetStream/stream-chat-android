package io.getstream.chat.android.client.api.models

public data class SearchMessagesRequest(
    val offset: Int,
    val limit: Int,
    val channelFilter: FilterObject,
    val messageFilter: FilterObject,
)
