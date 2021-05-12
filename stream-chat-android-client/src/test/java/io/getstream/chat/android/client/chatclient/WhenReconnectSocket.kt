package io.getstream.chat.android.client.chatclient

import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.clientstate.ClientState
import io.getstream.chat.android.client.clientstate.UserState
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.test.randomString
import org.junit.jupiter.api.Test
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

    inner class Fixture {
        fun givenIdleConnectionState() = apply {
            whenever(clientStateService.state) doReturn ClientState.Idle
        }

        fun givenPendingConnectionState() = apply {
            whenever(clientStateService.state) doReturn ClientState.Pending
        }

        fun givenConnectedConnectionState() = apply {
            whenever(clientStateService.state) doReturn ClientState.Connected(randomString())
        }

        fun givenDisconnectedConnectionState() = apply {
            whenever(clientStateService.state) doReturn ClientState.Disconnected
        }

        fun givenUserSetState(user: User) = apply {
            whenever(userStateService.state) doReturn UserState.User.UserSet(user)
        }
        fun clearSocketInvocations() = apply {
            clearInvocations(socket)
        }

        fun get() = chatClient
    }
}
