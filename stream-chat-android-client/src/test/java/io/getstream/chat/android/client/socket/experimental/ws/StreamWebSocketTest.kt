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

package io.getstream.chat.android.client.socket.experimental.ws

import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.fromChatErrorCode
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.socket.ErrorResponse
import io.getstream.chat.android.client.socket.SocketErrorMessage
import io.getstream.chat.android.client.socket.StreamWebSocket
import io.getstream.chat.android.client.socket.StreamWebSocketEvent
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomString
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class StreamWebSocketTest {

    private val closeSocketCode = 1000
    private val closeSocketReason = "Connection close by client"
    private val parser = mock<ChatParser>()
    private val webSocket = mock<WebSocket>()
    private lateinit var webSocketListener: WebSocketListener
    private val streamWebSocket = StreamWebSocket(parser) {
        webSocketListener = it
        webSocket
    }

    @Test
    fun `Class can be created correctly with listener invoked during creation`() {
        StreamWebSocket(parser, socketCreator = {
            it.onFailure(webSocket, IllegalStateException(), null)
            webSocket
        })
    }

    @Test
    fun `When a chatEvent is sent, websocket should send a parsed event`() {
        val chatEvent = mock<ChatEvent>()
        val textEvent = randomString()
        val expectedResult = randomBoolean()
        whenever(parser.toJson(chatEvent)) doReturn textEvent
        whenever(webSocket.send(textEvent)) doReturn expectedResult

        val result = streamWebSocket.send(chatEvent)

        verify(webSocket).send(textEvent)
        result `should be equal to` expectedResult
    }

    @Test
    fun `When close is called, the internal socket should be called`() {
        val expectedResult = randomBoolean()
        whenever(webSocket.close(closeSocketCode, closeSocketReason)) doReturn expectedResult

        val result = streamWebSocket.close()

        verify(webSocket).close(closeSocketCode, closeSocketReason)
        result `should be equal to` expectedResult
    }

    @Test
    fun `When messages are received by websocket, they should be added to eventFlow`() = runTest {
        val eventsMap = List(positiveRandomInt(50)) { mock<ChatEvent>() }.associateBy { randomString() }
        eventsMap.forEach { textEvent, chatEvent ->
            whenever(parser.fromJsonOrError(textEvent, ChatEvent::class.java)) doReturn Result.Success(chatEvent)
        }
        val listener = streamWebSocket.listen()
        val listResult = mutableListOf<StreamWebSocketEvent>()
        val job = launch { listener.collect { listResult.add(it) } }
        runCurrent()

        eventsMap.keys.forEach { textEvent -> webSocketListener.onMessage(webSocket, textEvent) }
        runCurrent()

        listResult `should be equal to` eventsMap.values.map { StreamWebSocketEvent.Message(it) }
        job.cancel()
    }

    @Test
    fun `When an known error is received by websocket, it should be added to eventFlow`() = runTest {
        val textEvent = randomString()
        val code = randomInt()
        val message = randomString()
        val statusCode = randomInt()
        val errorResult = Result.Failure(Error.ThrowableError(message = "", cause = mock()))
        whenever(parser.fromJsonOrError(textEvent, ChatEvent::class.java)) doReturn errorResult
        whenever(parser.fromJsonOrError(textEvent, SocketErrorMessage::class.java))
            .doReturn(Result.Success(SocketErrorMessage(ErrorResponse(code, message, statusCode))))
        val listener = streamWebSocket.listen()
        val listResult = mutableListOf<StreamWebSocketEvent>()
        val job = launch { listener.collect { listResult.add(it) } }
        runCurrent()

        webSocketListener.onMessage(webSocket, textEvent)
        runCurrent()

        listResult `should be equal to` listOf(
            StreamWebSocketEvent.Error(
                Error.NetworkError(
                    message = message,
                    serverErrorCode = code,
                    statusCode = statusCode,
                ),
            ),
        )
        job.cancel()
    }

    @Test
    fun `When an unknown error is received by websocket, it should be added to eventFlow`() = runTest {
        val textEvent = randomString()
        val cause = mock<Throwable>()
        whenever(parser.fromJsonOrError(textEvent, ChatEvent::class.java))
            .doReturn(Result.Failure(Error.ThrowableError(message = "", cause = cause)))
        val errorResult = Result.Failure(Error.ThrowableError(message = "", cause = mock()))
        whenever(parser.fromJsonOrError(textEvent, SocketErrorMessage::class.java)) doReturn errorResult
        val listener = streamWebSocket.listen()
        val listResult = mutableListOf<StreamWebSocketEvent>()
        val job = launch { listener.collect { listResult.add(it) } }
        runCurrent()

        webSocketListener.onMessage(webSocket, textEvent)
        runCurrent()

        listResult `should be equal to` listOf(
            StreamWebSocketEvent.Error(
                Error.NetworkError.fromChatErrorCode(
                    chatErrorCode = ChatErrorCode.CANT_PARSE_EVENT,
                    cause = cause,
                ),
            ),
        )
        job.cancel()
    }

    @Test
    fun `When socket is closded locally, we should reject the event within our eventFlow`() = runTest {
        val listener = streamWebSocket.listen()
        val listResult = mutableListOf<StreamWebSocketEvent>()
        val job = launch { listener.collect { listResult.add(it) } }
        runCurrent()

        webSocketListener.onClosed(webSocket, closeSocketCode, closeSocketReason)
        runCurrent()

        listResult `should be equal to` emptyList()
        job.cancel()
    }

    @Test
    fun `When socket is closded remotelly, it should be added to our eventFlow`() = runTest {
        val listener = streamWebSocket.listen()
        val listResult = mutableListOf<StreamWebSocketEvent>()
        val job = launch { listener.collect { listResult.add(it) } }
        runCurrent()

        webSocketListener.onClosed(webSocket, closeSocketCode + randomInt(), closeSocketReason)
        runCurrent()

        listResult `should be equal to` listOf(
            StreamWebSocketEvent.Error(
                Error.NetworkError.fromChatErrorCode(
                    chatErrorCode = ChatErrorCode.SOCKET_CLOSED,
                ),
            ),
        )
        job.cancel()
    }
}
