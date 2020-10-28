package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.parser.ChatParser
import okhttp3.Response
import okhttp3.WebSocket

internal class EventsParser(
    private val parser: ChatParser
) : okhttp3.WebSocketListener() {

    private var connectionEventReceived = false
    private val logger = ChatLogger.get("Events")
    private lateinit var service: ChatSocketService

    fun setSocketService(service: ChatSocketService) {
        this.service = service
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        logger.logI("onOpen")
        connectionEventReceived = false
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        try {
            logger.logI(text)
            val errorMessage = parser.fromJsonOrError(text, SocketErrorMessage::class.java)
            val errorData = errorMessage.data()
            if (errorMessage.isSuccess && errorData.error != null) {
                handleErrorEvent(errorData.error)
            } else {
                handleEvent(text)
            }
        } catch (t: Throwable) {
            logger.logE("onMessage", t)
            service.onSocketError(ChatNetworkError.create(ChatErrorCode.UNABLE_TO_PARSE_SOCKET_EVENT))
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) { }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        // Treat as failure and reconnect, socket shouldn't be closed by server
        onFailure(webSocket, ChatNetworkError.create(ChatErrorCode.SOCKET_CLOSED), null)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        logger.logE("onFailure: $t", t)
        // Called when socket is disconnected by client also (client.disconnect())
        service.onSocketError(ChatNetworkError.create(ChatErrorCode.SOCKET_FAILURE, t))
    }

    private fun onFailure(webSocket: WebSocket, chatError: ChatError, response: Response?) {
        logger.logE("onFailure $chatError", chatError)
        // Called when socket is disconnected by client also (client.disconnect())
        service.onSocketError(ChatNetworkError.create(ChatErrorCode.SOCKET_FAILURE, chatError.cause))
    }

    private fun handleEvent(text: String) {
        val eventResult = parser.fromJsonOrError(text, ChatEvent::class.java)
        if (eventResult.isSuccess) {
            val event = eventResult.data()
            if (!connectionEventReceived) {
                if (event is ConnectedEvent) {
                    connectionEventReceived = true
                    service.onConnectionResolved(event)
                } else {
                    service.onSocketError(ChatNetworkError.create(ChatErrorCode.CANT_PARSE_CONNECTION_EVENT))
                }
            } else {
                service.onEvent(event)
            }
        } else {
            service.onSocketError(
                ChatNetworkError.create(ChatErrorCode.CANT_PARSE_EVENT, eventResult.error().cause)
            )
        }
    }

    private fun handleErrorEvent(error: ErrorResponse) {
        service.onSocketError(ChatNetworkError.create(error.code, error.message, error.statusCode))
    }
}
