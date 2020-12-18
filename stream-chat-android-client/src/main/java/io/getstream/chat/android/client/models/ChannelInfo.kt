package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public data class ChannelInfo(
    val cid: String? = null,
    val id: String? = null,
    val type: String? = null,
    @SerializedName("member_count")
    val memberCount: Int = 0,
    val name: String? = null,
)
