package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.models.User

interface ChatSocketService {
    fun connect(
        wsEndpoint: String,
        apiKey: String,
        user: User?,
        userToken: String?,
        listener: InitConnectionListener?
    )

    fun disconnect()

    fun addListener(listener: SocketListener)
    fun removeListener(listener: SocketListener)
}
