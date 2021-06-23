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

    @Deprecated(
        "Use method with DisconnectCause",
        replaceWith = ReplaceWith("Use method with DisconnectCause instead of it"),
        level = DeprecationLevel.ERROR,
    )
    public open fun onDisconnected() {
    }

    public open fun onDisconnected(cause: DisconnectCause) {
    }

    public open fun onError(error: ChatError) {
    }

    public open fun onEvent(event: ChatEvent) {
    }
}
