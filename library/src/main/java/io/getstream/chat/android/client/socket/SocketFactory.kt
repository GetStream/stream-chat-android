package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.models.User

internal interface SocketFactory {
    fun create(endpoint: String, apiKey: String, user: User? = null): Socket
}
