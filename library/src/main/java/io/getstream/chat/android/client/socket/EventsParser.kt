package io.getstream.chat.android.client.socket

import android.util.Log
import io.getstream.chat.android.client.EventType
import io.getstream.chat.android.client.Result
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.TokenExpiredError
import io.getstream.chat.android.client.events.*
import io.getstream.chat.android.client.gson.JsonParser
import okhttp3.Response
import okhttp3.WebSocket
import java.util.*


class EventsParser(
    val service: ChatSocketServiceImpl,
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
                service.onEvent(parseEvent(event.type, text))
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

    private fun parseEvent(type: String, data: String): ChatEvent {
        return when (type) {
            EventType.MESSAGE_NEW.label -> {
                jsonParser.fromJson(data, NewMessageEvent::class.java)
            }
            EventType.TYPING_START.label -> {
                jsonParser.fromJson(data, TypingStartEvent::class.java)
            }
            EventType.TYPING_STOP.label -> {
                jsonParser.fromJson(data, TypingStopEvent::class.java)
            }
            EventType.MESSAGE_DELETED.label -> {
                jsonParser.fromJson(data, MessageDeletedEvent::class.java)
            }
            EventType.NOTIFICATION_ADDED_TO_CHANNEL.label -> {
                jsonParser.fromJson(data, AddedToChannelEvent::class.java)
            }
            else -> {
                jsonParser.fromJson(data, ChatEvent::class.java)
            }
        }
    }
}
