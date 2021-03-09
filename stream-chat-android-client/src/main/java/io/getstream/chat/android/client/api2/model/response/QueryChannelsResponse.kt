package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class QueryChannelsResponse(
    val channels: List<ChannelResponse>,
)
