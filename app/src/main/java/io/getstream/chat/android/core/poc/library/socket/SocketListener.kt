package io.getstream.chat.android.core.poc.library.socket

import io.getstream.chat.android.core.poc.library.errors.ChatError
import io.getstream.chat.android.core.poc.library.events.ChatEvent
import io.getstream.chat.android.core.poc.library.events.ConnectedEvent

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
