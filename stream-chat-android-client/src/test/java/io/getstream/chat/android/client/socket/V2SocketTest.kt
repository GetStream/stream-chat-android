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

import io.getstream.chat.android.DeliveryReceipts
import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.ReadReceipts
import io.getstream.chat.android.TypingIndicators
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.parser2.ParserFactory
import io.getstream.chat.android.client.parser2.V2EventParser
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.client.utils.HeadersUtil
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.User
import io.kotest.assertions.json.shouldEqualJson
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

/** The websocket: its connect request, the auth frame it sends once open, its keepalive and its event parsing. */
internal class V2SocketTest {

    private val webSocket: WebSocket = mock()
    private val httpClient: OkHttpClient = mock<OkHttpClient>().apply {
        whenever(newWebSocket(any(), any())) doReturn webSocket
    }
    private val headersUtil: HeadersUtil = mock<HeadersUtil>().apply {
        whenever(buildSdkTrackingHeaders()) doReturn "stream-chat-android-v1.0"
    }
    private val socketFactory = SocketFactory(
        parser = ParserFactory.createMoshiChatParser(),
        tokenManager = FakeTokenManager(TOKEN),
        headersUtil = headersUtil,
        httpClient = httpClient,
        eventParser = V2EventParser(
            DomainMapping(
                currentUserIdProvider = { USER.id },
                channelTransformer = NoOpChannelTransformer,
                messageTransformer = NoOpMessageTransformer,
                userTransformer = NoOpUserTransformer,
                hidesStoredDeclaredCustom = true,
            ),
        ),
    )

    /** Creates the socket, opens it and returns the request and the listener it was created with. */
    private fun open(conf: SocketFactory.ConnectionConf): Pair<Request, WebSocketListener> {
        socketFactory.createSocket(conf)
        val request = argumentCaptor<Request>()
        val listener = argumentCaptor<WebSocketListener>()
        verify(httpClient).newWebSocket(request.capture(), listener.capture())
        listener.firstValue.onOpen(webSocket, mock<Response>())
        return request.firstValue to listener.firstValue
    }

    private fun sentFrame(): String = argumentCaptor<String>().also { verify(webSocket).send(it.capture()) }.firstValue

    @Test
    fun `A user connects through the v2 endpoint without an Authorization header`() {
        val (request, _) = open(SocketFactory.ConnectionConf.UserConnectionConf(ENDPOINT, API_KEY, USER))

        // OkHttp keeps a wss URL as https.
        request.url.toString() shouldBeEqualTo "https://chat.example.com/api/v2/connect?api_key=$API_KEY" +
            "&stream-auth-type=jwt&X-Stream-Client=stream-chat-android-v1.0"
        request.header("X-Stream-Client") shouldBeEqualTo "stream-chat-android-v1.0"
        request.header("Authorization").shouldBeNull()
    }

    @Test
    fun `The first connection authenticates with the user details v2 reads, custom data nested`() {
        open(SocketFactory.ConnectionConf.UserConnectionConf(ENDPOINT, API_KEY, USER))

        sentFrame() shouldEqualJson """{"token":"$TOKEN","products":["chat"],"user_details":{"id":"filip",
            "name":"Chewbacca","image":"https://example.com/chewie.png","language":"en","invisible":true,
            "privacy_settings":{"typing_indicators":{"enabled":false}},"custom":{"flair":"gold"}}}"""
    }

    @Test
    fun `The first connection sends every privacy setting it has, and leaves out the unset ones`() {
        val user = USER.copy(
            privacySettings = PrivacySettings(
                typingIndicators = null,
                deliveryReceipts = DeliveryReceipts(enabled = false),
                readReceipts = ReadReceipts(enabled = true),
            ),
        )
        open(SocketFactory.ConnectionConf.UserConnectionConf(ENDPOINT, API_KEY, user))

        sentFrame() shouldEqualJson """{"token":"$TOKEN","products":["chat"],"user_details":{"id":"filip",
            "name":"Chewbacca","image":"https://example.com/chewie.png","language":"en","invisible":true,
            "privacy_settings":{"delivery_receipts":{"enabled":false},"read_receipts":{"enabled":true}},
            "custom":{"flair":"gold"}}}"""
    }

