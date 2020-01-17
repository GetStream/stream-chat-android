package io.getstream.chat.android.core.poc.library.socket

import io.getstream.chat.android.core.poc.library.Event

interface WsListener {

    fun onWSEvent(event: Event) {

    }

    fun connectionResolved(event: Event) {

    }

    fun connectionRecovered() {

    }

    fun tokenExpired() {

    }

    fun onError(error: WsErrorMessage) {

    }
}
