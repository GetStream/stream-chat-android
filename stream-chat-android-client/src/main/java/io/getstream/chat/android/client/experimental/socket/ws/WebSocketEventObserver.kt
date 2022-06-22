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

import io.getstream.chat.android.client.experimental.socket.ShutdownReason
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import io.getstream.chat.android.client.experimental.socket.Event.WebSocket as WebSocketEvent

private const val EVENTS_BUFFER_SIZE = 100

internal class WebSocketEventObserver : WebSocketListener() {
    private val _eventsFlow = MutableSharedFlow<WebSocketEvent>(extraBufferCapacity = EVENTS_BUFFER_SIZE)

    val eventsFlow = _eventsFlow

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
