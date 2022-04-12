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

import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.clientstate.SocketStateService
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.client.helpers.QueryChannelsPostponeHelper
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.client.utils.TokenUtils
import io.getstream.chat.android.client.utils.observable.FakeChatSocket
import io.getstream.chat.android.client.utils.retry.NoRetryPolicy
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.randomString
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

internal class ChatClientTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        val eventA = ConnectedEvent(EventType.HEALTH_CHECK, Date(), User(), "")
        val eventB = NewMessageEvent(EventType.MESSAGE_NEW, Date(), User(), "type:id", "type", "id", Message(), 0, 0, 0)
        val eventC = DisconnectedEvent(EventType.CONNECTION_DISCONNECTED, Date())

        val eventD = UnknownEvent("d", Date(), null, emptyMap<Any, Any>())
        val eventE = UnknownEvent("e", Date(), null, mapOf<Any, Any>("cid" to "myCid"))
        val eventF = UnknownEvent("f", Date(), null, emptyMap<Any, Any>())
    }

    lateinit var socket: FakeChatSocket
    lateinit var client: ChatClient
    lateinit var result: MutableList<ChatEvent>
    val token = randomString()
    val userId = randomString()
    val user = Mother.randomUser { id = userId }
    val tokenUtils: TokenUtils = mock()

    @BeforeEach
    fun setUp() {
        val config = ChatClientConfig(
            "api-key",
            "hello.http",
            "cdn.http",
            "socket.url",
            false,
            ChatLogger.Config(ChatLogLevel.NOTHING, null),
        )
        whenever(tokenUtils.getUserId(token)) doReturn userId
        socket = FakeChatSocket()
        val socketStateService = SocketStateService()
        val userStateService = UserStateService()
        val queryChannelsPostponeHelper = QueryChannelsPostponeHelper(socketStateService, testCoroutines.scope)
        client = ChatClient(
            config = config,
            api = mock(),
            socket = socket,
            notifications = mock(),
            tokenManager = FakeTokenManager(""),
            socketStateService = socketStateService,
            queryChannelsPostponeHelper = queryChannelsPostponeHelper,
            userCredentialStorage = mock(),
            userStateService = userStateService,
            tokenUtils = tokenUtils,
            scope = testCoroutines.scope,
            retryPolicy = NoRetryPolicy(),
            appSettingsManager = mock(),
        ).apply {
            connectUser(user, token).enqueue()
        }
        result = mutableListOf()
    }

    @Test
    fun `Simple subscribe for one event`() {
        client.subscribe {
            result.add(it)
        }

        socket.sendEvent(eventA)

        result shouldBeEqualTo listOf(eventA)
    }

    @Test
    fun `Simple subscribe for multiple events`() {
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
    fun `Subscribe for Java Class event types`() {
        client.subscribeFor(eventA::class.java, eventC::class.java) {
            result.add(it)
        }

        socket.sendEvent(eventA)
        socket.sendEvent(eventB)
        socket.sendEvent(eventC)

        result shouldBeEqualTo listOf(eventA, eventC)
    }

    @Test
    fun `Subscribe for KClass event types`() {
        client.subscribeFor(eventA::class, eventC::class) {
            result.add(it)
        }

        socket.sendEvent(eventA)
        socket.sendEvent(eventB)
        socket.sendEvent(eventC)

        result shouldBeEqualTo listOf(eventA, eventC)
    }

    @Test
    fun `Subscribe for event types with type parameter`() {
        client.subscribeFor<ConnectedEvent> {
            result.add(it)
        }

        socket.sendEvent(eventA)
        socket.sendEvent(eventB)
        socket.sendEvent(eventC)

        result shouldBeEqualTo listOf(eventA)
    }

    @Test
    fun `Subscribe for single event, with event type as type parameter`() {
        client.subscribeForSingle<ConnectedEvent> {
            result.add(it)
        }

        socket.sendEvent(eventB)
        socket.sendEvent(eventA)
        socket.sendEvent(eventA)

        result shouldBeEqualTo listOf(eventA)
    }

    @Test
    fun `Unsubscribe from events`() {
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
    fun `Given connected user When handle event with updated user Should updated user value`() {
        val updateUser = user.copy(extraData = mutableMapOf()).apply { name = "updateUserName" }

        socket.sendEvent(Mother.randomUserPresenceChangedEvent(updateUser))

        client.getCurrentUser() shouldBeEqualTo updateUser
    }
}
