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

import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.extractCause
import io.getstream.chat.android.client.errors.fromChatErrorCode
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.log.StreamLog
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.recover
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

private const val EVENTS_BUFFER_SIZE = 100
private const val CLOSE_SOCKET_CODE = 1000
private const val CLOSE_SOCKET_REASON = "Connection close by client"

internal class StreamWebSocket(
    private val parser: ChatParser,
    socketCreator: (WebSocketListener) -> WebSocket,
) {
    private val eventFlow = MutableSharedFlow<StreamWebSocketEvent>(extraBufferCapacity = EVENTS_BUFFER_SIZE)

    private val webSocket = socketCreator(object : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            StreamLog.v("Chat:Events") { "[handleEvent] event: `$text`" }
            eventFlow.tryEmit(parseMessage(text))
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

    fun send(chatEvent: ChatEvent): Boolean = webSocket.send(parser.toJson(chatEvent))
    fun close(): Boolean = webSocket.close(CLOSE_SOCKET_CODE, CLOSE_SOCKET_REASON)
    fun listen(): Flow<StreamWebSocketEvent> = eventFlow.asSharedFlow()

    private fun parseMessage(text: String): StreamWebSocketEvent = parser.fromJsonOrError(text, ChatEvent::class.java)
        .map { StreamWebSocketEvent.Message(it) }
        .recover { parseChatError ->
            val errorResponse =
                when (val chatErrorResult = parser.fromJsonOrError(text, SocketErrorMessage::class.java)) {
                    is Result.Success -> {
                        chatErrorResult.value.error
                    }
                    is Result.Failure -> null
                }
            StreamWebSocketEvent.Error(
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
        }.value
}

internal sealed class StreamWebSocketEvent {
    data class Error(val streamError: io.getstream.result.Error) : StreamWebSocketEvent()
    data class Message(val chatEvent: ChatEvent) : StreamWebSocketEvent()
}
