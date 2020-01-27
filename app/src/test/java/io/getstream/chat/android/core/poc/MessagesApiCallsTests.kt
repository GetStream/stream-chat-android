package io.getstream.chat.android.core.poc

import io.getstream.chat.android.core.poc.library.*
import io.getstream.chat.android.core.poc.library.rest.*
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

    @Test
    fun sendActionSuccess() {

        val messageId = "message-id"
        val messageText = "message-a"
        val message = Message().apply { text = messageText }

        val request = SendActionRequest(channelId, messageId, "type", emptyMap())

        Mockito.`when`(
            retrofitApi
                .sendAction(
                    messageId,
                    apiKey,
                    user.id,
                    connection.connectionId,
                    request
                )
        ).thenReturn(RetroSuccess(MessageResponse(message)))

        val result = client.sendAction(request).execute()

        verifySuccess(result, message)
    }

    @Test
    fun sendActionError() {

        val messageId = "message-id"
        val request = SendActionRequest(channelId, messageId, "type", emptyMap())

        Mockito.`when`(
            retrofitApi
                .sendAction(
                    messageId,
                    apiKey,
                    user.id,
                    connection.connectionId,
                    request
                )
        ).thenReturn(RetroError(serverErrorCode))

        val result = client.sendAction(request).execute()

        verifyError(result, serverErrorCode)
    }

    @Test
    fun getRepliesSuccess() {

        val messageId = "message-id"
        val messageText = "message-a"
        val message = Message().apply { text = messageText }
        val limit = 10

        Mockito.`when`(
            retrofitApi
                .getReplies(
                    messageId,
                    apiKey,
                    user.id,
                    connection.connectionId,
                    limit
                )
        ).thenReturn(RetroSuccess(GetRepliesResponse(listOf(message))))

        val result = client.getReplies(messageId, limit).execute()

        verifySuccess(result, listOf(message))
    }

    @Test
    fun getRepliesError() {

        val messageId = "message-id"
        val limit = 10

        Mockito.`when`(
            retrofitApi
                .getReplies(
                    messageId,
                    apiKey,
                    user.id,
                    connection.connectionId,
                    limit
                )
        ).thenReturn(RetroError(serverErrorCode))

        val result = client.getReplies(messageId, limit).execute()

        verifyError(result, serverErrorCode)
    }

    @Test
    fun getRepliesMoreSuccess() {

        val messageId = "message-id"
        val messageText = "message-a"
        val message = Message().apply { text = messageText }
        val limit = 10
        val firstId = "first-id"

        Mockito.`when`(
            retrofitApi
                .getRepliesMore(
                    messageId,
                    apiKey,
                    user.id,
                    connection.connectionId,
                    limit,
                    firstId
                )
        ).thenReturn(RetroSuccess(GetRepliesResponse(listOf(message))))

        val result = client.getRepliesMore(messageId, firstId, limit).execute()

        verifySuccess(result, listOf(message))
    }

    @Test
    fun getRepliesMoreError() {

        val messageId = "message-id"
        val limit = 10
        val firstId = "first-id"

        Mockito.`when`(
            retrofitApi
                .getRepliesMore(
                    messageId,
                    apiKey,
                    user.id,
                    connection.connectionId,
                    limit,
                    firstId
                )
        ).thenReturn(RetroError(serverErrorCode))

        val result = client.getRepliesMore(messageId, firstId, limit).execute()

        verifyError(result, serverErrorCode)
    }

    @Test
    fun deleteReactionSuccess() {

        val messageId = "message-id"
        val messageText = "message-a"
        val message = Message().apply { text = messageText }
        val reactionType = "reactionType"

        Mockito.`when`(
            retrofitApi
                .deleteReaction(
                    messageId,
                    reactionType,
                    apiKey,
                    user.id,
                    connection.connectionId
                )
        ).thenReturn(RetroSuccess(MessageResponse(message)))

        val result = client.deleteReaction(messageId, reactionType).execute()

        verifySuccess(result, message)
    }

    @Test
    fun deleteReactionError() {

        val messageId = "message-id"
        val reactionType = "reactionType"

        Mockito.`when`(
            retrofitApi
                .deleteReaction(
                    messageId,
                    reactionType,
                    apiKey,
                    user.id,
                    connection.connectionId
                )
        ).thenReturn(RetroError(serverErrorCode))

        val result = client.deleteReaction(messageId, reactionType).execute()

        verifyError(result, serverErrorCode)
    }

    @Test
    fun getReactionsSuccess() {

        val messageId = "message-id"
        val offset = 0
        val limit = 1
        val reaction = Reaction(messageId)

        Mockito.`when`(
            retrofitApi
                .getReactions(
                    messageId,
                    apiKey,
                    connection.connectionId,
                    offset,
                    limit
                )
        ).thenReturn(RetroSuccess(GetReactionsResponse(listOf(reaction))))

        val result = client.getReactions(messageId, offset, limit).execute()

        verifySuccess(result, listOf(reaction))
    }

    @Test
    fun getReactionsError() {

        val messageId = "message-id"
        val offset = 0
        val limit = 1

        Mockito.`when`(
            retrofitApi
                .getReactions(
                    messageId,
                    apiKey,
                    connection.connectionId,
                    offset,
                    limit
                )
        ).thenReturn(RetroError(serverErrorCode))

        val result = client.getReactions(messageId, offset, limit).execute()

        verifyError(result, serverErrorCode)
    }
}