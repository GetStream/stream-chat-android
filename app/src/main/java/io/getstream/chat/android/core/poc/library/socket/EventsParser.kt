package io.getstream.chat.android.core.poc.library.socket

import android.util.Log
import io.getstream.chat.android.core.poc.library.Result
import io.getstream.chat.android.core.poc.library.errors.ChatError
import io.getstream.chat.android.core.poc.library.errors.TokenExpiredError
import io.getstream.chat.android.core.poc.library.events.ChatEvent
import io.getstream.chat.android.core.poc.library.events.ConnectedEvent
import io.getstream.chat.android.core.poc.library.gson.JsonParser
import okhttp3.Response
import okhttp3.WebSocket
import java.util.*


class EventsParser(
    val service: ChatSocketService,
    val jsonParser: JsonParser
) : okhttp3.WebSocketListener() {

    private var firstReceivedMessage = false
    private val TAG = javaClass.simpleName

    override fun onOpen(webSocket: WebSocket, response: Response) {
        firstReceivedMessage = true
    }

    override fun onMessage(webSocket: WebSocket, text: String) {

        Log.d(TAG, "onMessage: $text")

        val errorMessage = jsonParser.fromJsonOrError(text, WsErrorMessage::class.java)

        if (errorMessage.isSuccess && errorMessage.data().error != null) {
            handleErrorEvent(errorMessage)
        } else {
            handleEvent(text)
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {

    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        //treat as failure, socket shouldn't be closed by server
        onFailure(webSocket, ChatError("server closed connection"), null)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        //Called when socket is connected by client also. See issue here https://stream-io.atlassian.net/browse/CAS-88
        service.onSocketError(ChatError("listener.onFailure error. reconnecting", t))
    }

    private fun handleEvent(text: String) {
        val eventMessage = jsonParser.fromJsonOrError(text, ChatEvent::class.java)

        if (eventMessage.isSuccess) {
            val event = eventMessage.data()
            val now = Date()
            event.receivedAt = now
            service.setLastEventDate(now)

            if (firstReceivedMessage) {
                firstReceivedMessage = false
                val connection = jsonParser.fromJsonOrError(text, ConnectedEvent::class.java)

                if (connection.isSuccess) {
                    service.onConnectionResolved(connection.data())
                } else {
                    service.onSocketError(
                        ChatError("unable to parse connection event", connection.error())
                    )
                }

            } else {
                service.onEvent(event)
            }
        } else {
            service.onSocketError(ChatError("Unable to parse message: $text"))
        }
    }

    private fun handleErrorEvent(errorMessage: Result<WsErrorMessage>) {
        val error = errorMessage.data().error!!

        if (error.code == 40) {
            service.onSocketError(TokenExpiredError())
        } else {
            service.onSocketError(ChatError("listener.onMessage error code: ${error.code} message: ${error.message}"))
        }
    }
}
