package io.getstream.chat.android.client

import android.content.Context
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatApiImpl
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api.models.RetrofitApi
import io.getstream.chat.android.client.api.models.RetrofitCdnApi
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig
import io.getstream.chat.android.client.parser.ChatParserImpl
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.utils.observable.JustObservable
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class ClientConnectionTests {

    val userId = "test-id"
    val connectionId = "connection-id"
    val user = User().apply { id = userId }
    val token = "token"
    val context = mock(Context::class.java)

    val config = ChatClientConfig(
        "api-key",
        "hello.http",
        "cdn.http",
        "socket.url",
        1000,
        1000,
        ChatLogLevel.NOTHING,
        ChatNotificationConfig(context)
    )

    val connectedEvent = ConnectedEvent().apply {
        me = this@ClientConnectionTests.user
        connectionId = this@ClientConnectionTests.connectionId
    }

    lateinit var api: ChatApi
    lateinit var socket: ChatSocket
    lateinit var retrofitApi: RetrofitApi
    lateinit var retrofitCdnApi: RetrofitCdnApi
    lateinit var client: ChatClient
    lateinit var logger: ChatLogger
    lateinit var notificationsManager: ChatNotifications

    @Before
    fun before() {
        socket = mock(ChatSocket::class.java)
        retrofitApi = mock(RetrofitApi::class.java)
        retrofitCdnApi = mock(RetrofitCdnApi::class.java)
        logger = mock(ChatLogger::class.java)
        notificationsManager = mock(ChatNotifications::class.java)
        api = ChatApiImpl(
            retrofitApi,
            retrofitCdnApi,
            config,
            ChatParserImpl()
        )
    }

    @Test
    fun successConnection() {

        `when`(socket.events()).thenReturn(JustObservable(connectedEvent))

        client = ChatClientImpl(config, api, socket, notificationsManager)
        client.setUser(user, token)

        verify(socket, times(1)).connect(user)
    }

    @Test
    fun connectAndDisconnect() {
        `when`(socket.events()).thenReturn(JustObservable(connectedEvent))

        client = ChatClientImpl(config, api, socket, notificationsManager)
        client.setUser(user, token)

        client.disconnect()

        verify(socket, times(1)).disconnect()
    }


}
