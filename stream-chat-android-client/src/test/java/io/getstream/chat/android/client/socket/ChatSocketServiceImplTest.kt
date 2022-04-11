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

import io.getstream.chat.android.client.Mother.randomUser
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.randomString
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ChatSocketServiceImplTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var tokenManager: TokenManager
    private lateinit var socketFactory: SocketFactory
    private lateinit var chatParser: ChatParser
    private lateinit var networkStateProvider: NetworkStateProvider
    private lateinit var socketListener: SocketListener
    private lateinit var socketService: ChatSocketServiceImpl

    @BeforeEach
    fun setup() {
        tokenManager = mock()
        socketFactory = mock()
        chatParser = mock()
        networkStateProvider = mock()
        socketListener = mock()
        socketService = ChatSocketServiceImpl(
            tokenManager,
            socketFactory,
            networkStateProvider,
            chatParser,
            testCoroutines.scope
        )
        socketService.addListener(socketListener)
    }

    @Test
    fun `Should start connecting to socket when connecting and network connectivity exists`() {
        whenever(networkStateProvider.isConnected()) doReturn true

        socketService.userConnect(randomString(), randomString(), randomUser())

        socketService.state shouldBeEqualTo ChatSocketServiceImpl.State.Connecting
    }

    @Test
    fun `Should start connecting to socket when connecting was called and connection was recovered`() {
        whenever(networkStateProvider.isConnected()) doReturn false
        whenever(networkStateProvider.subscribe(any())) doAnswer {
            it.getArgument<NetworkStateProvider.NetworkStateListener>(0).onConnected()
        }

        socketService.userConnect(randomString(), randomString(), randomUser())

        socketService.state shouldBeEqualTo ChatSocketServiceImpl.State.Connecting
    }

    @Test
    fun `Should not start connecting to socket when connecting and there is no network connectivity`() {
        whenever(networkStateProvider.isConnected()) doReturn false

        socketService.userConnect(randomString(), randomString(), randomUser())

        socketService.state shouldBeEqualTo ChatSocketServiceImpl.State.NetworkDisconnected
    }

    @Test
    fun `Should start connecting to socket when connecting with anymous user and network connectivity exists`() {
        whenever(networkStateProvider.isConnected()) doReturn true

        socketService.anonymousConnect(randomString(), randomString())

        socketService.state shouldBeEqualTo ChatSocketServiceImpl.State.Connecting
    }

    @Test
    fun `Should start connecting to socket when connecting with anymous user  was called and connection was recovered`() {
        whenever(networkStateProvider.isConnected()) doReturn false
        whenever(networkStateProvider.subscribe(any())) doAnswer {
            it.getArgument<NetworkStateProvider.NetworkStateListener>(0).onConnected()
        }

        socketService.anonymousConnect(randomString(), randomString())

        socketService.state shouldBeEqualTo ChatSocketServiceImpl.State.Connecting
    }

    @Test
    fun `Should not start connecting to socket when connecting with anymous user  and there is no network connectivity`() {
        whenever(networkStateProvider.isConnected()) doReturn false

        socketService.anonymousConnect(randomString(), randomString())

        socketService.state shouldBeEqualTo ChatSocketServiceImpl.State.NetworkDisconnected
    }

    @Test
    fun `Should retry to connect`() {
        whenever(networkStateProvider.isConnected()) doReturn true

        val networkError = ChatNetworkError.create(
            cause = null,
            code = ChatErrorCode.NO_ERROR_BODY,
            statusCode = 500,
        )

        socketService.anonymousConnect(randomString(), randomString())
        socketService.state shouldBeEqualTo ChatSocketServiceImpl.State.Connecting

        whenever(networkStateProvider.isConnected()) doReturn false
        socketService.onSocketError(networkError)

        // Socket was recreated
        verify(socketFactory, times(2)).createAnonymousSocket(any(), any(), any())
    }
}
