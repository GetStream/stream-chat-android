package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.Reaction

internal data class ReactionRequest(
    val reaction: Reaction,
    val enforceUnique: Boolean,
)
