package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionDto

@JsonClass(generateAdapter = true)
internal data class ReactionResponse(
    val reaction: DownstreamReactionDto,
)
