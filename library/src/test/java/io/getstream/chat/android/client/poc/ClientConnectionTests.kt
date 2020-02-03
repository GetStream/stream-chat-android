package io.getstream.chat.android.client.poc

import io.getstream.chat.android.client.*
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.ConnectionData
import io.getstream.chat.android.client.poc.utils.Callback
import io.getstream.chat.android.client.poc.utils.ErrorCall
import io.getstream.chat.android.client.poc.utils.SuccessCall
import io.getstream.chat.android.client.poc.utils.SuccessTokenProvider
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class ClientConnectionTests {

    val user = User("test-id")
    val connection = ConnectionData("connection-id", user)
    val tokenProvider = SuccessTokenProvider()
    val error = ChatError("connection-error")

    lateinit var api: ChatApi
    lateinit var anonymousApi: ChatApi
    lateinit var socket: ChatSocket
    lateinit var client: ChatClient
    lateinit var call: Callback<Result<ConnectionData>>

    @Before
    fun before() {
        val config = ChatClientBuilder.ChatConfig()
        api = mock(ChatApi::class.java)
        anonymousApi = mock(ChatApi::class.java)
        socket = mock(ChatSocket::class.java)
        client = ChatClientImpl(api, socket, config)

        call = mock(Callback::class.java) as Callback<Result<ConnectionData>>
    }

    @Test
    fun successConnection() {

        val callback: (Result<ConnectionData>) -> Unit = { call.call(it) }

        client.setUser(user, tokenProvider)

        verify(call, times(1)).call(Result(connection, null))
    }

    @Test
    fun failedConnection() {
        val callback: (Result<ConnectionData>) -> Unit = { call.call(it) }

        client.setUser(user, tokenProvider)

        verify(call, times(1)).call(Result(null, error))
    }

    @Test
    fun connectAndDisconnect() {
        val callback: (Result<ConnectionData>) -> Unit = { call.call(it) }

        client.setUser(user, tokenProvider)

        verify(call, times(1)).call(Result(connection, null))

        client.disconnect()

        verify(socket, times(1)).disconnect()
    }


}
