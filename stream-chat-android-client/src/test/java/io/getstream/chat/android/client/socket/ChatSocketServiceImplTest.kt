package io.getstream.chat.android.client.socket

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.Mother.randomUser
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.randomString
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

internal class ChatSocketServiceImplTest {
    private lateinit var tokenManager: TokenManager
    private lateinit var socketFactory: SocketFactory
    private lateinit var eventsParser: EventsParser
    private lateinit var networkStateProvider: NetworkStateProvider
    private lateinit var socketListener: SocketListener
    private lateinit var socketService: ChatSocketServiceImpl

    @JvmField
    @RegisterExtension
    val testCoroutines = TestCoroutineExtension()
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
            networkStateProvider,
            testCoroutines.scope
        )
        socketService.addListener(socketListener)
    }

    @Test
    fun `Should start connecting to socket when connecting and network connectivity exists`() {
        whenever(networkStateProvider.isConnected()) doReturn true

        socketService.userConnect(randomString(), randomString(), randomUser())

        socketService.state shouldBeEqualTo ChatSocketServiceImpl.State.Connecting
    }

    @Test
    fun `Should start connecting to socket when connecting was called and connection was recovered`() {
        whenever(networkStateProvider.isConnected()) doReturn false
        whenever(networkStateProvider.subscribe(any())) doAnswer {
            it.getArgument<NetworkStateProvider.NetworkStateListener>(0).onConnected()
        }

        socketService.userConnect(randomString(), randomString(), randomUser())

        socketService.state shouldBeEqualTo ChatSocketServiceImpl.State.Connecting
    }

    @Test
    fun `Should not start connecting to socket when connecting and there is no network connectivity`() {
        whenever(networkStateProvider.isConnected()) doReturn false

        socketService.userConnect(randomString(), randomString(), randomUser())

        socketService.state shouldBeEqualTo ChatSocketServiceImpl.State.NetworkDisconnected
    }

    @Test
    fun `Should start connecting to socket when connecting with anymous user and network connectivity exists`() {
        whenever(networkStateProvider.isConnected()) doReturn true

        socketService.anonymousConnect(randomString(), randomString())

        socketService.state shouldBeEqualTo ChatSocketServiceImpl.State.Connecting
    }

    @Test
    fun `Should start connecting to socket when connecting with anymous user  was called and connection was recovered`() {
        whenever(networkStateProvider.isConnected()) doReturn false
        whenever(networkStateProvider.subscribe(any())) doAnswer {
            it.getArgument<NetworkStateProvider.NetworkStateListener>(0).onConnected()
        }

        socketService.anonymousConnect(randomString(), randomString())

        socketService.state shouldBeEqualTo ChatSocketServiceImpl.State.Connecting
    }

    @Test
    fun `Should not start connecting to socket when connecting with anymous user  and there is no network connectivity`() {
        whenever(networkStateProvider.isConnected()) doReturn false

        socketService.anonymousConnect(randomString(), randomString())

        socketService.state shouldBeEqualTo ChatSocketServiceImpl.State.NetworkDisconnected
    }
}
