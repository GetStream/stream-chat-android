package io.getstream.chat.android.core.poc.library.socket

import io.getstream.chat.android.core.poc.library.User

interface WebSocketService {
    fun connect(wsEndpoint: String, apiKey: String, user: User?, userToken: String?, listener: SocketListener)
    fun disconnect()
}
