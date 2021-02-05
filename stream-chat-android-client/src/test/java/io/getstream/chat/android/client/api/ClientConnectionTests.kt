package io.getstream.chat.android.client.api

import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.ConnectionData
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.SocketListener
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.utils.UuidGeneratorImpl
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import java.util.Date

internal class ClientConnectionTests {

    private val userId = "test-id"
    private val connectionId = "connection-id"
    private val user = User().apply { id = userId }
    private val token = "token"

    private val config = ChatClientConfig(
        "api-key",
        "hello.http",
        "cdn.http",
        "socket.url",
        1000,
        1000,
        false,
        ChatLogger.Config(ChatLogLevel.NOTHING, null),

    )

    private val connectedEvent = ConnectedEvent(
        EventType.HEALTH_CHECK,
        Date(),
        user,
        connectionId
    )
    private val disconnectedEvent = DisconnectedEvent(EventType.CONNECTION_DISCONNECTED, Date())

    private lateinit var api: GsonChatApi
    private lateinit var socket: ChatSocket
    private lateinit var retrofitApi: RetrofitApi
    private lateinit var retrofitAnonymousApi: RetrofitAnonymousApi
    private lateinit var fileUploader: FileUploader
    private lateinit var client: ChatClient
    private lateinit var logger: ChatLogger
    private lateinit var notificationsManager: ChatNotifications
    private lateinit var initCallback: Call.Callback<ConnectionData>
    private lateinit var socketListener: SocketListener

    @Before
    fun before() {
        socket = mock()
        retrofitApi = mock()
        retrofitAnonymousApi = mock()
        fileUploader = mock()
        logger = mock()
        notificationsManager = mock()
        initCallback = mock()
        api = GsonChatApi(
            config.apiKey,
            retrofitApi,
            retrofitAnonymousApi,
            UuidGeneratorImpl(),
            fileUploader,
        )

        whenever(socket.addListener(anyOrNull())) doAnswer { invocationOnMock ->
            socketListener = invocationOnMock.getArgument(0)
            socketListener.onEvent(disconnectedEvent)
        }

        client = ChatClient(
            config,
            api,
            socket,
            notificationsManager,
            tokenManager = FakeTokenManager(token)
        )
    }

    @Test
    fun successConnection() {
        client.connectUser(user, token).enqueue()

        verify(socket, times(1)).connect(user)
    }

    @Test
    fun `Should not connect and report error when user is already set`() {
        client.connectUser(user, token).enqueue()
        socketListener.onEvent(connectedEvent)
        reset(socket)

        client.connectUser(user, token).enqueue(initCallback)

        verify(socket, never()).connect(user)

        val error = ChatError("User cannot be set until previous one is disconnected.")
        verify(initCallback).onResult(argThat { this.error().message == error.message })
    }

    @Test
    fun connectAndDisconnect() {
        client.connectUser(user, token).enqueue()
        socketListener.onEvent(connectedEvent)

        client.disconnect()

        verify(socket, times(1)).disconnect()
    }
}
