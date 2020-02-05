package io.getstream.chat.android.client.poc

import io.getstream.chat.android.client.*
import io.getstream.chat.android.client.api.ChatConfig
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.gson.JsonParserImpl
import io.getstream.chat.android.client.logger.StreamLogger
import io.getstream.chat.android.client.observable.JustObservable
import io.getstream.chat.android.client.rest.RetrofitApi
import io.getstream.chat.android.client.socket.ChatSocket
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class ClientConnectionTests {

    val userId = "test-id"
    val connectionId = "connection-id"
    val user = User(userId)
    val token = "token"
    val config = ChatConfig.Builder().token(token).build()

    val connectedEvent = ConnectedEvent().apply {
        me = this@ClientConnectionTests.user
        connectionId = this@ClientConnectionTests.connectionId
    }

    lateinit var api: ChatApi
    lateinit var socket: ChatSocket
    lateinit var retrofitApi: RetrofitApi
    lateinit var client: ChatClient
    lateinit var logger: StreamLogger


    @Before
    fun before() {
        socket = mock(ChatSocket::class.java)
        retrofitApi = mock(RetrofitApi::class.java)
        logger = mock(StreamLogger::class.java)
        api = ChatApiImpl(config, retrofitApi, JsonParserImpl(), null)
    }

    @Test
    fun successConnection() {

        `when`(socket.events()).thenReturn(JustObservable(connectedEvent))

        client = ChatClientImpl(api, socket, config, logger)
        client.setUser(user, token)

        verify(socket, times(1)).connect(user)
    }

    @Test
    fun connectAndDisconnect() {
        `when`(socket.events()).thenReturn(JustObservable(connectedEvent))

        client = ChatClientImpl(api, socket, config, logger)
        client.setUser(user, token)

        client.disconnect()

        verify(socket, times(1)).disconnect()
    }


}
