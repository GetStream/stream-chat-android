package io.getstream.chat.android.client.chatclient

import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.clientstate.SocketStateService
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.socket.ChatSocket
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mock
import org.mockito.MockitoAnnotations

internal open class BaseChatClientTest {
    @Mock
    protected lateinit var socketStateService: SocketStateService
    @Mock
    protected lateinit var userStateService: UserStateService
    @Mock
    protected lateinit var socket: ChatSocket

    protected lateinit var chatClient: ChatClient

    @BeforeEach
    fun before() {
        MockitoAnnotations.openMocks(this)
        chatClient = ChatClient(
            config = mock(),
            api = mock(),
            socket = socket,
            notifications = mock(),
            tokenManager = mock(),
            socketStateService = socketStateService,
            queryChannelsPostponeHelper = mock(),
            userStateService = userStateService,
            encryptedPushNotificationsConfigStore = mock(),
        )
    }
}
