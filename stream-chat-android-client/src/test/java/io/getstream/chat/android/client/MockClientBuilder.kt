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

import androidx.lifecycle.testing.TestLifecycleOwner
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api2.MoshiChatApi
import io.getstream.chat.android.client.clientstate.SocketStateService
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.persistance.repository.noop.NoOpRepositoryFactory
import io.getstream.chat.android.client.scope.ClientTestScope
import io.getstream.chat.android.client.scope.UserTestScope
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.utils.TokenUtils
import io.getstream.chat.android.client.utils.observable.FakeSocket
import io.getstream.chat.android.client.utils.retry.NoRetryPolicy
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.util.Date

/**
 * Used for integrations tests.
 * Initialises mock internals of [ChatClient]
 */
@ExperimentalCoroutinesApi
internal class MockClientBuilder(
    private val testCoroutineExtension: TestCoroutineExtension,
) {

    private val streamDateFormatter = StreamDateFormatter()

    val userId = "jc"
    val connectionId = "connection-id"
    val apiKey = "api-key"
    val channelType = "channel-type"
    val channelId = "channel-id"
    val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ==.devtoken"
    val serverErrorCode = 500
    val user = User().apply { id = userId }
    val createdAt = Date()
    val rawCreatedAt = streamDateFormatter.format(createdAt)
    val connectedEvent = ConnectedEvent(
        EventType.HEALTH_CHECK,
        createdAt,
        rawCreatedAt,
        user,
        connectionId
    )

    private lateinit var socket: FakeSocket
    private lateinit var fileUploader: FileUploader

    lateinit var api: MoshiChatApi
    private lateinit var notificationsManager: ChatNotifications
    private lateinit var client: ChatClient

    fun build(): ChatClient {
        val config = ChatClientConfig(
            apiKey,
            "hello.http",
            "cdn.http",
            "socket.url",
            false,
            Mother.chatLoggerConfig(),
            false,
            false
        )

        val lifecycleOwner = TestLifecycleOwner(coroutineDispatcher = testCoroutineExtension.dispatcher)

        val tokenUtil: TokenUtils = mock()
        Mockito.`when`(tokenUtil.getUserId(token)) doReturn userId
        socket = FakeSocket()
        fileUploader = mock()
        notificationsManager = mock()

        api = mock()

        val socketStateService = SocketStateService()
        val userStateService = UserStateService()
        val clientScope = ClientTestScope(testCoroutineExtension.scope)
        val userScope = UserTestScope(clientScope)
        client = ChatClient(
            config,
            api,
            socket,
            notificationsManager,
            tokenManager = FakeTokenManager(token),
            socketStateService = socketStateService,
            userCredentialStorage = mock(),
            userStateService = userStateService,
            tokenUtils = tokenUtil,
            clientScope = clientScope,
            userScope = userScope,
            retryPolicy = NoRetryPolicy(),
            appSettingsManager = mock(),
            chatSocketExperimental = mock(),
            lifecycleObserver = StreamLifecycleObserver(lifecycleOwner.lifecycle),
            pluginFactories = emptyList(),
            repositoryFactoryProvider = NoOpRepositoryFactory.Provider,
            clientState = mock(),
            currentUserFetcher = mock(),
        )

        client.connectUser(user, token).enqueue()

        socket.sendEvent(connectedEvent)

        return client.apply {
            plugins = mutableListOf()
        }
    }
}
