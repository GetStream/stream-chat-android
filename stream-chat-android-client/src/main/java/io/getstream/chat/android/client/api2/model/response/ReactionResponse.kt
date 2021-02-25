package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.ReactionDto

@JsonClass(generateAdapter = true)
internal data class ReactionResponse(
    val reaction: ReactionDto,
)
