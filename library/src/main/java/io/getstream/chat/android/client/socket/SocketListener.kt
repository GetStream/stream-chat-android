package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent

open class SocketListener {

    open fun onConnecting() {

    }

    open fun onConnected(event: ConnectedEvent) {

    }

    open fun onDisconnected() {

    }

    open fun onError(error: ChatError) {

    }

    open fun onEvent(event: ChatEvent) {

    }
}
