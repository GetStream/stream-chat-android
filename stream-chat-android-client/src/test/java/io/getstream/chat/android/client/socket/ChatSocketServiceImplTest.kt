package io.getstream.chat.android.client.socket

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.token.TokenManager
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ChatSocketServiceImplTest {
    private lateinit var tokenManager: TokenManager
    private lateinit var socketFactory: SocketFactory
    private lateinit var eventsParser: EventsParser
    private lateinit var networkStateProvider: NetworkStateProvider
    private lateinit var socketListener: SocketListener
    private lateinit var socketService: ChatSocketServiceImpl

    @BeforeEach
    fun setup() {
        tokenManager = mock()
        socketFactory = mock()
        eventsParser = mock()
        networkStateProvider = mock()
        socketListener = mock()
        socketService = ChatSocketServiceImpl.create(
            tokenManager,
            socketFactory,
            eventsParser,
            networkStateProvider
        )
        socketService.addListener(socketListener)
    }

    @Test
    fun `Should start connecting to socket when connecting and network connectivity exists`() {
        When calling networkStateProvider.isConnected() doReturn true

        socketService.connect("", "", null)

        socketService.state shouldBeEqualTo ChatSocketServiceImpl.State.Connecting
    }

    @Test
    fun `Should start connecting to socket when connecting was called and connection was recovered`() {
        When calling networkStateProvider.isConnected() doReturn false
        When calling networkStateProvider.subscribe(any()) doAnswer {
            it.getArgument<NetworkStateProvider.NetworkStateListener>(0).onConnected()
        }

        socketService.connect("", "", null)

        socketService.state shouldBeEqualTo ChatSocketServiceImpl.State.Connecting
    }

    @Test
    fun `Should not start connecting to socket when connecting and there is no network connectivity`() {
        When calling networkStateProvider.isConnected() doReturn false

        socketService.connect("", "", null)

        socketService.state shouldBeEqualTo ChatSocketServiceImpl.State.NetworkDisconnected
    }
}
