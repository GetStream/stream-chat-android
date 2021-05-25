package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.SocketListener

internal class FakeChatSocket : ChatSocket {

    private val listeners = mutableSetOf<SocketListener>()

    override fun connect(user: User) {
    }

    override fun connectAnonymously() {
    }

    override fun addListener(listener: SocketListener) {
        listeners += listener
    }

    override fun removeListener(listener: SocketListener) {
        listeners -= listener
    }

    fun sendEvent(event: ChatEvent) {
        listeners.forEach {
            it.onEvent(event)
        }
    }

    override fun disconnect() {
    }

    override fun releaseConnection() {
    }
}
