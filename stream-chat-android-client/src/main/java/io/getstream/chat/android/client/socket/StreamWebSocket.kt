/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.extractCause
import io.getstream.chat.android.client.errors.fromChatErrorCode
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.parser2.V2EventParser
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.network.infrastructure.Serializer
import io.getstream.log.StreamLog
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

private const val EVENTS_BUFFER_SIZE = 100
private const val CLOSE_SOCKET_CODE = 1000
private const val CLOSE_SOCKET_REASON = "Connection close by client"

internal val socketFrameAdapter = Serializer.moshi.adapter(Any::class.java)

/**
 * @param parser Parses the error frames.
 * @param eventParser Parses the events.
 * @param authFrame Authenticates the socket once open.
 */
internal class StreamWebSocket(
    private val parser: ChatParser,
    private val eventParser: V2EventParser,
    private val authFrame: String,
    socketCreator: (WebSocketListener) -> WebSocket,
) {
    private val logger by taggedLogger("Chat:StreamWebSocket")
    private val eventFlow = MutableSharedFlow<StreamWebSocketEvent>(extraBufferCapacity = EVENTS_BUFFER_SIZE)

    // Only touched from onMessage, which OkHttp calls on its single reader thread.
    private var connected = false

    private val webSocket = socketCreator(object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            webSocket.send(authFrame)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            StreamLog.v("Chat:Events") { "[handleEvent] event: `$text`" }
            parseMessage(text)?.let(eventFlow::tryEmit)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            eventFlow.tryEmit(
                StreamWebSocketEvent.Error(
                    Error.NetworkError.fromChatErrorCode(
                        chatErrorCode = ChatErrorCode.SOCKET_FAILURE,
                        cause = t,
                    ),
                ),
            )
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            if (code != CLOSE_SOCKET_CODE) {
                // Treat as failure and reconnect, socket shouldn't be closed by server
                eventFlow.tryEmit(
                    StreamWebSocketEvent.Error(
                        Error.NetworkError.fromChatErrorCode(
                            chatErrorCode = ChatErrorCode.SOCKET_CLOSED,
                        ),
                    ),
                )
            }
        }
    })

    /**
     * Keeps the connection [connectedEvent] established alive with a minimal health check, which the server answers
     * whatever the frame carries.
     */
    fun sendKeepAlive(connectedEvent: ConnectedEvent): Boolean = webSocket.send(
        socketFrameAdapter.toJson(
            listOf(mapOf("type" to EventType.HEALTH_CHECK, "client_id" to connectedEvent.connectionId)),
        ),
    )

    fun close(): Boolean = webSocket.close(CLOSE_SOCKET_CODE, CLOSE_SOCKET_REASON)
    fun listen(): Flow<StreamWebSocketEvent> = eventFlow.asSharedFlow()

    /**
     * Parses [text] into its event, or null for a frame the parser ignores. Once connected, a frame that fails to parse
     * and isn't an error is dropped and logged, instead of reconnecting on every such frame: its update is lost until
     * the channel is queried again, as `/sync` skips the same event. Before that, it fails the connection.
     */
    @Suppress("TooGenericExceptionCaught")
    private fun parseMessage(text: String): StreamWebSocketEvent? = try {
        eventParser.parse(text)?.let { event ->
            if (event is ConnectedEvent) connected = true
            StreamWebSocketEvent.Message(event)
        }
    } catch (expected: Throwable) {
        // Throwable: an error escaping here would reach OkHttp's reader thread.
        parseFailure(text, Error.ThrowableError("Failed to parse the event", expected))
    }

    private fun parseFailure(text: String, parseChatError: Error): StreamWebSocketEvent? {
        val errorResponse = when (val chatErrorResult = parser.fromJsonOrError(text, SocketErrorMessage::class.java)) {
            is Result.Success -> chatErrorResult.value.error
            is Result.Failure -> null
        }
        if (errorResponse == null && connected) {
            val cause = parseChatError.extractCause()?.message
            logger.e { "[parseFailure] dropped a '${text.eventType()}' event: $cause" }
            return null
        }
        return StreamWebSocketEvent.Error(
            errorResponse?.let {
                Error.NetworkError(
                    message = it.message,
                    statusCode = it.statusCode,
                    serverErrorCode = it.code,
                )
            } ?: Error.NetworkError.fromChatErrorCode(
                chatErrorCode = ChatErrorCode.CANT_PARSE_EVENT,
                cause = parseChatError.extractCause(),
            ),
        )
    }
}

private val eventTypeRegex = Regex("\"type\"\\s*:\\s*\"([^\"]*)\"")

/** The frame's event type, read without parsing it, so a log line can name it without the frame's content. */
private fun String.eventType(): String = eventTypeRegex.find(this)?.groupValues?.get(1) ?: "unknown"

internal sealed class StreamWebSocketEvent {
    data class Error(val streamError: io.getstream.result.Error) : StreamWebSocketEvent()
    data class Message(val chatEvent: ChatEvent) : StreamWebSocketEvent()
}
