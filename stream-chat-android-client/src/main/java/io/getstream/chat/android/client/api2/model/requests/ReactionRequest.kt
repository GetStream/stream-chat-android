package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.UpstreamReactionDto

@JsonClass(generateAdapter = true)
internal data class ReactionRequest(
    val reaction: UpstreamReactionDto,
    val enforce_unique: Boolean,
)
