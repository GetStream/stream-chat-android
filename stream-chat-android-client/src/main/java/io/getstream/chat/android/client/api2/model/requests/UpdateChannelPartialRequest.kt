package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class UpdateChannelPartialRequest(
    val set: Map<String, Any>,
    val unset: List<String>,
)
