package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.ChatEventDto

@JsonClass(generateAdapter = true)
internal data class SyncHistoryResponse(
    val events: List<ChatEventDto>,
)
