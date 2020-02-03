package io.getstream.chat.android.client.poc

import io.getstream.chat.android.client.*
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.gson.JsonParserImpl
import io.getstream.chat.android.client.observable.JustObservable
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.utils.ImmediateTokenProvider
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class ClientConnectionTests {

    val userId = "test-id"
    val connectionId = "connection-id"
    val user = User(userId)
    val token = "token"
    val config = ChatClientBuilder.ChatConfig()

    val connectedEvent = ConnectedEvent().apply {
        me = this@ClientConnectionTests.user
        connectionId = this@ClientConnectionTests.connectionId
    }

    lateinit var api: ChatApi
    lateinit var socket: ChatSocket
    lateinit var retrofitApi: RetrofitApi
    lateinit var client: ChatClient


    @Before
    fun before() {
        socket = mock(ChatSocket::class.java)
        retrofitApi = mock(RetrofitApi::class.java)
        api = ChatApiImpl("api-key", retrofitApi, JsonParserImpl(), null)

    }

    @Test
    fun successConnection() {

        `when`(socket.events()).thenReturn(JustObservable(connectedEvent))

        client = ChatClientImpl(api, socket, config)
        client.setUser(user, token)

        verify(socket, times(1)).connect(user, ImmediateTokenProvider(token))
    }

    @Test
    fun connectAndDisconnect() {
        `when`(socket.events()).thenReturn(JustObservable(connectedEvent))

        client = ChatClientImpl(api, socket, config)
        client.setUser(user, token)

        client.disconnect()

        verify(socket, times(1)).disconnect()
    }


}
