package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.models.User

internal data class TokenResponse(
    val user: User,
    @SerializedName("access_token")
    val accessToken: String
)
