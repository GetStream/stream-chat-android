package io.getstream.chat.android.compose.sample.data

import io.getstream.chat.android.client.models.User

/**
 * A data class that encapsulates all the information needed to initialize
 * the SDK and connect to Stream servers.
 */
data class UserCredentials(
    val apiKey: String,
    val user: User,
    val token: String,
)
