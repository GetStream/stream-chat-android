package io.getstream.chat.android.core.poc.library.socket

interface WebSocketService {
    fun connect()
    fun disconnect()
    val webSocketListener: WSResponseHandler?
}
