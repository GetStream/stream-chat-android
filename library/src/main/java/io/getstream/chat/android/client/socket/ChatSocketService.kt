package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.models.User
import java.util.Date

interface ChatSocketService {

    var state: State

    fun connect(endpoint: String, apiKey: String, user: User?)

    fun disconnect()

    fun addListener(listener: SocketListener)
    fun removeListener(listener: SocketListener)

    fun onSocketError(error: ChatError)
    fun onConnectionResolved(event: ConnectedEvent)
    fun onEvent(event: ChatEvent)
    fun setLastEventDate(date: Date)

    sealed class State {
        object Connecting : State()
        class Connected(val event: ConnectedEvent) : State()
        class Disconnected(val connectionWillFollow: Boolean) : State()
        class Error(val error: ChatError) : State()
    }
}
