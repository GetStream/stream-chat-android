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

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.clientstate.DisconnectCause
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.parser.EventArguments
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.persistance.repository.noop.NoOpRepositoryFactory
import io.getstream.chat.android.client.scope.ClientTestScope
import io.getstream.chat.android.client.scope.UserTestScope
import io.getstream.chat.android.client.socket.FakeChatSocket
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.client.utils.TokenUtils
import io.getstream.chat.android.client.utils.retry.NoRetryPolicy
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

internal class ChatClientTest {

    private companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        val createdAt = Date()
        val rawCreatedAt = StreamDateFormatter().format(createdAt)

        val eventA = EventArguments.randomEvent()
        val eventB = EventArguments.randomEvent()
        val eventC = EventArguments.randomEvent()

        val eventD = UnknownEvent("d", createdAt, rawCreatedAt, null, emptyMap<Any, Any>())
        val eventE = UnknownEvent("e", createdAt, rawCreatedAt, null, mapOf<Any, Any>("cid" to "myCid"))
        val eventF = UnknownEvent("f", createdAt, rawCreatedAt, null, emptyMap<Any, Any>())
    }

    private lateinit var lifecycleOwner: TestLifecycleOwner
    private lateinit var client: ChatClient
    private lateinit var fakeChatSocket: FakeChatSocket
    private lateinit var result: MutableList<ChatEvent>
    private val token = randomString()
    private val userId = randomString()
    private val user = randomUser(id = userId)
    private val tokenUtils: TokenUtils = mock()

    @BeforeEach
    fun setUp() {
        val apiKey = "api-key"
        val wssUrl = "socket.url"
        val config = ChatClientConfig(
            apiKey = apiKey,
            httpUrl = "hello.http",
            cdnHttpUrl = "cdn.http",
            wssUrl = wssUrl,
            warmUp = false,
            loggerConfig = Mother.chatLoggerConfig(),
            distinctApiCalls = false,
            debugRequests = false,
            notificationConfig = NotificationConfig(),
        )
        whenever(tokenUtils.getUserId(token)) doReturn userId
        lifecycleOwner = TestLifecycleOwner(coroutineDispatcher = testCoroutines.dispatcher)
        val clientScope = ClientTestScope(testCoroutines.scope)
        val userScope = UserTestScope(clientScope)
        val lifecycleObserver = StreamLifecycleObserver(userScope, lifecycleOwner.lifecycle)
        val tokenManager = FakeTokenManager("")
        val networkStateProvider: NetworkStateProvider = mock()
        whenever(networkStateProvider.isConnected()) doReturn true
        fakeChatSocket = FakeChatSocket(
            userScope = userScope,
            lifecycleObserver = lifecycleObserver,
            tokenManager = tokenManager,
            apiKey = apiKey,
            wssUrl = wssUrl,
            networkStateProvider = networkStateProvider,
        )
        client = ChatClient(
            config = config,
            api = mock(),
            dtoMapping = DtoMapping(NoOpMessageTransformer, NoOpUserTransformer),
            notifications = mock(),
            tokenManager = tokenManager,
            userCredentialStorage = mock(),
            userStateService = UserStateService(),
            tokenUtils = tokenUtils,
            clientScope = clientScope,
            userScope = userScope,
            retryPolicy = NoRetryPolicy(),
            appSettingsManager = mock(),
            chatSocket = fakeChatSocket,
            pluginFactories = emptyList(),
            mutableClientState = Mother.mockedClientState(),
            repositoryFactoryProvider = NoOpRepositoryFactory.Provider,
            currentUserFetcher = mock(),
            audioPlayer = mock(),
            repository = mock(),
            messageReceiptReporter = mock(),
        ).apply {
            attachmentsSender = mock()
            connectUser(user, token).enqueue()
        }

        result = mutableListOf()
    }

    @Test
    fun `Instantiate ChannelClient by type and id`() {
        val channelType = randomString()
        val channelId = randomString()
        val channelClient = client.channel(channelType, channelId)

        channelClient.channelType shouldBeEqualTo channelType
        channelClient.channelId shouldBeEqualTo channelId
    }

    @Test
    fun `Instantiate ChannelClient by cid`() {
        val cid = randomCID()
        val channelClient = client.channel(cid)

        val (type, id) = cid.cidToTypeAndId()
        channelClient.channelType shouldBeEqualTo type
        channelClient.channelId shouldBeEqualTo id
    }

    @Test
    fun `Simple subscribe for one event`() {
        client.subscribe {
            result.add(it)
        }

        fakeChatSocket.mockEventReceived(eventB)

        result shouldBeEqualTo listOf(eventB)
    }

    @Test
    fun `Simple subscribe for multiple events`() {
        client.subscribe {
            result.add(it)
        }

        fakeChatSocket.mockEventReceived(eventA)
        fakeChatSocket.mockEventReceived(eventB)
        fakeChatSocket.mockEventReceived(eventC)

        result shouldBeEqualTo listOf(eventA, eventB, eventC)
    }

    @Test
    fun `Subscribe for string event types`() {
        client.subscribeFor("d", "f") {
            result.add(it)
        }

        fakeChatSocket.mockEventReceived(eventD)
        fakeChatSocket.mockEventReceived(eventE)
        fakeChatSocket.mockEventReceived(eventF)
        fakeChatSocket.mockEventReceived(eventE)
        fakeChatSocket.mockEventReceived(eventD)

        result shouldBeEqualTo listOf(eventD, eventF, eventD)
    }

    @Test
    fun `Subscribe for Java Class event types`() {
        client.subscribeFor(eventA::class.java, eventC::class.java) {
            result.add(it)
        }

        fakeChatSocket.mockEventReceived(eventA)
        fakeChatSocket.mockEventReceived(eventB)
        fakeChatSocket.mockEventReceived(eventC)

        result shouldBeEqualTo listOf(eventA, eventC)
    }

    @Test
    fun `Subscribe for KClass event types`() {
        client.subscribeFor(eventA::class, eventC::class) {
            result.add(it)
        }

        fakeChatSocket.mockEventReceived(eventA)
        fakeChatSocket.mockEventReceived(eventB)
        fakeChatSocket.mockEventReceived(eventC)

        result shouldBeEqualTo listOf(eventA, eventC)
    }

    @Test
    fun `Subscribe for event types with type parameter`() {
        client.subscribeFor<UnknownEvent> {
            result.add(it)
        }

        fakeChatSocket.mockEventReceived(eventA)
        fakeChatSocket.mockEventReceived(eventD)
        fakeChatSocket.mockEventReceived(eventC)

        result shouldBeEqualTo listOf(eventD)
    }

    @Test
    fun `Subscribe for single string event type`() {
        client.subscribeForSingle("d") {
            result.add(it)
        }

        fakeChatSocket.mockEventReceived(eventD)
        fakeChatSocket.mockEventReceived(eventE)
        fakeChatSocket.mockEventReceived(eventD)
        fakeChatSocket.mockEventReceived(eventE)

        result shouldBeEqualTo listOf(eventD)
    }

    @Test
    fun `Subscribe for single event, with event type as type parameter`() {
        client.subscribeForSingle<UnknownEvent> {
            result.add(it)
        }

        fakeChatSocket.mockEventReceived(eventB)
        fakeChatSocket.mockEventReceived(eventD)
        fakeChatSocket.mockEventReceived(eventE)

        result shouldBeEqualTo listOf(eventD)
    }

    @Test
    fun `Unsubscribe from events`() {
        val disposable = client.subscribe {
            result.add(it)
        }

        fakeChatSocket.mockEventReceived(eventA)

        disposable.dispose()

        fakeChatSocket.mockEventReceived(eventB)
        fakeChatSocket.mockEventReceived(eventC)

        result shouldBeEqualTo listOf(eventA)
    }

    @Test
    fun `Given connected user When handle event with updated user Should updated user value`() {
        val updateUser = user.copy(
            extraData = mutableMapOf(),
            name = "updateUserName",
        )

        fakeChatSocket.mockEventReceived(Mother.randomUserPresenceChangedEvent(user = updateUser))

        client.getCurrentUser() shouldBeEqualTo updateUser
    }

    @Test
    fun `Disconnect on unrecoverable error`() = runTest {
        /* Given */
        lifecycleOwner.currentState = Lifecycle.State.RESUMED

        /* When */
        fakeChatSocket.mockEventReceived(
            DisconnectedEvent(
                EventType.CONNECTION_DISCONNECTED,
                Date(),
                rawCreatedAt = null,
                disconnectCause = DisconnectCause.UnrecoverableError(
                    Error.NetworkError(
                        statusCode = -1,
                        serverErrorCode = ChatErrorCode.VALIDATION_ERROR.code,
                        message = ChatErrorCode.VALIDATION_ERROR.description,
                    ),
                ),
            ),
        )
        delay(10L)
        lifecycleOwner.currentState = Lifecycle.State.STARTED
        delay(10L)
        lifecycleOwner.currentState = Lifecycle.State.CREATED
        delay(1000L)
        lifecycleOwner.currentState = Lifecycle.State.RESUMED

        /* Then */
        fakeChatSocket.verifySocketFactory {
            verify(it, times(1)).createSocket(any())
        }
        client.clientState.connectionState.value shouldBeEqualTo ConnectionState.Offline
    }

    @Test
    fun `Reconnect fails after unrecoverable error`() = runTest {
        /* Given */

        /* When */
        fakeChatSocket.mockEventReceived(
            DisconnectedEvent(
                EventType.CONNECTION_DISCONNECTED,
                Date(),
                rawCreatedAt = null,
                disconnectCause = DisconnectCause.UnrecoverableError(
                    Error.NetworkError(
                        statusCode = -1,
                        serverErrorCode = ChatErrorCode.VALIDATION_ERROR.code,
                        message = ChatErrorCode.VALIDATION_ERROR.description,
                    ),
                ),
            ),
        )
        delay(10L)
        client.disconnectSocket().await()
        delay(1000L)
        val result = client.reconnectSocket().await()

        /* Then */
        result shouldBeEqualTo Result.Failure(
            value = Error.GenericError(message = "Invalid user state NotSet without user being set!"),
        )
        client.getCurrentUser() shouldBeEqualTo null
        client.clientState.user.value shouldBeEqualTo null
        client.clientState.connectionState.value shouldBeEqualTo ConnectionState.Offline
        client.clientState.initializationState.value shouldBeEqualTo InitializationState.NOT_INITIALIZED
    }
}
