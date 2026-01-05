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

import io.getstream.chat.android.client.StreamLifecycleObserver
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.scope.UserScope
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomString
import io.getstream.result.Result
import okhttp3.WebSocketListener
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

internal class FakeChatSocket private constructor(
    private val parser: ChatParser,
    apiKey: String,
    wssUrl: String,
    tokenManager: TokenManager,
    socketFactory: SocketFactory,
    userScope: UserScope,
    lifecycleObserver: StreamLifecycleObserver,
    networkStateProvider: NetworkStateProvider,
    getWebSocketListener: () -> WebSocketListener,
) : ChatSocket(
    apiKey,
    wssUrl,
    tokenManager,
    socketFactory,
    userScope,
    lifecycleObserver,
    networkStateProvider,
) {
    private val streamDateFormatter = StreamDateFormatter()
    private val webSocketListener: WebSocketListener by lazy { getWebSocketListener() }
    private val _socketFactory: SocketFactory

    init {
        _socketFactory = socketFactory
    }

    fun mockEventReceived(event: ChatEvent) {
        val randomString = randomString()
        whenever(parser.fromJsonOrError(eq(randomString), eq(ChatEvent::class.java))) doReturn Result.Success(event)
        webSocketListener.onMessage(mock(), randomString)
    }

    suspend fun prepareAliveConnection(user: User, connectionId: String) {
        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)
        connectUser(user, false)
        mockEventReceived(ConnectedEvent(EventType.HEALTH_CHECK, createdAt, rawCreatedAt, user, connectionId))
    }

    fun verifySocketFactory(block: (SocketFactory) -> Unit) {
        block(_socketFactory)
    }

    companion object {
        operator fun invoke(
            userScope: UserScope,
            lifecycleObserver: StreamLifecycleObserver,
            apiKey: String = randomString(),
            wssUrl: String = randomString(),
            tokenManager: TokenManager = FakeTokenManager(randomString()),
            networkStateProvider: NetworkStateProvider = mock(),
        ): FakeChatSocket {
            var webSocketListener: WebSocketListener? = null
            val parser: ChatParser = mock()
            val streamWebSocket = StreamWebSocket(parser) {
                webSocketListener = it
                mock()
            }
            val socketFactory: SocketFactory = mock()
            whenever(socketFactory.createSocket(any())) doReturn streamWebSocket
            return FakeChatSocket(
                parser,
                apiKey,
                wssUrl,
                tokenManager,
                socketFactory,
                userScope,
                lifecycleObserver,
                networkStateProvider,
            ) { webSocketListener!! }
        }
    }
}
