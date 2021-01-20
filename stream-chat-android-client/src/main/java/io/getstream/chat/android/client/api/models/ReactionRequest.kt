package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.models.Reaction

internal data class ReactionRequest(
    val reaction: Reaction,
    @SerializedName("enforce_unique") val enforceUnique: Boolean,
)
