package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.User


class TokenResponse {
    lateinit var user: User
    val access_token: String = ""
}