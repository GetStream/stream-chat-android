package io.getstream.chat.android.core.poc.library.socket

import android.util.Log
import io.getstream.chat.android.core.poc.library.Event
import io.getstream.chat.android.core.poc.library.errors.ChatError
import io.getstream.chat.android.core.poc.library.gson.JsonParser
import okhttp3.Response
import okhttp3.WebSocket
import java.util.*


class ChatSocketListener(
    val service: StreamWebSocketService,
    val jsonParser: JsonParser
) : okhttp3.WebSocketListener() {

    private val NORMAL_CLOSURE_STATUS = 1000
    private var firstMessage = false

    @Synchronized
    override fun onOpen(webSocket: WebSocket, response: Response) {
        if (service.shuttingDown) return

        firstMessage = true

        service.setHealth(true)
        service.isConnecting = false
        service.resetConsecutiveFailures()
        service.onSocketOpen()
    }

    @Synchronized
    override fun onMessage(
        webSocket: WebSocket,
        text: String
    ) {
        Log.d("event", text)

        if (service.shuttingDown) return


        val eventMessage = jsonParser.fromJsonOrError(text, Event::class.java)
        val errorMessage = jsonParser.fromJsonOrError(text, WsErrorMessage::class.java)

        if (eventMessage.isSuccess) {

            val event = eventMessage.data()
            val now = Date()
            event.receivedAt = now
            service.lastEvent = now

            if (firstMessage) {
                firstMessage = false
                service.onConnectionResolved(event.connectionId, event.me!!)
            } else {
                service.onEvent(event)
            }

        } else if (errorMessage.isSuccess) {

            val error = errorMessage.data()

            if (error.error.code == 40) {
                service.onSocketTokenExpired()
            } else {
                service.onSocketError(ChatError("listener.onMessage error: ${error.error}"))
            }
        } else {
            service.onSocketError(ChatError("Unable to parse message: $text"))
        }
    }

    @Synchronized
    override fun onClosing(
        webSocket: WebSocket,
        code: Int,
        reason: String
    ) {
        if (service.shuttingDown) return

        service.onSocketClosing(code, reason)

        if (code == NORMAL_CLOSURE_STATUS) {
            webSocket.close(code, reason)
        } else {
            service.consecutiveFailures++
            service.isConnecting = false
            service.setHealth(false)
            service.reconnect(true)
            webSocket.close(code, reason)
        }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {

        if (service.shuttingDown) return

        service.onSocketClosed(code, reason)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {

        t.printStackTrace()

        if (service.shuttingDown) return

        service.onSocketFailure(ChatError("listener.onFailure error", t))

        if (service.shuttingDown) {
            service.shuttingDown = false
            service.isConnecting = false
            service.isHealthy = false
            service.connected = false
            service.resetConsecutiveFailures()
            return
        }

        service.consecutiveFailures++
        service.isConnecting = false
        service.setHealth(false)
        service.reconnect(true)
    }
}
