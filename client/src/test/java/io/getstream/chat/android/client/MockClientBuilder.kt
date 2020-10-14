package io.getstream.chat.android.client

import android.content.Context
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatApiImpl
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api.RetrofitApi
import io.getstream.chat.android.client.api.RetrofitCdnApi
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.parser.ChatParserImpl
import io.getstream.chat.android.client.utils.UuidGeneratorImpl
import io.getstream.chat.android.client.utils.observable.FakeChatSocket
import java.util.Date

/**
 * Used for integrations tests.
 * Initialises mock internals of [ChatClientImpl]
 */
internal class MockClientBuilder {

    val userId = "test-id"
    val connectionId = "connection-id"
    val apiKey = "api-key"
    val channelType = "channel-type"
    val channelId = "channel-id"
    val token = "token"
    val serverErrorCode = 500
    val user = User().apply { id = userId }
    val connectedEvent = ConnectedEvent(
        EventType.HEALTH_CHECK,
        Date(),
        user,
        connectionId
    )

    private lateinit var socket: FakeChatSocket
    lateinit var retrofitCdnApi: RetrofitCdnApi

    internal lateinit var retrofitApi: RetrofitApi
    private lateinit var api: ChatApi
    private lateinit var notificationsManager: ChatNotifications
    private lateinit var client: ChatClient

    fun build(): ChatClient {

        val context = mock<Context>()

        val config = ChatClientConfig(
            apiKey,
            "hello.http",
            "cdn.http",
            "socket.url",
            1000,
            1000,
            false,
            ChatLogger.Config(ChatLogLevel.NOTHING, null),
            ChatNotificationHandler(context)
        )

        socket = FakeChatSocket()
        retrofitApi = mock()
        retrofitCdnApi = mock()
        notificationsManager = mock()
        api = ChatApiImpl(
            config.apiKey,
            retrofitApi,
            retrofitCdnApi,
            ChatParserImpl(),
            UuidGeneratorImpl()
        )

        client = ChatClientImpl(config, api, socket, notificationsManager)
        client.setUser(user, token)

        socket.sendEvent(connectedEvent)

        return client
    }
}
