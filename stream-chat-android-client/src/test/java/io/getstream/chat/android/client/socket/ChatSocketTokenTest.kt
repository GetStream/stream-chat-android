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

import androidx.lifecycle.testing.TestLifecycleOwner
import io.getstream.chat.android.client.StreamLifecycleObserver
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.events.ConnectionErrorEvent
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.parser2.ParserFactory
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.scope.ClientTestScope
import io.getstream.chat.android.client.scope.UserTestScope
import io.getstream.chat.android.client.token.CacheableTokenProvider
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.token.TokenManagerImpl
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.android.client.utils.HeadersUtil
import io.getstream.chat.android.client.utils.internal.ServerClockOffset
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be greater than`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

/**
 * Covers when the socket is allowed to discard the token it holds. Discarding it makes the next connection
 * attempt call back into the integrator's [io.getstream.chat.android.client.token.TokenProvider], so it must
 * only happen for authentication failures.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class ChatSocketTokenTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        /** Long enough for the health monitor to run several reconnect attempts. */
        private const val SEVERAL_RECONNECT_ATTEMPTS_MS = 120_000L

        /** Longer than the health monitor's longest retry interval, so one attempt is guaranteed to run. */
        private const val RECONNECT_ATTEMPT_MS = 30_000L

        private const val SOCKET_FAILURES = 5
    }

    private val streamDateFormatter = StreamDateFormatter()
    private lateinit var userScope: UserTestScope
    private lateinit var tokenManager: TokenManager
    private lateinit var fakeChatSocket: FakeChatSocket
    private var socketListener: WebSocketListener? = null
    private var socketsCreated = 0

    @BeforeEach
    fun setUp() {
        userScope = UserTestScope(ClientTestScope(testCoroutines.scope))
        val lifecycleOwner = TestLifecycleOwner(coroutineDispatcher = testCoroutines.dispatcher)
        val networkStateProvider: NetworkStateProvider = mock()
        whenever(networkStateProvider.isConnected()) doReturn true
        tokenManager = mock()
        whenever(tokenManager.hasTokenProvider()) doReturn true
        whenever(tokenManager.hasToken()) doReturn true
        whenever(tokenManager.getToken()) doReturn randomString()
        fakeChatSocket = FakeChatSocket(
            userScope = userScope,
            lifecycleObserver = StreamLifecycleObserver(userScope, lifecycleOwner.lifecycle),
            tokenManager = tokenManager,
            networkStateProvider = networkStateProvider,
        )
        userScope.launch { fakeChatSocket.prepareAliveConnection(randomUser(), randomString()) }
    }

    @AfterEach
    fun tearDown() {
        // The socket keeps rescheduling reconnect attempts, so the scheduler never goes idle on its own.
        userScope.cancel()
    }

    @Test
    fun `reconnecting after a transport error keeps the token`() {
        socketFails(ChatErrorCode.NETWORK_FAILED.code)

        testCoroutines.dispatcher.scheduler.advanceTimeBy(SEVERAL_RECONNECT_ATTEMPTS_MS)

        verify(tokenManager, never()).expireToken()
    }

    @Test
    fun `an authentication error expires the token`() {
        socketFails(ChatErrorCode.TOKEN_EXPIRED.code)

        verify(tokenManager, atLeastOnce()).expireToken()
    }

    /**
     * Wires the real token stack ([TokenManagerImpl] + [CacheableTokenProvider]) and a real [SocketFactory] so the
     * assertion is on what an integrator actually observes: how many times their [TokenProvider] is called while the
     * socket retries.
     */
    @Test
    fun `a reconnect storm does not call the token provider`() {
        var loadTokenCalls = 0
        val countingProvider = object : TokenProvider {
            override fun loadToken(): String {
                loadTokenCalls++
                return randomString()
            }
        }
        val tokenManager = TokenManagerImpl().apply {
            setTokenProvider(CacheableTokenProvider(countingProvider))
        }
        val chatSocket = realSocket(tokenManager)

        userScope.launch { chatSocket.connectUser(randomUser(), false) }
        testCoroutines.dispatcher.scheduler.runCurrent()
        // The initial connection must have happened, otherwise the failures below land on nothing.
        loadTokenCalls `should be equal to` 1
        socketsCreated `should be equal to` 1

        repeat(SOCKET_FAILURES) {
            checkNotNull(socketListener).onFailure(mock(), Throwable(randomString()), null)
            testCoroutines.dispatcher.scheduler.advanceTimeBy(RECONNECT_ATTEMPT_MS)
        }

        socketsCreated `should be greater than` 1
        loadTokenCalls `should be equal to` 1
    }

    private fun realSocket(tokenManager: TokenManager): ChatSocket {
        val parser: ChatParser = ParserFactory.createMoshiChatParser()
        val headersUtil: HeadersUtil = mock()
        whenever(headersUtil.buildSdkTrackingHeaders()) doReturn randomString()
        val httpClient: OkHttpClient = mock()
        whenever(httpClient.newWebSocket(any(), any())) doAnswer { invocation ->
            socketsCreated++
            socketListener = invocation.getArgument(1)
            mock<WebSocket>()
        }
        val networkStateProvider: NetworkStateProvider = mock()
        whenever(networkStateProvider.isConnected()) doReturn true
        val lifecycleOwner = TestLifecycleOwner(coroutineDispatcher = testCoroutines.dispatcher)
        return ChatSocket(
            apiKey = randomString(),
            wssUrl = "https://" + randomString() + "/",
            tokenManager = tokenManager,
            socketFactory = SocketFactory(parser, tokenManager, headersUtil, httpClient),
            userScope = userScope,
            lifecycleObserver = StreamLifecycleObserver(userScope, lifecycleOwner.lifecycle),
            networkStateProvider = networkStateProvider,
            serverClockOffset = ServerClockOffset(),
        )
    }

    private fun socketFails(serverErrorCode: Int) {
        val createdAt = Date()
        fakeChatSocket.mockEventReceived(
            ConnectionErrorEvent(
                type = EventType.CONNECTION_ERROR,
                createdAt = createdAt,
                rawCreatedAt = streamDateFormatter.format(createdAt),
                connectionId = randomString(),
                error = ChatError(code = serverErrorCode, message = randomString()),
            ),
        )
    }
}
