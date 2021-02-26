package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto

@JsonClass(generateAdapter = true)
internal data class MessagesResponse(
    val messages: List<DownstreamMessageDto>,
)
