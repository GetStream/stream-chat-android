package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.clientstate.DisconnectCause
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent

public open class SocketListener {

    public open fun onConnecting() {
    }

    public open fun onConnected(event: ConnectedEvent) {
    }

    public open fun onDisconnected(cause: DisconnectCause) {
    }

    public open fun onError(error: ChatError) {
    }

    public open fun onEvent(event: ChatEvent) {
    }
}
