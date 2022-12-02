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

import com.flextrade.jfixture.JFixture
import com.flextrade.kfixture.KFixture
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLoggerConfig
import io.getstream.chat.android.client.logger.ChatLoggerHandler
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.socket.SocketFactory
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.PushProvider
import io.getstream.chat.android.models.User
import io.getstream.chat.android.test.randomBoolean
import io.getstream.chat.android.test.randomDate
import io.getstream.chat.android.test.randomInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.mockito.kotlin.mock
import java.util.Date
import java.util.UUID

internal object Mother {
    private val fixture: JFixture
        get() = JFixture()

    private val streamDateFormatter = StreamDateFormatter()

    fun randomAttachment(attachmentBuilder: Attachment.() -> Unit = { }): Attachment {
        return KFixture(fixture) {
            sameInstance(Attachment.UploadState::class.java, Attachment.UploadState.Success)
        } <Attachment>().apply(attachmentBuilder)
    }

    fun randomChannel(channelBuilder: Channel.() -> Unit = { }): Channel {
        return KFixture(fixture) {
            sameInstance(Mute::class.java, mock())
            sameInstance(Message::class.java, mock())
            sameInstance(Attachment.UploadState::class.java, Attachment.UploadState.Success)
        } <Channel>().apply(channelBuilder)
    }

    fun randomUser(userBuilder: User.() -> Unit = { }): User {
        return KFixture(fixture) {
            sameInstance(Mute::class.java, mock())
        } <User>().apply(userBuilder)
    }

    fun randomString(): String = UUID.randomUUID().toString()

    fun randomDevice(
        token: String = randomString(),
        pushProvider: PushProvider = PushProvider.values().random(),
        providerName: String? = randomString().takeIf { randomBoolean() },
    ): Device =
        Device(
            token = token,
            pushProvider = pushProvider,
            providerName = providerName,
        )

    fun randomUserPresenceChangedEvent(user: User = randomUser()): UserPresenceChangedEvent {
        return KFixture(fixture) {
            sameInstance(User::class.java, user)
        }()
    }

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

    fun mockedClientState(): ClientState {
        return object : ClientState {

            override val initializationState: StateFlow<InitializationState> =
                MutableStateFlow(InitializationState.COMPLETE)

            override val connectionState: StateFlow<ConnectionState> = MutableStateFlow(ConnectionState.CONNECTED)

            override val isOnline: Boolean = true

            override val isOffline: Boolean = false

            override val isConnecting: Boolean = false

            override val isInitialized: Boolean = true

            override val isNetworkAvailable: Boolean = true
        }
    }

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

    fun randomChatNetworkError(
        streamCode: Int = randomInt(),
        description: String = randomString(),
        statusCode: Int = randomInt(),
        cause: Throwable? = null,
    ): ChatError.NetworkError = ChatError.NetworkError(
        message = description,
        streamCode = streamCode,
        statusCode = statusCode,
        cause = cause,
    )
}