    @Test
    fun `A reconnection authenticates with the user id only`() {
        open(SocketFactory.ConnectionConf.UserConnectionConf(ENDPOINT, API_KEY, USER).asReconnectionConf())

        sentFrame() shouldEqualJson """{"token":"$TOKEN","products":["chat"],"user_details":{"id":"filip"}}"""
    }

    @Test
    fun `An expired token is reloaded before the auth frame is built`() {
        val tokenManager = FakeTokenManager(TOKEN, loadSyncToken = "fresh").apply { expireToken() }
        val factory = SocketFactory(
            parser = ParserFactory.createMoshiChatParser(),
            tokenManager = tokenManager,
            headersUtil = headersUtil,
            eventParser = mock(),
            httpClient = httpClient,
        )
        factory.createSocket(SocketFactory.ConnectionConf.UserConnectionConf(ENDPOINT, API_KEY, USER).asReconnectionConf())
        val listener = argumentCaptor<WebSocketListener>().also { verify(httpClient).newWebSocket(any(), it.capture()) }
        listener.firstValue.onOpen(webSocket, mock<Response>())

        sentFrame() shouldEqualJson """{"token":"fresh","products":["chat"],"user_details":{"id":"filip"}}"""
    }

    @Test
    fun `An anonymous user connects with the anonymous auth type and placeholder token`() {
        val user = User(id = "!anon")
        val (request, _) = open(SocketFactory.ConnectionConf.AnonymousConnectionConf(ENDPOINT, API_KEY, user))

        request.url.queryParameter("stream-auth-type") shouldBeEqualTo "anonymous"
        sentFrame() shouldEqualJson """{"token":"anon","products":["chat"],"user_details":{"id":"anon"}}"""
    }

    @Test
    fun `The keepalive is a minimal health check for the connection`() {
        val socket = socketFactory.createSocket(
            SocketFactory.ConnectionConf.UserConnectionConf(ENDPOINT, API_KEY, USER).asReconnectionConf(),
        )
        val connected = ConnectedEvent("health.check", Date(), "", USER, "conn-1")

        socket.sendKeepAlive(connected)

        sentFrame() shouldEqualJson """[{"type":"health.check","client_id":"conn-1"}]"""
    }

    @Test
    fun `Frames parse with the v2 parser, ignored ones emit nothing`() = runTest(UnconfinedTestDispatcher()) {
        val socket = socketFactory.createSocket(
            SocketFactory.ConnectionConf.UserConnectionConf(ENDPOINT, API_KEY, USER).asReconnectionConf(),
        )
        val listener = argumentCaptor<WebSocketListener>().also { verify(httpClient).newWebSocket(any(), it.capture()) }
        val events = mutableListOf<StreamWebSocketEvent>()
        val job = launch { socket.listen().toList(events) }

        listener.firstValue.onMessage(webSocket, """{"type":"feeds.feed.created","created_at":1791976405614650759}""")
        listener.firstValue.onMessage(
            webSocket,
            """{"type":"health.check","connection_id":"conn-1","cid":"*","created_at":1791976405614650759,"custom":{}}""",
        )
        listener.firstValue.onMessage(webSocket, """{"type":"message.new"}""")
        job.cancel()

        events.size shouldBeEqualTo 2
        (events[0] as StreamWebSocketEvent.Message).chatEvent.shouldBeInstanceOf<HealthEvent>()
        events[1].shouldBeInstanceOf<StreamWebSocketEvent.Error>()
    }

    private companion object {
        private const val ENDPOINT = "wss://chat.example.com/"
        private const val API_KEY = "key"
        private const val TOKEN = "token"
        private val USER = User(
            id = "filip",
            role = "user",
            name = "Chewbacca",
            image = "https://example.com/chewie.png",
            language = "en",
            invisible = true,
            privacySettings = PrivacySettings(typingIndicators = TypingIndicators(enabled = false)),
            extraData = mapOf("flair" to "gold", "devices" to listOf(mapOf("id" to "tok"))),
        )
    }
}
