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

package io.getstream.chat.android.client.api

import androidx.lifecycle.testing.TestLifecycleOwner
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api2.MoshiChatApi
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.clientstate.SocketStateService
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.helpers.CallPostponeHelper
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.ConnectionData
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.persistance.repository.noop.NoOpRepositoryFactory
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.SocketListener
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.utils.TokenUtils
import io.getstream.chat.android.client.utils.retry.NoRetryPolicy
import io.getstream.chat.android.test.TestCoroutineExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

internal class ClientConnectionTests {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private val userId = "test-id"
    private val connectionId = "connection-id"
    private val user = User().apply { id = userId }
    private val token = "token"

    private val config = ChatClientConfig(
        "api-key",
        "hello.http",
        "cdn.http",
        "socket.url",
        false,
        ChatLogger.Config(ChatLogLevel.NOTHING, null),
        false,
        false
    )

    private val connectedEvent = ConnectedEvent(
        EventType.HEALTH_CHECK,
        Date(),
        user,
        connectionId
    )
    private val disconnectedEvent = DisconnectedEvent(EventType.CONNECTION_DISCONNECTED, Date())

    private lateinit var api: MoshiChatApi
    private lateinit var socket: ChatSocket
    private lateinit var fileUploader: FileUploader
    private lateinit var client: ChatClient
    private lateinit var notificationsManager: ChatNotifications
    private lateinit var initCallback: Call.Callback<ConnectionData>
    private lateinit var socketListener: SocketListener

    @BeforeEach
    fun before() {
        val lifecycleOwner = TestLifecycleOwner(coroutineDispatcher = testCoroutines.dispatcher)
        val socketStateService = SocketStateService()
        val userStateService = UserStateService()
        val callPostponeHelper = CallPostponeHelper(socketStateService, testCoroutines.scope)
        val tokenUtils: TokenUtils = mock()
        whenever(tokenUtils.getUserId(token)) doReturn userId
        socket = mock()
        fileUploader = mock()
        notificationsManager = mock()
        initCallback = mock()
        api = mock()

        whenever(socket.addListener(anyOrNull())) doAnswer { invocationOnMock ->
            socketListener = invocationOnMock.getArgument(0)
            socketListener.onEvent(disconnectedEvent)
        }

        client = ChatClient(
            config,
            api,
            socket,
            notificationsManager,
            tokenManager = FakeTokenManager(token),
            socketStateService = socketStateService,
            callPostponeHelper = callPostponeHelper,
            userCredentialStorage = mock(),
            userStateService = userStateService,
            tokenUtils = tokenUtils,
            scope = testCoroutines.scope,
            retryPolicy = NoRetryPolicy(),
            appSettingsManager = mock(),
            chatSocketExperimental = mock(),
            lifecycle = lifecycleOwner.lifecycle,
            pluginFactories = emptyList(),
            repositoryFactoryProvider = NoOpRepositoryFactory.Provider,
            clientState = mock()
        )
    }

    @Test
    fun successConnection() {
        client.connectUser(user, token).enqueue()

        verify(socket, times(1)).connectUser(user, false)
    }

    @Test
    fun connectAndDisconnect() {
        client.connectUser(user, token).enqueue()
        socketListener.onEvent(connectedEvent)

        client.disconnect()

        verify(socket, times(1)).disconnect()
    }
}
