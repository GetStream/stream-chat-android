package io.getstream.chat.android.client

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
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
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.randomString
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.Date

internal class ChatClientTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        val eventA = ConnectedEvent(EventType.HEALTH_CHECK, Date(), User(), "")
        val eventB = NewMessageEvent(EventType.MESSAGE_NEW, Date(), User(), "type:id", "type", "id", Message(), 0, 0, 0)
        val eventC = DisconnectedEvent(EventType.CONNECTION_DISCONNECTED, Date())

        val eventD = UnknownEvent("d", Date(), emptyMap<Any, Any>())
        val eventE = UnknownEvent("e", Date(), mapOf<Any, Any>("cid" to "myCid"))
        val eventF = UnknownEvent("f", Date(), emptyMap<Any, Any>())
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
            1000,
            1000,
            false,
            ChatLogger.Config(ChatLogLevel.NOTHING, null),
        )
        whenever(tokenUtils.getUserId(token)) doReturn userId
        socket = FakeChatSocket()
        val socketStateService = SocketStateService()
        val userStateService = UserStateService()
        val queryChannelsPostponeHelper = QueryChannelsPostponeHelper(mock(), socketStateService, testCoroutines.scope)
        client = ChatClient(
            config = config,
            api = mock(),
            socket = socket,
            notifications = mock(),
            tokenManager = FakeTokenManager(""),
            socketStateService = socketStateService,
            queryChannelsPostponeHelper = queryChannelsPostponeHelper,
            userStateService = userStateService,
            encryptedPushNotificationsConfigStore = mock(),
            tokenUtils = tokenUtils,
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
}
