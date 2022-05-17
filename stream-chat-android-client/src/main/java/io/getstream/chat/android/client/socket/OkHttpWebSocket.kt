package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.parser.ChatParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import okhttp3.WebSocket
import okio.ByteString

/**
 * A wrapper around [WebSocket] which allows us to observe the events
 * and also handles some of the events before they are propagated to downstream.
 */
internal class OkHttpWebSocket constructor(
    private val eventsObserver: WebSocketEventObserver,
    private val parser: ChatParser,
) {
    private var webSocket: WebSocket? = null

    fun open(): Flow<Event.WebSocket> {
        return eventsObserver.eventsFlow.onEach(::handleWebSocketEvent)
    }

    fun send(event: ChatEvent): Boolean = webSocket?.send(parser.toJson(event)) ?: false

    fun send(bytes: ByteString) = webSocket?.send(bytes) ?: false

    fun close(shutdownReason: ShutdownReason): Boolean {
        val (code, reasonText) = shutdownReason
        return webSocket?.close(code, reasonText) ?: false
    }

    private fun handleConnectionShutdown() {
        webSocket = null
        eventsObserver.terminate()
    }

    fun cancel() = webSocket?.cancel() ?: Unit

    private fun handleWebSocketEvent(event: Event.WebSocket) {
        when (event) {
            is Event.WebSocket.OnConnectionOpened<*> -> webSocket = event.webSocket as WebSocket
            is Event.WebSocket.OnConnectionClosing -> close(ShutdownReason.GRACEFUL)
            is Event.WebSocket.OnConnectionClosed, is Event.WebSocket.OnConnectionFailed -> handleConnectionShutdown()
            else -> Unit
        }
    }
}