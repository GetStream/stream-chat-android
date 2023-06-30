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
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.scope.UserScope
import io.getstream.chat.android.client.scope.UserTestScope
import io.getstream.chat.android.client.socket.ErrorResponse
import io.getstream.chat.android.client.socket.SocketErrorMessage
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.WebSocket
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
    lateinit var httpClient: OkHttpClient
    lateinit var networkStateProvider: NetworkStateProvider
    lateinit var urlBuilder: CurrentUserUrlBuilder
    lateinit var fetcher: CurrentUserFetcherImpl

    @BeforeEach
    fun before() {
        userScope = UserTestScope(testCoroutines.scope)
        networkStateProvider = mock()
        httpClient = mock()
        chatParser = mock()
        urlBuilder = mock()
        fetcher = CurrentUserFetcherImpl(
            userScope = userScope,
            httpClient = httpClient,
            networkStateProvider = networkStateProvider,
            urlBuilder = urlBuilder,
            chatParser = chatParser
        )
    }

    @Test
    fun `When network is not available`() = runTest {
        /* Given */
        whenever(networkStateProvider.isConnected()) doReturn false

        /* When */
        val result = fetcher.fetch()

        /* Then */
        result.isFailure `should be equal to` true
    }

    @Test
    fun `When fails de to Timeout`() = runTest {
        /* Given */
        val ws: WebSocket = mock()
        whenever(networkStateProvider.isConnected()) doReturn true
        whenever(urlBuilder.buildUrl()) doReturn "wss://test.com"
        whenever(httpClient.newWebSocket(any(), any())) doReturn ws

        /* When */
        val result = fetcher.fetch()
        testCoroutines.scope.advanceTimeBy(20_000)

        /* Then */
        result.isFailure `should be equal to` true
        result.errorOrNull()?.message `should be equal to` "Timeout while fetching current user"
    }

    @Test
    fun `When ws message contains error`() = runTest {
        /* Given */
        val ws: WebSocket = mock()
        val errorMessage = SocketErrorMessage(
            ErrorResponse(message = "Error")
        )
        whenever(networkStateProvider.isConnected()) doReturn true
        whenever(urlBuilder.buildUrl()) doReturn "wss://test.com"
        whenever(httpClient.newWebSocket(any(), any())) doReturn ws
        whenever(chatParser.fromJsonOrError(errorMessage.toString(), SocketErrorMessage::class.java)) doReturn Result.Success(errorMessage)

        /* When */
        val localScope = testCoroutines.scope + Job()
        val deferredResult = localScope.async {
            fetcher.fetch()
        }
        testCoroutines.scope.advanceTimeBy(2_000)
        fetcher.onMessage(ws, errorMessage.toString())
        testCoroutines.scope.advanceTimeBy(2_000)

        val result = deferredResult.await()

        /* Then */
        result.isFailure `should be equal to` true
        result.errorOrNull() `should be equal to` errorMessage.error!!.let {
            Error.NetworkError(serverErrorCode = it.code, message = it.message, statusCode = it.statusCode)
        }
    }

    @Test
    fun `When ws fails`() = runTest {
        /* Given */
        val ws: WebSocket = mock()
        whenever(networkStateProvider.isConnected()) doReturn true
        whenever(urlBuilder.buildUrl()) doReturn "wss://test.com"
        whenever(httpClient.newWebSocket(any(), any())) doReturn ws

        /* When */
        val localScope = testCoroutines.scope + Job()
        val deferredResult = localScope.async {
            fetcher.fetch()
        }
        testCoroutines.scope.advanceTimeBy(2_000)
        fetcher.onFailure(ws, Exception("Error"), null)
        testCoroutines.scope.advanceTimeBy(2_000)

        val result = deferredResult.await()

        /* Then */
        result.isFailure `should be equal to` true
        result.errorOrNull() `should be equal to` ChatErrorCode.SOCKET_CLOSED.let {
            Error.NetworkError(serverErrorCode = it.code, message = it.description)
        }
    }

    @Test
    fun `When everything is ok`() = runTest {
        /* Given */
        val ws: WebSocket = mock()
        val message = "health_check"
        val connectedEvent = randomConnectedEvent()
        whenever(networkStateProvider.isConnected()) doReturn true
        whenever(urlBuilder.buildUrl()) doReturn "wss://test.com"
        whenever(httpClient.newWebSocket(any(), any())) doReturn ws
        whenever(chatParser.fromJsonOrError(message, SocketErrorMessage::class.java)) doReturn Result.Failure(Error.GenericError("Error"))
        whenever(chatParser.fromJsonOrError(message, ChatEvent::class.java)) doReturn Result.Success(connectedEvent)

        /* When */
        val localScope = testCoroutines.scope + Job()
        val deferredResult = localScope.async {
            fetcher.fetch()
        }
        testCoroutines.scope.advanceTimeBy(2_000)
        fetcher.onMessage(ws, message)
        testCoroutines.scope.advanceTimeBy(2_000)

        val result = deferredResult.await()

        /* Then */
        result.isSuccess `should be equal to` true
        result.getOrThrow() `should be equal to` connectedEvent.me
    }
}
