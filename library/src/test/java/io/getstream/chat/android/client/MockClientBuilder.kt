package io.getstream.chat.android.client

import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.gson.JsonParserImpl
import io.getstream.chat.android.client.observable.JustObservable
import io.getstream.chat.android.client.poc.utils.SuccessTokenProvider
import io.getstream.chat.android.client.socket.ChatSocket
import org.mockito.Mockito

/**
 * Used for integrations tests.
 * Initialises mock internals of [ChatClientImpl]
 */
class MockClientBuilder {

    val userId = "test-id"
    val connectionId = "connection-id"
    val apiKey = "api-key"
    val channelType = "channel-type"
    val channelId = "channel-id"
    val serverErrorCode = 500
    val user = User(userId)
    val connectedEvent = ConnectedEvent().apply {
        me = this@MockClientBuilder.user
        connectionId = this@MockClientBuilder.connectionId
    }
    val tokenProvider = SuccessTokenProvider()

    lateinit var api: ChatApi
    lateinit var socket: ChatSocket
    lateinit var retrofitApi: RetrofitApi

    private lateinit var client: ChatClient

    fun build(): ChatClient {

        val config = ChatClientBuilder.ChatConfig()
        socket = Mockito.mock(ChatSocket::class.java)
        retrofitApi = Mockito.mock(RetrofitApi::class.java)
        api = ChatApiImpl(apiKey, retrofitApi, JsonParserImpl(), null)

        Mockito.`when`(socket.events()).thenReturn(JustObservable(connectedEvent))

        client = ChatClientImpl(api, socket, config)

        client.setUser(user, tokenProvider)

        return client
    }
}