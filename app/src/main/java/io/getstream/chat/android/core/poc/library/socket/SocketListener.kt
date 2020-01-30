package io.getstream.chat.android.core.poc.library.socket

import io.getstream.chat.android.core.poc.library.events.ChatEvent
import io.getstream.chat.android.core.poc.library.errors.ChatError
import io.getstream.chat.android.core.poc.library.events.ConnectionEvent

open class SocketListener {

    open fun onSocketOpen() {

    }

    open fun onSocketClosing(code: Int, reason: String) {

    }

    open fun onSocketClosed(code: Int, reason: String) {

    }

    open fun onRemoteEvent(event: ChatEvent) {

    }

    open fun onSocketFailure(error: ChatError) {

    }

    open fun connectionResolved(event: ConnectionEvent) {

    }

    open fun connectionRecovered(connection: ConnectionData) {

    }

    open fun onDisconnectCalled() {

    }

    open fun tokenExpired() {

    }

    open fun onError(error: ChatError) {

    }
}
