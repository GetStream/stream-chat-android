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
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.clientstate.SocketStateService
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.persistance.repository.noop.NoOpRepositoryFactory
import io.getstream.chat.android.client.plugin.factory.PluginFactory
import io.getstream.chat.android.client.scope.ClientTestScope
import io.getstream.chat.android.client.scope.UserTestScope
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.TokenUtils
import io.getstream.chat.android.client.utils.observable.FakeSocket
import io.getstream.chat.android.client.utils.retry.NoRetryPolicy
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

internal class ChatClientTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        val createdAt = Date()
        val rawCreatedAt = StreamDateFormatter().format(createdAt)

        val eventA = ConnectedEvent(EventType.HEALTH_CHECK, createdAt, rawCreatedAt, User(), "")
        val eventB = NewMessageEvent(
            EventType.MESSAGE_NEW,
            createdAt,
            rawCreatedAt,
            User(),
            "type:id",
            "type",
            "id",
            Message(),
            0,
            0,
            0
        )
        val eventC = DisconnectedEvent(EventType.CONNECTION_DISCONNECTED, Date(), rawCreatedAt = null)

        val eventD = UnknownEvent("d", createdAt, rawCreatedAt, null, emptyMap<Any, Any>())
        val eventE = UnknownEvent("e", createdAt, rawCreatedAt, null, mapOf<Any, Any>("cid" to "myCid"))
        val eventF = UnknownEvent("f", createdAt, rawCreatedAt, null, emptyMap<Any, Any>())
    }

    lateinit var api: ChatApi
    lateinit var socket: FakeSocket
    lateinit var client: ChatClient
    lateinit var result: MutableList<ChatEvent>
    val token = randomString()
    val userId = randomString()
    val user = Mother.randomUser { id = userId }
    val tokenUtils: TokenUtils = mock()
    var pluginFactories: List<PluginFactory> = emptyList()
    private val streamDateFormatter = StreamDateFormatter()

    @BeforeEach
    fun setUp() {
        val lifecycleOwner = TestLifecycleOwner(coroutineDispatcher = testCoroutines.dispatcher)
        val config = ChatClientConfig(
            "api-key",
            "hello.http",
            "cdn.http",
            "socket.url",
            false,
            Mother.chatLoggerConfig(),
            false,
            false
        )
        whenever(tokenUtils.getUserId(token)) doReturn userId
        api = mock()
        socket = FakeSocket()
        val socketStateService = SocketStateService()
        val userStateService = UserStateService()
        val clientScope = ClientTestScope(testCoroutines.scope)
        val userScope = UserTestScope(clientScope)

        client = ChatClient(
            config = config,
            api = api,
            socket = socket,
            notifications = mock(),
            tokenManager = FakeTokenManager(""),
            socketStateService = socketStateService,
            userCredentialStorage = mock(),
            userStateService = userStateService,
            tokenUtils = tokenUtils,
            clientScope = clientScope,
            userScope = userScope,
            retryPolicy = NoRetryPolicy(),
            appSettingsManager = mock(),
            chatSocketExperimental = mock(),
            lifecycleObserver = StreamLifecycleObserver(lifecycleOwner.lifecycle),
            pluginFactories = pluginFactories,
            repositoryFactoryProvider = NoOpRepositoryFactory.Provider,
            clientState = Mother.mockedClientState(),
            currentUserFetcher = mock(),
        ).apply {
            connectUser(user, token).enqueue()
        }

        result = mutableListOf()
    }

    @Test
    fun `Simple subscribe for one event`() = runTest {
        client.subscribe {
            result.add(it)
        }

        socket.sendEvent(eventA)

        result shouldBeEqualTo listOf(eventA)
    }

    @Test
    fun `Simple subscribe for multiple events`() = runTest {
        client.subscribe {
            result.add(it)
        }

        socket.sendEvent(eventA)
        socket.sendEvent(eventB)
        socket.sendEvent(eventC)

        result shouldBeEqualTo listOf(eventA, eventB, eventC)
    }

    @Test
    fun `Subscribe for string event types`() {
        client.subscribeFor("d", "f") {
            result.add(it)
        }

        socket.sendEvent(eventD)
        socket.sendEvent(eventE)
        socket.sendEvent(eventF)
        socket.sendEvent(eventE)
        socket.sendEvent(eventD)

        result shouldBeEqualTo listOf(eventD, eventF, eventD)
    }

    @Test
    fun `Subscribe for Java Class event types`() = runTest {
        client.subscribeFor(eventA::class.java, eventC::class.java) {
            result.add(it)
        }

        socket.sendEvent(eventA)
        socket.sendEvent(eventB)
        socket.sendEvent(eventC)

        result shouldBeEqualTo listOf(eventA, eventC)
    }

    @Test
    fun `Subscribe for KClass event types`() = runTest {
        client.subscribeFor(eventA::class, eventC::class) {
            result.add(it)
        }

        socket.sendEvent(eventA)
        socket.sendEvent(eventB)
        socket.sendEvent(eventC)

        result shouldBeEqualTo listOf(eventA, eventC)
    }

    @Test
    fun `Subscribe for event types with type parameter`() = runTest {
        client.subscribeFor<ConnectedEvent> {
            result.add(it)
        }

        socket.sendEvent(eventA)
        socket.sendEvent(eventB)
        socket.sendEvent(eventC)

        result shouldBeEqualTo listOf(eventA)
    }

    @Test
    fun `Subscribe for single event, with event type as type parameter`() = runTest {
        client.subscribeForSingle<ConnectedEvent> {
            result.add(it)
        }

        socket.sendEvent(eventB)
        socket.sendEvent(eventA)
        socket.sendEvent(eventA)

        result shouldBeEqualTo listOf(eventA)
    }

    @Test
    fun `Unsubscribe from events`() = runTest {
        val disposable = client.subscribe {
            result.add(it)
        }

        socket.sendEvent(eventA)

        disposable.dispose()

        socket.sendEvent(eventB)
        socket.sendEvent(eventC)

        result shouldBeEqualTo listOf(eventA)
    }

    @Test
    fun `Given connected user When handle event with updated user Should updated user value`() = runTest {
        val updateUser = user.copy(extraData = mutableMapOf()).apply { name = "updateUserName" }

        socket.sendEvent(Mother.randomUserPresenceChangedEvent(updateUser))

        client.getCurrentUser() shouldBeEqualTo updateUser
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `Sync with empty cids`() = runTest {
        /* Given */
        whenever(api.getSyncHistory(any(), any())) doReturn TestCall(
            Result.error(
                ChatNetworkError.create(
                    statusCode = 400,
                    streamCode = 4,
                    description = "channel_cids must contain at least 1 item"
                )
            )
        )

        /* When */
        val result = client.getSyncHistory(emptyList(), Date()).await()

        /* Then */
        result shouldBeEqualTo Result.error(ChatError("channelsIds must contain at least 1 id."))
    }

    @Test
    fun `Sync with nonempty cids`() = runTest {
        /* Given */
        val date = Date()
        val rawDate = streamDateFormatter.format(date)

        whenever(api.getSyncHistory(any(), any())) doReturn TestCall(
            Result.success(
                listOf(
                    HealthEvent(
                        type = "type",
                        createdAt = date,
                        rawCreatedAt = rawDate,
                        connectionId = "12345"
                    )
                )
            )
        )

        /* When */
        val result = client.getSyncHistory(listOf("test"), Date()).await()

        /* Then */
        result shouldBeEqualTo Result.success(
            listOf(
                HealthEvent(
                    type = "type",
                    createdAt = date,
                    rawCreatedAt = rawDate,
                    connectionId = "12345"
                )
            )
        )
    }
}
