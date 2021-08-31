package io.getstream.chat.android.client.chatclient

import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.clientstate.SocketStateService
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.utils.TokenUtils
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

    @Mock
    protected lateinit var tokenManager: TokenManager

    @Mock
    protected lateinit var config: ChatClientConfig

    @Mock
    protected lateinit var api: ChatApi

    protected lateinit var chatClient: ChatClient
    internal val tokenUtils: TokenUtils = mock()

    @BeforeEach
    fun before() {
        MockitoAnnotations.openMocks(this)
        chatClient = ChatClient(
            config = config,
            api = api,
            socket = socket,
            notifications = mock(),
            tokenManager = tokenManager,
            socketStateService = socketStateService,
            queryChannelsPostponeHelper = mock(),
            userStateService = userStateService,
            encryptedUserConfigStorage = mock(),
            tokenUtils = tokenUtils,
            appContext = mock(),
            scope = mock(),
        )
    }
}
