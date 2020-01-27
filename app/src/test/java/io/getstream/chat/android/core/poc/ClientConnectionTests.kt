package io.getstream.chat.android.core.poc

import io.getstream.chat.android.core.poc.library.*
import io.getstream.chat.android.core.poc.library.errors.ChatError
import io.getstream.chat.android.core.poc.library.socket.ChatSocket
import io.getstream.chat.android.core.poc.library.socket.ConnectionData
import io.getstream.chat.android.core.poc.utils.Callback
import io.getstream.chat.android.core.poc.utils.ErrorCall
import io.getstream.chat.android.core.poc.utils.SuccessCall
import io.getstream.chat.android.core.poc.utils.SuccessTokenProvider
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class ClientConnectionTests {

    val user = User("test-id")
    val connection = ConnectionData("connection-id", user)
    val tokenProvider = SuccessTokenProvider()
    val error = ChatError("connection-error")

    lateinit var api: ChatApi
    lateinit var socket: ChatSocket
    lateinit var client: ChatClient
    lateinit var call: Callback<Result<ConnectionData>>

    @Before
    fun before() {
        api = mock(ChatApi::class.java)
        socket = mock(ChatSocket::class.java)
        client = ChatClientImpl(api, socket)

        call = mock(Callback::class.java) as Callback<Result<ConnectionData>>
    }

    @Test
    fun successConnection() {

        `when`(socket.connect(user, tokenProvider)).thenReturn(SuccessCall(connection))
        val callback: (Result<ConnectionData>) -> Unit = { call.call(it) }

        client.setUser(user, tokenProvider, callback)

        verify(call, times(1)).call(Result(connection, null))
    }

    @Test
    fun failedConnection() {
        `when`(socket.connect(user, tokenProvider)).thenReturn(ErrorCall(error))
        val callback: (Result<ConnectionData>) -> Unit = { call.call(it) }

        client.setUser(user, tokenProvider, callback)

        verify(call, times(1)).call(Result(null, error))
    }


}
