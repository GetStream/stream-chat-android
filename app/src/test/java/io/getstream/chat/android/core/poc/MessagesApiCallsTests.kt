package io.getstream.chat.android.core.poc

import io.getstream.chat.android.core.poc.library.*
import io.getstream.chat.android.core.poc.library.rest.MessageRequest
import io.getstream.chat.android.core.poc.library.rest.MessageResponse
import io.getstream.chat.android.core.poc.library.rest.SearchMessagesRequest
import io.getstream.chat.android.core.poc.library.rest.SearchMessagesResponse
import io.getstream.chat.android.core.poc.library.socket.ChatSocket
import io.getstream.chat.android.core.poc.library.socket.ConnectionData
import io.getstream.chat.android.core.poc.utils.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class MessagesApiCallsTests {

    val user = User("test-id")
    val apiKey = "api-key"
    val connection = ConnectionData("connection-id", user)
    val tokenProvider = SuccessTokenProvider()
    val channelType = "test-type"
    val channelId = "test-id"
    val serverErrorCode = 500

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

        Mockito.`when`(socket.connect(user, tokenProvider)).thenReturn(
            SuccessCall(
                connection
            )
        )

        client.setUser(user, tokenProvider) {}
    }

    @Test
    fun getMessageSuccess() {

        val messageId = "message-id"
        val message = Message().apply { text = "a-message" }

        Mockito.`when`(
            retrofitApi
                .getMessage(messageId, apiKey, user.id, connection.connectionId)
        ).thenReturn(
            RetroSuccess(
                MessageResponse(message)
            )
        )

        val result = client.getMessage(messageId).execute()

        verifySuccess(result, message)
    }

    @Test
    fun getMessageError() {

        val messageId = "message-id"

        Mockito.`when`(
            retrofitApi
                .getMessage(messageId, apiKey, user.id, connection.connectionId)
        ).thenReturn(
            RetroError(
                serverErrorCode
            )
        )

        val result = client.getMessage(messageId).execute()

        verifyError(
            result,
            serverErrorCode
        )
    }

    @Test
    fun deleteMessageSuccess() {

        val messageId = "message-id"
        val message = Message().apply { text = "a-message" }

        Mockito.`when`(
            retrofitApi
                .deleteMessage(messageId, apiKey, user.id, connection.connectionId)
        ).thenReturn(
            RetroSuccess(
                MessageResponse(message)
            )
        )

        val result = client.deleteMessage(messageId).execute()

        verifySuccess(result, message)
    }

    @Test
    fun deleteMessageError() {

        val messageId = "message-id"

        Mockito.`when`(
            retrofitApi
                .deleteMessage(messageId, apiKey, user.id, connection.connectionId)
        ).thenReturn(
            RetroError(
                serverErrorCode
            )
        )

        val result = client.deleteMessage(messageId).execute()

        verifyError(
            result,
            serverErrorCode
        )
    }

    @Test
    fun searchMessageSuccess() {

        val messageText = "message-a"
        val message = Message().apply { text = messageText }
        val searchRequest = SearchMessagesRequest("search-text", 0, 1)

        Mockito.`when`(
            retrofitApi
                .searchMessages(apiKey, connection.connectionId, searchRequest)
        ).thenReturn(RetroSuccess(SearchMessagesResponse(listOf(MessageResponse(Message().apply {
            text = messageText
        })))))

        val result = client.searchMessages(searchRequest).execute()

        verifySuccess(result, listOf(message))
    }

    @Test
    fun searchMessageError() {

        val searchRequest = SearchMessagesRequest("search-text", 0, 1)

        Mockito.`when`(
            retrofitApi
                .searchMessages(apiKey, connection.connectionId, searchRequest)
        ).thenReturn(RetroError(serverErrorCode))

        val result = client.searchMessages(searchRequest).execute()

        verifyError(result, serverErrorCode)
    }

    @Test
    fun sendMessageSuccess() {

        val messageText = "message-a"
        val message = Message().apply { text = messageText }

        Mockito.`when`(
            retrofitApi
                .sendMessage(
                    channelType,
                    channelId,
                    apiKey,
                    user.id,
                    connection.connectionId,
                    MessageRequest(message)
                )
        ).thenReturn(RetroSuccess(MessageResponse(message)))

        val result = client.sendMessage(channelType, channelId, message).execute()

        verifySuccess(result, message)
    }

    @Test
    fun sendMessageError() {

        val messageText = "message-a"
        val message = Message().apply { text = messageText }

        Mockito.`when`(
            retrofitApi
                .sendMessage(
                    channelType,
                    channelId,
                    apiKey,
                    user.id,
                    connection.connectionId,
                    MessageRequest(message)
                )
        ).thenReturn(RetroError(serverErrorCode))

        val result = client.sendMessage(channelType, channelId, message).execute()

        verifyError(result, serverErrorCode)
    }
}