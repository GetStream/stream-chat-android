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
import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.StreamLifecycleObserver
import io.getstream.chat.android.client.api2.MoshiChatApi
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.clientstate.SocketStateService
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.models.ConnectionData
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.persistance.repository.noop.NoOpRepositoryFactory
import io.getstream.chat.android.client.scope.ClientTestScope
import io.getstream.chat.android.client.scope.UserTestScope
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.SocketListener
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.TokenUtils
import io.getstream.chat.android.client.utils.retry.NoRetryPolicy
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.randomBoolean
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
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

@OptIn(ExperimentalCoroutinesApi::class)
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
    private val createdAt = Date()

    private val streamDateFormatter = StreamDateFormatter()

    private val rawCreatedAt = streamDateFormatter.format(createdAt)

    private val config = ChatClientConfig(
        "api-key",
        "hello.http",
        "cdn.http",
        "socket.url",
        false,
        Mother.chatLoggerConfig(),
        false,
        false,
        NotificationConfig(),
    )

    private val connectedEvent = ConnectedEvent(
        EventType.HEALTH_CHECK,
        createdAt,
        rawCreatedAt,
        user,
        connectionId
    )
    private val disconnectedEvent = DisconnectedEvent(EventType.CONNECTION_DISCONNECTED, Date(), null)

    private lateinit var api: MoshiChatApi
    private lateinit var socket: ChatSocket
    private lateinit var fileUploader: FileUploader
    private lateinit var client: ChatClient
    private lateinit var notificationsManager: ChatNotifications
    private lateinit var initCallback: Call.Callback<ConnectionData>
    private lateinit var socketListener: SocketListener
    private val clientState = mock<ClientState>()

    @BeforeEach
    fun before() {
        val clientScope = ClientTestScope(testCoroutines.scope)
        val userScope = UserTestScope(clientScope)
        val lifecycleOwner = TestLifecycleOwner(coroutineDispatcher = testCoroutines.dispatcher)
        val socketStateService = SocketStateService()
        val userStateService = UserStateService()
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

        whenever(clientState.user) doReturn MutableStateFlow(user)

        client = ChatClient(
            config,
            api = api,
            socket = socket,
            notifications = notificationsManager,
            tokenManager = FakeTokenManager(token),
            socketStateService = socketStateService,
            userCredentialStorage = mock(),
            userStateService = userStateService,
            tokenUtils = tokenUtils,
            clientScope = clientScope,
            userScope = userScope,
            retryPolicy = NoRetryPolicy(),
            appSettingsManager = mock(),
            chatSocketExperimental = mock(),
            lifecycleObserver = StreamLifecycleObserver(userScope, lifecycleOwner.lifecycle),
            pluginFactories = emptyList(),
            repositoryFactoryProvider = NoOpRepositoryFactory.Provider,
            clientState = clientState,
            currentUserFetcher = mock(),
        )
    }

    @Test
    fun successConnection() {
        client.connectUser(user, token).enqueue()

        verify(socket, times(1)).connectUser(user, false)
    }

    @Test
    fun connectAndDisconnect() = runTest {
        client.connectUser(user, token).enqueue()
        socketListener.onEvent(connectedEvent)

        val result = client.disconnect(flushPersistence = false).await()

        result `should be equal to` Result.success(Unit)
        verify(socket, times(1)).disconnect()
    }

    @Test
    fun `Should return a failure if try to disconnect without a connected user`() = runTest {
        val result = client.disconnect(randomBoolean()).await()

        result `should be equal to` Result.error(ChatError("ChatClient can't be disconnected because user wasn't connected previously"))
    }

    @Test
    fun `switchUser should return and also connect the socket`() = runTest {
        val timeout = 10L

        val result = client.connectUser(user, token, timeout).await()
        result.isError `should be equal to` true
        result.error().message `should be equal to` "Connection wasn't established in ${timeout}ms"

        val result2 = client.switchUser(user, token, timeout).await()
        result2.isError `should be equal to` true
        result2.error().message `should be equal to` "Connection wasn't established in ${timeout}ms"

        verify(socket, times(2)).connectUser(user, false)
    }
}
