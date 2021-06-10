package io.getstream.chat.android.client.chatclient

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.clientstate.SocketState
import io.getstream.chat.android.client.clientstate.UserState
import io.getstream.chat.android.client.models.ConnectionData
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.android.test.randomString
import org.junit.jupiter.api.Test

internal class WhenConnectUser : BaseChatClientTest() {

    @Test
    fun `Given user set and socket in idle state and user with the same id Should connect socket`() {
        val user = Mother.randomUser { id = "userId" }
        val sut = Fixture()
            .givenUserAndToken(user, "token")
            .givenIdleConnectionState()
            .givenUserSetState(Mother.randomUser { id = "userId" })
            .get()

        sut.connectUser(user, "token").enqueue()

        verify(socket).connect(user)
    }

    @Test
    fun `Given user set and socket in idle state and user with the same id Should update user`() {
        val user = Mother.randomUser { id = "userId" }
        val sut = Fixture()
            .givenUserAndToken(Mother.randomUser { id = "userId" }, "token")
            .givenIdleConnectionState()
            .givenUserSetState(Mother.randomUser { id = "userId" })
            .get()

        sut.connectUser(user, "token").enqueue()

        verify(userStateService).onUserUpdated(user)
    }

    @Test
    fun `Given user set and socket in idle state and user with different id Should Not connect to the socket, update the user and token provider`() {
        val sut = Fixture()
            .givenUserAndToken(Mother.randomUser { id = "differentUserId" }, "token")
            .givenIdleConnectionState()
            .givenUserSetState(Mother.randomUser { id = "userId" })
            .get()

        sut.connectUser(Mother.randomUser { id = "differentUserId" }, "token").enqueue()

        verify(socket, never()).connect(any())
        verify(userStateService, never()).onUserUpdated(any())
        verify(tokenManager, never()).setTokenProvider(any())
    }

    @Test
    fun `Given user set and socket in idle state and user with the same id Should update token provider`() {
        val tokenProviderMock = mock<TokenProvider>()
        val token = randomString()
        whenever(tokenProviderMock.loadToken()) doReturn token
        val sut = Fixture()
            .givenIdleConnectionState()
            .givenUserAndToken(Mother.randomUser { id = "userId" }, token)
            .givenUserSetState(Mother.randomUser { id = "userId" })
            .get()

        sut.connectUser(Mother.randomUser { id = "userId" }, tokenProviderMock).enqueue()

        verify(tokenManager).setTokenProvider(tokenProviderMock)
    }

    @Test
    fun `Given user set and socket in idle state and user with the same id Should invoke pre set listeners`() {
        val listener: (User) -> Unit = mock()
        val sut = Fixture()
            .givenUserAndToken(Mother.randomUser { id = "userId" }, "token")
            .givenIdleConnectionState()
            .givenUserSetState(Mother.randomUser { id = "userId" })
            .givenPreSetUserListener(listener)
            .get()

        sut.connectUser(Mother.randomUser { id = "userId" }, "token").enqueue()

        verify(listener).invoke(argThat { id == "userId" })
    }

    @Test
    fun `Given user not set Should set the user`() {
        val user = Mother.randomUser { id = "userId" }
        val sut = Fixture()
            .givenUserAndToken(user, "token")
            .givenUserNotSetState()
            .get()

        sut.connectUser(user, "token").enqueue()

        verify(userStateService).onSetUser(user)
    }

    @Test
    fun `Given user not set Should update token provider`() {
        val tokenProviderMock = mock<TokenProvider>()
        whenever(tokenProviderMock.loadToken()) doReturn "token"
        val sut = Fixture()
            .givenUserAndToken(Mother.randomUser { id = "userId" }, "token")
            .givenUserNotSetState()
            .get()

        sut.connectUser(Mother.randomUser { id = "userId" }, tokenProviderMock).enqueue()

        verify(tokenManager).setTokenProvider(tokenProviderMock)
    }

    @Test
    fun `Given user not set Should connect to the socket`() {
        val user = Mother.randomUser { id = "userId" }
        val sut = Fixture()
            .givenUserAndToken(user, "token")
            .givenUserNotSetState()
            .get()

        sut.connectUser(user, "token").enqueue()

        verify(socket).connect(user)
    }

    @Test
    fun `Given user not set and config with warmup Should do warmup`() {
        val sut = Fixture()
            .givenUserAndToken(Mother.randomUser { id = "userId" }, "token")
            .givenUserNotSetState()
            .givenWarmUpEnabled()
            .get()

        sut.connectUser(Mother.randomUser { id = "userId" }, "token").enqueue()

        verify(api).warmUp()
    }

    @Test
    fun `Given user not set Should invoke pre set listeners`() {
        val listener: (User) -> Unit = mock()
        val sut = Fixture()
            .givenUserAndToken(Mother.randomUser { id = "userId" }, "token")
            .givenUserNotSetState()
            .givenPreSetUserListener(listener)
            .get()

        sut.connectUser(Mother.randomUser { id = "userId" }, "token").enqueue()

        verify(listener).invoke(argThat { id == "userId" })
    }

    @Test
    fun `Given user set and user with different id Should call init connection listener with error`() {
        val connectionDataCallback: Call.Callback<ConnectionData> = mock()
        val sut = Fixture()
            .givenUserAndToken(Mother.randomUser { id = "userId" }, "token")
            .givenUserSetState(Mother.randomUser { id = "userId1" })
            .get()

        sut.connectUser(Mother.randomUser { id = "userId2" }, "token").enqueue(connectionDataCallback)

        verify(connectionDataCallback).onResult(argThat { isError })
    }

    inner class Fixture {
        fun givenIdleConnectionState() = apply {
            whenever(socketStateService.state) doReturn SocketState.Idle
        }

        fun givenPendingConnectionState() = apply {
            whenever(socketStateService.state) doReturn SocketState.Pending
        }

        fun givenConnectedConnectionState() = apply {
            whenever(socketStateService.state) doReturn SocketState.Connected(randomString())
        }

        fun givenDisconnectedConnectionState() = apply {
            whenever(socketStateService.state) doReturn SocketState.Disconnected
        }

        fun givenUserSetState(user: User) = apply {
            whenever(userStateService.state) doReturn UserState.UserSet(user)
        }

        fun givenAnonymousUserSetState() = apply {
            whenever(userStateService.state) doReturn UserState.Anonymous.AnonymousUserSet(Mother.randomUser())
        }

        fun givenAnonymousPendingState() = apply {
            whenever(userStateService.state) doReturn UserState.Anonymous.Pending
        }

        fun givenUserNotSetState() = apply {
            whenever(userStateService.state) doReturn UserState.NotSet
        }

        fun givenWarmUpEnabled() = apply {
            whenever(config.warmUp) doReturn true
        }

        fun givenPreSetUserListener(listener: (User) -> Unit) = apply {
            chatClient.preSetUserListeners.add(listener)
        }

        fun givenUserAndToken(user: User, token: String) = apply {
            whenever(tokenUtils.getUserId(token)) doReturn user.id
        }

        fun clearSocketInvocations() = apply {
            clearInvocations(socket)
        }

        fun get() = chatClient
    }
}
