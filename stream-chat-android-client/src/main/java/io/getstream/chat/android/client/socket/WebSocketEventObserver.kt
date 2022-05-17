package io.getstream.chat.android.client.socket

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import io.getstream.chat.android.client.socket.Event.WebSocket as WebSocketEvent

internal class WebSocketEventObserver : WebSocketListener() {
    private val _eventsFlow = MutableSharedFlow<WebSocketEvent>(extraBufferCapacity = 1)

    val eventsFlow = _eventsFlow.asSharedFlow()

    fun terminate() = _eventsFlow.tryEmit(WebSocketEvent.Terminate)

    override fun onOpen(webSocket: WebSocket, response: Response) {
        _eventsFlow.tryEmit(WebSocketEvent.OnConnectionOpened(webSocket))
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        // no-op
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        _eventsFlow.tryEmit(WebSocketEvent.OnMessageReceived(text))
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        _eventsFlow.tryEmit(WebSocketEvent.OnConnectionClosing(ShutdownReason(code, reason)))
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        _eventsFlow.tryEmit(WebSocketEvent.OnConnectionClosed(ShutdownReason(code, reason)))
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        _eventsFlow.tryEmit(WebSocketEvent.OnConnectionFailed(t))
    }
}