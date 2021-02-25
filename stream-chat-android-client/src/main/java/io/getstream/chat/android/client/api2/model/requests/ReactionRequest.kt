package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.ReactionDto

@JsonClass(generateAdapter = true)
internal data class ReactionRequest(
    val reaction: ReactionDto,
    val enforce_unique: Boolean,
)
