package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.UpstreamMessageDto

@JsonClass(generateAdapter = true)
internal data class UpdateChannelRequest(
    val data: Map<String, Any>,
    val message: UpstreamMessageDto?,
)
