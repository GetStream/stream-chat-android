/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.utils.stringify
import io.getstream.logging.StreamLog
import okhttp3.Response
import okhttp3.WebSocket

@Suppress("TooManyFunctions")
internal class EventsParser(
    private val parser: ChatParser,
    private val chatSocket: ChatSocket,
) : okhttp3.WebSocketListener() {

    private var connectionEventReceived = false
    private val logger = StreamLog.getLogger("Chat:Events")
    private var closedByClient = true

    override fun onOpen(webSocket: WebSocket, response: Response) {
        logger.i { "[onOpen] closedByClient: $closedByClient" }
        connectionEventReceived = false
        closedByClient = false
    }

    @Suppress("TooGenericExceptionCaught")
    override fun onMessage(webSocket: WebSocket, text: String) {
        try {
            logger.i { text }
            val errorMessage = parser.fromJsonOrError(text, SocketErrorMessage::class.java)
            val errorData = errorMessage.data()
            if (errorMessage.isSuccess && errorData.error != null) {
                handleErrorEvent(errorData.error)
            } else {
                handleEvent(text)
            }
        } catch (t: Throwable) {
            logger.e(t) { "[onMessage] failed: $t" }
            onSocketError(ChatNetworkError.create(ChatErrorCode.UNABLE_TO_PARSE_SOCKET_EVENT))
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        // no-op
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        logger.i { "[onClosed] code: $code, closedByClient: $closedByClient" }
        if (code == CODE_CLOSE_SOCKET_FROM_CLIENT) {
            closedByClient = true
        } else {
            // Treat as failure and reconnect, socket shouldn't be closed by server
            onFailure(ChatNetworkError.create(ChatErrorCode.SOCKET_CLOSED))
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        logger.e(t) { "[onFailure] throwable: $t" }
        // Called when socket is disconnected by client also (client.disconnect())
        onSocketError(ChatNetworkError.create(ChatErrorCode.SOCKET_FAILURE, t))
    }

    private fun onFailure(chatError: ChatError) {
        logger.e { "[onFailure] chatError: ${chatError.stringify()}" }
        // Called when socket is disconnected by client also (client.disconnect())
        onSocketError(ChatNetworkError.create(ChatErrorCode.SOCKET_FAILURE, chatError.cause))
    }

    internal fun closeByClient() {
        logger.i { "[closeByClient] closedByClient: $closedByClient" }
        closedByClient = true
    }

    private fun handleEvent(text: String) {
        val eventResult = parser.fromJsonOrError(text, ChatEvent::class.java)
        if (eventResult.isSuccess) {
            val event = eventResult.data()
            if (!connectionEventReceived) {
                if (event is ConnectedEvent) {
                    connectionEventReceived = true
                    onConnectionResolved(event)
                } else {
                    onSocketError(ChatNetworkError.create(ChatErrorCode.CANT_PARSE_CONNECTION_EVENT))
                }
            } else {
                onEvent(event)
            }
        } else {
            onSocketError(ChatNetworkError.create(ChatErrorCode.CANT_PARSE_EVENT, eventResult.error().cause))
        }
    }

    private fun handleErrorEvent(error: ErrorResponse) {
        logger.e { "[handleErrorEvent] error: $error" }
        onSocketError(ChatNetworkError.create(error.code, error.message, error.statusCode))
    }

    private fun onSocketError(error: ChatError) {
        logger.e { "[onSocketError] closedByClient: $closedByClient, error: ${error.stringify()}" }
        if (!closedByClient) {
            chatSocket.onSocketError(error)
        }
    }

    private fun onConnectionResolved(event: ConnectedEvent) {
        if (!closedByClient) {
            chatSocket.onConnectionResolved(event)
        }
    }

    private fun onEvent(event: ChatEvent) {
        if (!closedByClient) {
            chatSocket.onEvent(event)
        }
    }

    internal companion object {
        internal const val CODE_CLOSE_SOCKET_FROM_CLIENT = 1000
    }
}
