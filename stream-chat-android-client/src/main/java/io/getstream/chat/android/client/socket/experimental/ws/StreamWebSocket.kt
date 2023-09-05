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

package io.getstream.chat.android.client.socket.experimental.ws

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.socket.SocketErrorMessage
import io.getstream.chat.android.client.utils.map
import io.getstream.chat.android.client.utils.recover
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
            eventFlow.tryEmit(parseMessage(text))
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            eventFlow.tryEmit(
                StreamWebSocketEvent.Error(
                    ChatNetworkError.create(ChatErrorCode.SOCKET_FAILURE, t)
                )
            )
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            if (code != CLOSE_SOCKET_CODE) {
                // Treat as failure and reconnect, socket shouldn't be closed by server
                eventFlow.tryEmit(StreamWebSocketEvent.Error(ChatNetworkError.create(ChatErrorCode.SOCKET_CLOSED)))
            }
        }
    })

    fun send(chatEvent: ChatEvent): Boolean = webSocket.send(parser.toJson(chatEvent))
    fun close(): Boolean = webSocket.close(CLOSE_SOCKET_CODE, CLOSE_SOCKET_REASON)
    fun listen(): Flow<StreamWebSocketEvent> = eventFlow.asSharedFlow()

    private fun parseMessage(text: String): StreamWebSocketEvent =
        parser.fromJsonOrError(text, ChatEvent::class.java)
            .map { StreamWebSocketEvent.Message(it) as StreamWebSocketEvent }
            .recover { parseChatError ->
                StreamWebSocketEvent.Error(
                    parser.fromJsonOrError(text, SocketErrorMessage::class.java)
                        .takeIf { it.isSuccess }
                        ?.data()
                        ?.error
                        ?.let {
                            ChatNetworkError.create(
                                it.code,
                                it.message,
                                it.statusCode
                            )
                        }
                        ?: ChatNetworkError.create(ChatErrorCode.CANT_PARSE_EVENT, parseChatError.cause)
                )
            }.data()
}

internal sealed class StreamWebSocketEvent {
    data class Error(val chatError: ChatError) : StreamWebSocketEvent()
    data class Message(val chatEvent: ChatEvent) : StreamWebSocketEvent()
}
