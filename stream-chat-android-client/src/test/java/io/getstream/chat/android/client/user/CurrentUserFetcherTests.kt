/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.user

import io.getstream.chat.android.client.Mother.randomConnectedEvent
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.Mother.randomUser
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.scope.UserScope
import io.getstream.chat.android.client.scope.UserTestScope
import io.getstream.chat.android.client.socket.ErrorResponse
import io.getstream.chat.android.client.socket.SocketErrorMessage
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.client.socket.SocketFactory
import io.getstream.chat.android.client.socket.experimental.ws.StreamWebSocket
import io.getstream.chat.android.test.randomBoolean
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
internal class CurrentUserFetcherTests {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    lateinit var userScope: UserScope
    lateinit var chatParser: ChatParser
    val networkStateProvider: NetworkStateProvider = mock()
    lateinit var fetcher: CurrentUserFetcher
    lateinit var webSocketListener: WebSocketListener
    lateinit var streamWebSocket: StreamWebSocket
    private val ws: WebSocket = mock()

    @BeforeEach
    fun before() {
        userScope = UserTestScope(testCoroutines.scope)
        chatParser = mock()
        streamWebSocket = StreamWebSocket(chatParser) {
            webSocketListener = it
            ws
        }
        val socketFactory: SocketFactory = mock()
        whenever(socketFactory.createSocket(any())) doReturn streamWebSocket
        fetcher = CurrentUserFetcher(
            networkStateProvider = networkStateProvider,
            socketFactory = socketFactory,
            ChatClientConfig(
                apiKey = randomString(),
                httpUrl = randomString(),
                cdnHttpUrl = randomString(),
                wssUrl = randomString(),
                warmUp = randomBoolean(),
                loggerConfig = mock(),
                debugRequests = randomBoolean(),
            ).apply { isAnonymous = randomBoolean() }
        )
    }

    @Test
    fun `When network is not available`() = runTest {
        /* Given */
        whenever(networkStateProvider.isConnected()) doReturn false

        /* When */
        val result = fetcher.fetch(randomUser())

        /* Then */
        result.isError `should be equal to` true
    }

    @Test
    fun `When fails due to Timeout`() = runTest {
        /* Given */
        whenever(networkStateProvider.isConnected()) doReturn true

        /* When */
        val result = fetcher.fetch(randomUser())

        /* Then */
        result.isError `should be equal to` true
        result.error().message `should be equal to` "Timeout while fetching current user"
    }

    @Test
    fun `When ws message contains error`() = runTest {
        /* Given */
        val errorMessage = SocketErrorMessage(
            ErrorResponse(message = "Error")
        )
        whenever(networkStateProvider.isConnected()) doReturn true
        whenever(chatParser.fromJsonOrError(errorMessage.toString(), ChatEvent::class.java)) doReturn Result.error(Exception())
        whenever(chatParser.fromJsonOrError(errorMessage.toString(), SocketErrorMessage::class.java)) doReturn Result.success(errorMessage)

        /* When */
        val deferredResult = testCoroutines.scope.async {
            fetcher.fetch(randomUser())
        }
        webSocketListener.onMessage(ws, errorMessage.toString())

        val result = deferredResult.await()

        /* Then */
        result.isError `should be equal to` true
        result.error() `should be equal to` errorMessage.error!!.let {
            ChatNetworkError.create(it.code, it.message, it.statusCode)
        }
    }

    @Test
    fun `When ws fails`() = runTest {
        /* Given */
        whenever(networkStateProvider.isConnected()) doReturn true
        val exception = Exception("Error")

        /* When */
        val deferredResult = testCoroutines.scope.async {
            fetcher.fetch(randomUser())
        }

        webSocketListener.onFailure(ws, exception, null)
        testCoroutines.scope.advanceTimeBy(2_000)

        val result = deferredResult.await()

        /* Then */
        result.isError `should be equal to` true
        result.error().message `should be equal to` ChatNetworkError.create(ChatErrorCode.SOCKET_FAILURE).message
    }

    @Test
    fun `When everything is ok`() = runTest {
        /* Given */
        val message = "health_check"
        val connectedEvent = randomConnectedEvent()
        whenever(networkStateProvider.isConnected()) doReturn true
        whenever(chatParser.fromJsonOrError(message, SocketErrorMessage::class.java)) doReturn Result.error(ChatError("Error"))
        whenever(chatParser.fromJsonOrError(message, ChatEvent::class.java)) doReturn Result.success(connectedEvent)

        /* When */
        val localScope = testCoroutines.scope + Job()
        val deferredResult = localScope.async {
            fetcher.fetch(randomUser())
        }
        testCoroutines.scope.advanceTimeBy(2_000)
        webSocketListener.onMessage(ws, message)
        testCoroutines.scope.advanceTimeBy(2_000)

        val result = deferredResult.await()

        /* Then */
        result.isSuccess `should be equal to` true
        result.data() `should be equal to` connectedEvent.me
    }
}
