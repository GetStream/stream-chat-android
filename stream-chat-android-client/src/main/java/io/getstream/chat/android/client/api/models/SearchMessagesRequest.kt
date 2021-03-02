package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName

public data class SearchMessagesRequest(
    val offset: Int,
    val limit: Int,
    @SerializedName("filter_conditions")
    val channelFilter: FilterObject,
    @SerializedName("message_filter_conditions")
    val messageFilter: FilterObject
)
