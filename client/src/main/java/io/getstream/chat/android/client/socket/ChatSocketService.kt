package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.models.User

internal interface ChatSocketService {

    var state: State

    fun connect(endpoint: String, apiKey: String, user: User?)

    fun disconnect()

    fun addListener(listener: SocketListener)
    fun removeListener(listener: SocketListener)

    fun onSocketError(error: ChatError)
    fun onConnectionResolved(event: ConnectedEvent)
    fun onEvent(event: ChatEvent)

    sealed class State {
        object Connecting : State()
        data class Connected(val event: ConnectedEvent) : State()
        data class Disconnected(val connectionWillFollow: Boolean) : State()
        data class Error(val error: ChatError) : State()
    }
}
