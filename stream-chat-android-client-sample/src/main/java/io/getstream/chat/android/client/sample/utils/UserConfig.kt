package io.getstream.chat.android.client.sample.utils

import io.getstream.chat.android.client.models.User

data class UserConfig(
    val userId: String,
    val token: String,
    val apiKey: String
) {
    fun getUser(): User {
        return User(userId)
    }
}
