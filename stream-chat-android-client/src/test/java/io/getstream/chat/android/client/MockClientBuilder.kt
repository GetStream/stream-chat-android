package io.getstream.chat.android.client

import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api.GsonChatApi
import io.getstream.chat.android.client.api.RetrofitAnonymousApi
import io.getstream.chat.android.client.api.RetrofitApi
import io.getstream.chat.android.client.clientstate.ClientStateService
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.helpers.QueryChannelsPostponeHelper
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.utils.UuidGeneratorImpl
import io.getstream.chat.android.client.utils.observable.FakeChatSocket
import kotlinx.coroutines.test.TestCoroutineScope
import java.util.Date

/**
 * Used for integrations tests.
 * Initialises mock internals of [ChatClient]
 */
internal class MockClientBuilder(
    private val testCoroutineScope: TestCoroutineScope,
) {
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
    private lateinit var fileUploader: FileUploader

    internal lateinit var retrofitApi: RetrofitApi
    internal lateinit var retrofitAnonymousApi: RetrofitAnonymousApi
    private lateinit var api: GsonChatApi
    private lateinit var notificationsManager: ChatNotifications
    private lateinit var client: ChatClient

    fun build(): ChatClient {
        val config = ChatClientConfig(
            apiKey,
            "hello.http",
            "cdn.http",
            "socket.url",
            1000,
            1000,
            false,
            ChatLogger.Config(ChatLogLevel.NOTHING, null)
        )

        socket = FakeChatSocket()
        retrofitApi = mock()
        retrofitAnonymousApi = mock()
        fileUploader = mock()
        notificationsManager = mock()
        api = GsonChatApi(
            retrofitApi,
            retrofitAnonymousApi,
            UuidGeneratorImpl(),
            fileUploader,
            testCoroutineScope
        )

        val clientStateService = ClientStateService()
        val userStateService = UserStateService()
        val queryChannelsPostponeHelper = QueryChannelsPostponeHelper(api, userStateService, testCoroutineScope)
        client = ChatClient(
            config,
            api,
            socket,
            notificationsManager,
            tokenManager = FakeTokenManager(token),
            clientStateService = clientStateService,
            queryChannelsPostponeHelper = queryChannelsPostponeHelper,
            userStateService = userStateService,
        )

        client.connectUser(user, token).enqueue()

        socket.sendEvent(connectedEvent)

        return client
    }
}
