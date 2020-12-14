package io.getstream.chat.android.client.models

import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public data class ChannelInfo(
    val cid: String? = null,
    val id: String? = null,
    val type: String? = null,
    val memberCount: Int = 0,
    val name: String? = null,
)
