package io.getstream.chat.android.client.chatclient

import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.clientstate.SocketState
import io.getstream.chat.android.client.clientstate.UserState
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.test.randomString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito

internal class WhenReconnectSocket : BaseChatClientTest() {

    @Test
    fun `Given idle connection state Should do nothing`() {
        val sut = Fixture().givenIdleConnectionState().clearSocketInvocations().get()

        sut.reconnectSocket()

        Mockito.verifyNoInteractions(socket)
    }

    @Test
    fun `Given pending connection state Should do nothing`() {
        val sut = Fixture().givenPendingConnectionState().clearSocketInvocations().get()

        sut.reconnectSocket()

        Mockito.verifyNoInteractions(socket)
    }

    @Test
    fun `Given connected connection state Should do nothing`() {
        val sut = Fixture().givenConnectedConnectionState().clearSocketInvocations().get()

        sut.reconnectSocket()

        Mockito.verifyNoInteractions(socket)
    }

    @Test
    fun `Given disconnected connection state And User set state Should connect to socket`() {
        val user = Mother.randomUser()
        val sut = Fixture().givenDisconnectedConnectionState().givenUserSetState(user).get()

        sut.reconnectSocket()

        verify(socket).connect(user)
    }

    @Test
    fun `Given disconnected connection state And Anonymous user set state Should connect to socket anonymously`() {
        val sut = Fixture().givenDisconnectedConnectionState().givenAnonymousUserSetState().get()

        sut.reconnectSocket()

        verify(socket).connectAnonymously()
    }

    @Test
    fun `Given disconnected connection state And anonymous pending state Should throw exception`() {
        val sut = Fixture().givenDisconnectedConnectionState().givenAnonymousPendingState().get()

        assertThrows<IllegalStateException> { sut.reconnectSocket() }
    }

    @Test
    fun `Given disconnected connection state And user not set state Should throw exception`() {
        val sut = Fixture().givenDisconnectedConnectionState().givenUserNotSetState().get()

        assertThrows<IllegalStateException> { sut.reconnectSocket() }
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

        fun clearSocketInvocations() = apply {
            clearInvocations(socket)
        }

        fun get() = chatClient
    }
}
