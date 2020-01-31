package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.User

interface WebSocketService {
    fun connect(wsEndpoint: String, apiKey: String, user: User?, userToken: String?)
    fun disconnect()
}
