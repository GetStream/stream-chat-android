package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.ChatSocketService
import io.getstream.chat.android.client.socket.SocketListener

class FakeChatSocket : ChatSocket {

    override val state: ChatSocketService.State
        get() = ChatSocketService.State.Error(ChatError(""))

    private val listeners = mutableSetOf<SocketListener>()

    override fun connect(user: User) {
    }

    override fun connectAnonymously() {
    }

    override fun events(): ChatObservable {
        error("not implemented")
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
}
