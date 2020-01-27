package io.getstream.chat.android.core.poc.utils

import io.getstream.chat.android.core.poc.library.*
import io.getstream.chat.android.core.poc.library.rest.MessageResponse
import io.getstream.chat.android.core.poc.library.socket.ChatSocket
import io.getstream.chat.android.core.poc.library.socket.ConnectionData
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class MessagesApiCallsTests {

    val user = User("test-id")
    val apiKey = "api-key"
    val channelId = "test-id"
    val connection = ConnectionData("connection-id", user)
    val tokenProvider = SuccessTokenProvider()

    lateinit var client: ChatClient

    lateinit var api: ChatApi
    lateinit var socket: ChatSocket
    lateinit var retrofitApi: RetrofitApi

    @Before
    fun before() {
        api = Mockito.mock(ChatApi::class.java)
        socket = Mockito.mock(ChatSocket::class.java)
        retrofitApi = Mockito.mock(RetrofitApi::class.java)
        client = ChatClientImpl(ChatApiImpl(apiKey, retrofitApi), socket)

        Mockito.`when`(socket.connect(user, tokenProvider)).thenReturn(SuccessCall(connection))

        client.setUser(user, tokenProvider) {}
    }

    @Test
    fun test() {

        val messageId = "message-id"
        val message = Message().apply { text = "a-message" }

        Mockito.`when`(
            retrofitApi
                .getMessage(messageId, apiKey, user.id, connection.connectionId)
        ).thenReturn(RetroSuccess(MessageResponse(message)))

        val result = client.getMessage(messageId).execute()

        verifySuccess(result, message)
    }
}