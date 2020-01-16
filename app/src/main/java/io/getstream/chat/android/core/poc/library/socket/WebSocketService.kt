package io.getstream.chat.android.core.poc.library.socket

interface WebSocketService {
    fun connect(listener: (ConnectionData, Throwable?) -> Unit)
    fun disconnect()
    fun webSocketListener(): WSResponseHandler
}
