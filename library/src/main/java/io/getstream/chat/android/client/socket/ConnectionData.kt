package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.User

data class ConnectionData(
    val connectionId: String,
    val user: User
)