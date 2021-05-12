package io.getstream.chat.android.client.chatclient

import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.clientstate.ClientState
import org.junit.jupiter.api.Test
import org.mockito.Mockito

internal class WhenReconnectSocket : BaseChatClientTest() {

    @Test
    fun `Given idle connection state Should do nothing`() {
        val sut = Fixture().givenIdleConnectionState().get()
        clearInvocations(socket)

        sut.reconnectSocket()

        Mockito.verifyNoInteractions(socket)
    }

    inner class Fixture {
        fun givenIdleConnectionState() = apply {
            whenever(clientStateService.state) doReturn ClientState.Idle
        }

        fun get() = chatClient
    }
}
