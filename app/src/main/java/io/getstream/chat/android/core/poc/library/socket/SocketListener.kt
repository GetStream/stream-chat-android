package io.getstream.chat.android.core.poc.library.socket

import io.getstream.chat.android.core.poc.library.Event
import io.getstream.chat.android.core.poc.library.errors.ChatError

open class SocketListener {

    open fun onSocketOpen() {

    }

    open fun onSocketClosing(code: Int, reason: String) {

    }

    open fun onSocketClosed(code: Int, reason: String) {

    }

    open fun onRemoteEvent(event: Event) {

    }

    open fun onSocketFailure(error: ChatError) {

    }

    open fun connectionResolved(connection: ConnectionData) {

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
