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

package io.getstream.chat.android.client

import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLoggerConfig
import io.getstream.chat.android.client.logger.ChatLoggerHandler
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.setup.state.internal.MutableClientState
import io.getstream.chat.android.client.socket.ChatSocketStateService
import io.getstream.chat.android.client.socket.SocketFactory
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

internal object Mother {
    private val streamDateFormatter = StreamDateFormatter()

    fun randomUserPresenceChangedEvent(
        type: String = randomString(),
        createdAt: Date = randomDate(),
        rawCreatedAt: String = randomString(),
        user: User = randomUser(),
    ): UserPresenceChangedEvent =
        UserPresenceChangedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt,
            user = user,
        )

    fun randomUserConnectionConf(
        endpoint: String = randomString(),
        apiKey: String = randomString(),
        user: User = randomUser(),
    ) = SocketFactory.ConnectionConf.UserConnectionConf(endpoint, apiKey, user)

    fun randomAnonymousConnectionConf(
        endpoint: String = randomString(),
        apiKey: String = randomString(),
        user: User = randomUser(),
    ) = SocketFactory.ConnectionConf.UserConnectionConf(endpoint, apiKey, user)

    fun randomConnectionConf(
        endpoint: String = randomString(),
        apiKey: String = randomString(),
        user: User = randomUser(),
    ) = when (randomBoolean()) {
        true -> randomAnonymousConnectionConf(endpoint, apiKey, user)
        false -> randomUserConnectionConf(endpoint, apiKey, user)
    }

    fun mockedClientState(): MutableClientState {
        val networkStatProvider: NetworkStateProvider = mock()
        whenever(networkStatProvider.isConnected()) doReturn true
        return MutableClientState(networkStatProvider).apply {
            setConnectionState(ConnectionState.Connected)
            setInitializationState(InitializationState.COMPLETE)
        }
    }

    fun randomConnectionType(): ChatSocketStateService.ConnectionType =
        ChatSocketStateService.ConnectionType.values().random()

    fun chatLoggerConfig(): ChatLoggerConfig = object : ChatLoggerConfig {
        override val level: ChatLogLevel = ChatLogLevel.NOTHING
        override val handler: ChatLoggerHandler? = null
    }

    fun randomConnectedEvent(
        type: String = randomString(),
        createdAt: Date = randomDate(),
        me: User = randomUser(),
        connectionId: String = randomString(),
    ): ConnectedEvent {
        return ConnectedEvent(type, createdAt, streamDateFormatter.format(createdAt), me, connectionId)
    }
}
