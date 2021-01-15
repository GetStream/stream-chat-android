package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.models.User

internal interface ChatSocketService {
    fun anonymousConnect(endpoint: String, apiKey: String)
    fun userConnect(endpoint: String, apiKey: String, user: User)
    fun disconnect()
    fun addListener(listener: SocketListener)
    fun removeListener(listener: SocketListener)
    fun onSocketError(error: ChatError)
    fun onConnectionResolved(event: ConnectedEvent)
    fun onEvent(event: ChatEvent)
}
