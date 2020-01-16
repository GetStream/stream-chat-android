package io.getstream.chat.android.core.poc.library.socket

import io.getstream.chat.android.core.poc.library.User

data class ConnectionData(
    val connectionId: String,
    val user: User
)