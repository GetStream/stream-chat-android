package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.parser.ChatParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import okhttp3.WebSocket

internal class OkHttpWebSocket constructor(
    private val socketHolder: SocketHolder,
    private val webSocketEventObserver: WebSocketEventObserver,
    private val parser: ChatParser,
) {
    fun open(): Flow<ChatSocket.Event.WebSocket> {
        return webSocketEventObserver.eventsFlow.onEach(::handleWebSocketEvent)
    }

    fun send(event: ChatEvent): Boolean = socketHolder.send(parser.toJson(event))

    fun close(shutdownReason: ShutdownReason): Boolean {
        val (code, reasonText) = shutdownReason
        return socketHolder.close(code, reasonText)
    }

    private fun handleConnectionShutdown() {
        socketHolder.shutdown()
        webSocketEventObserver.terminate()
    }

    fun cancel() = socketHolder.cancel()

    private fun handleWebSocketEvent(event: ChatSocket.Event.WebSocket) {
        when (event) {
            is ChatSocket.Event.WebSocket.OnConnectionOpened<*> -> socketHolder.initiate(event.webSocket as WebSocket)
            is ChatSocket.Event.WebSocket.OnConnectionClosing -> close(ShutdownReason.GRACEFUL)
            is ChatSocket.Event.WebSocket.OnConnectionClosed, is ChatSocket.Event.WebSocket.OnConnectionFailed -> handleConnectionShutdown()
            else -> Unit
        }
    }
}