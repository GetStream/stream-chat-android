package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.ChatEventDto

@JsonClass(generateAdapter = true)
internal data class EventResponse(
    val event: ChatEventDto,
    val duration: String,
)
