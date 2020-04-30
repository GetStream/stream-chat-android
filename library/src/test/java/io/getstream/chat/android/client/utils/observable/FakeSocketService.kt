package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.ChatSocketService
import io.getstream.chat.android.client.socket.SocketListener

class FakeSocketService() : ChatSocketService {

    override var state: ChatSocketService.State = ChatSocketService.State.Disconnected(false)

    override fun connect(endpoint: String, apiKey: String, user: User?) {

    }

    private val listeners = mutableListOf<SocketListener>()

    fun sendEvent(event: ChatEvent) {
        listeners.forEach {
            it.onEvent(event)
        }
    }

    override fun disconnect() {

    }

    override fun addListener(listener: SocketListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: SocketListener) {
        listeners.remove(listener)
    }

}