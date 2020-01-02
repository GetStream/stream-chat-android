package io.getstream.chat.android.core.poc.library.socket

interface WSResponseHandler {
    fun onWSEvent(event: Event?)
    fun connectionResolved(event: Event?)
    fun connectionRecovered()
    fun tokenExpired()
    fun onError(error: WsErrorMessage?)
}
