package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.User

internal data class TokenResponse(
    val user: User,
    val accessToken: String,
)
