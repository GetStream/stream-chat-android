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

package io.getstream.chat.android.client.experimental.socket.ws

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.experimental.socket.Event
import io.getstream.chat.android.client.experimental.socket.ShutdownReason
import io.getstream.chat.android.client.parser.ChatParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import okhttp3.WebSocket
import okio.ByteString

/**
 * A wrapper around [WebSocket] which allows us to observe the events
 * and also handles some of the events before they are propagated to downstream.
 */
internal class OkHttpWebSocket constructor(
    private val eventsObserver: WebSocketEventObserver,
    private val parser: ChatParser,
) {
    private var webSocket: WebSocket? = null

    fun open(): Flow<Event.WebSocket> {
        return eventsObserver.eventsFlow.onEach(::handleWebSocketEvent)
    }

    fun send(event: ChatEvent): Boolean = webSocket?.send(parser.toJson(event)) ?: false

    fun send(bytes: ByteString) = webSocket?.send(bytes) ?: false

    fun close(shutdownReason: ShutdownReason): Boolean {
        val (code, reasonText) = shutdownReason
        return webSocket?.close(code, reasonText) ?: false
    }

    private fun handleConnectionShutdown() {
        webSocket = null
        eventsObserver.terminate()
    }

    fun cancel() = webSocket?.cancel() ?: Unit

    private fun handleWebSocketEvent(event: Event.WebSocket) {
        when (event) {
            is Event.WebSocket.OnConnectionOpened<*> -> webSocket = event.webSocket as WebSocket
            is Event.WebSocket.OnConnectionClosing -> close(ShutdownReason.GRACEFUL)
            is Event.WebSocket.OnConnectionClosed, is Event.WebSocket.OnConnectionFailed -> handleConnectionShutdown()
            else -> Unit
        }
    }
}
