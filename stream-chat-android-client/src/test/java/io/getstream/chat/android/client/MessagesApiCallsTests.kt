package io.getstream.chat.android.client

import io.getstream.chat.android.client.api.models.GetReactionsResponse
import io.getstream.chat.android.client.api.models.GetRepliesResponse
import io.getstream.chat.android.client.api.models.MessageRequest
import io.getstream.chat.android.client.api.models.MessageResponse
import io.getstream.chat.android.client.api.models.SearchMessagesRequest
import io.getstream.chat.android.client.api.models.SearchMessagesResponse
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyError
import io.getstream.chat.android.client.utils.verifySuccess
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

internal class MessagesApiCallsTests {

    lateinit var mock: MockClientBuilder
    lateinit var client: ChatClient

    @Before
    fun before() {
        mock = MockClientBuilder()
        client = mock.build()
    }

    @Test
    fun getMessageSuccess() {

        val messageId = "message-id"
        val message = Message()
            .apply { text = "a-message" }

        Mockito.`when`(
            mock.retrofitApi
                .getMessage(messageId, mock.apiKey, mock.userId, mock.connectionId)
        ).thenReturn(
            RetroSuccess(MessageResponse(message)).toRetrofitCall()
        )

        val result = client.getMessage(messageId).execute()

        verifySuccess(result, message)
    }

    @Test
    fun getMessageError() {

        val messageId = "message-id"

        Mockito.`when`(
            mock.retrofitApi
                .getMessage(messageId, mock.apiKey, mock.userId, mock.connectionId)
        ).thenReturn(
            RetroError<MessageResponse>(mock.serverErrorCode).toRetrofitCall()
        )

        val result = client.getMessage(messageId).execute()

        verifyError(
            result,
            mock.serverErrorCode
        )
    }

    @Test
    fun deleteMessageSuccess() {

        val messageId = "message-id"
        val message = Message()
            .apply { text = "a-message" }

        Mockito.`when`(
            mock.retrofitApi
                .deleteMessage(messageId, mock.apiKey, mock.userId, mock.connectionId)
        ).thenReturn(
            RetroSuccess(MessageResponse(message)).toRetrofitCall()
        )

        val result = client.deleteMessage(messageId).execute()

        verifySuccess(result, message)
    }

    @Test
    fun deleteMessageError() {

        val messageId = "message-id"

        Mockito.`when`(
            mock.retrofitApi
                .deleteMessage(messageId, mock.apiKey, mock.userId, mock.connectionId)
        ).thenReturn(
            RetroError<MessageResponse>(mock.serverErrorCode).toRetrofitCall()
        )

        val result = client.deleteMessage(messageId).execute()

        verifyError(
            result,
            mock.serverErrorCode
        )
    }

    @Test
    fun searchMessageSuccess() {

        val messageText = "message-a"
        val user = User()
        val message = Message()
            .apply {
                this.text = messageText
                this.user = user
            }

        val messageFilter = Filters.eq("text", "search-text")
        val channelFilter = Filters.eq("cid", "cid")

        val searchRequest = SearchMessagesRequest(0, 1, channelFilter, messageFilter)

        Mockito.`when`(
            mock.retrofitApi
                .searchMessages(mock.apiKey, mock.connectionId, searchRequest)
        ).thenReturn(
            RetroSuccess(
                SearchMessagesResponse(
                    listOf(
                        MessageResponse(
                            Message().apply {
                                this.text = messageText
                                this.user = user
                            }
                        )
                    )
                )
            ).toRetrofitCall()
        )

        val result = client.searchMessages(searchRequest).execute()

        verifySuccess(result, listOf(message))
    }

    @Test
    fun searchMessageError() {

        val messageFilter = Filters.eq("text", "search-text")
        val channelFilter = Filters.eq("cid", "cid")

        val searchRequest = SearchMessagesRequest(0, 1, channelFilter, messageFilter)

        Mockito.`when`(
            mock.retrofitApi
                .searchMessages(mock.apiKey, mock.connectionId, searchRequest)
        ).thenReturn(RetroError<SearchMessagesResponse>(mock.serverErrorCode).toRetrofitCall())

        val result = client.searchMessages(searchRequest).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun sendMessageSuccess() {

        val messageText = "message-a"
        val message = Message()
            .apply { text = messageText }

        Mockito.`when`(
            mock.retrofitApi
                .sendMessage(
                    mock.channelType,
                    mock.channelId,
                    mock.apiKey,
                    mock.userId,
                    mock.connectionId,
                    MessageRequest(message)
                )
        ).thenReturn(RetroSuccess(MessageResponse(message)).toRetrofitCall())

        val result = client.sendMessage(mock.channelType, mock.channelId, message).execute()

        verifySuccess(result, message)
    }

    @Test
    fun sendMessageError() {

        val messageText = "message-a"
        val message = Message().apply { text = messageText }

        Mockito.`when`(
            mock.retrofitApi
                .sendMessage(
                    mock.channelType,
                    mock.channelId,
                    mock.apiKey,
                    mock.userId,
                    mock.connectionId,
                    MessageRequest(message)
                )
        ).thenReturn(RetroError<MessageResponse>(mock.serverErrorCode).toRetrofitCall())

        val result = client.sendMessage(mock.channelType, mock.channelId, message).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun sendActionSuccess() {

        val messageId = "message-id"
        val messageText = "message-a"
        val message = Message()
            .apply { text = messageText }

        val request = SendActionRequest(mock.channelId, messageId, "type", emptyMap())

        Mockito.`when`(
            mock.retrofitApi
                .sendAction(
                    messageId,
                    mock.apiKey,
                    mock.userId,
                    mock.connectionId,
                    request
                )
        ).thenReturn(RetroSuccess(MessageResponse(message)).toRetrofitCall())

        val result = client.sendAction(request).execute()

        verifySuccess(result, message)
    }

    @Test
    fun sendActionError() {

        val messageId = "message-id"
        val request = SendActionRequest(mock.channelId, messageId, "type", emptyMap())

        Mockito.`when`(
            mock.retrofitApi
                .sendAction(
                    messageId,
                    mock.apiKey,
                    mock.userId,
                    mock.connectionId,
                    request
                )
        ).thenReturn(RetroError<MessageResponse>(mock.serverErrorCode).toRetrofitCall())

        val result = client.sendAction(request).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun getRepliesSuccess() {

        val messageId = "message-id"
        val messageText = "message-a"
        val message = Message()
            .apply { text = messageText }
        val limit = 10

        Mockito.`when`(
            mock.retrofitApi
                .getReplies(
                    messageId,
                    mock.apiKey,
                    mock.userId,
                    mock.connectionId,
                    limit
                )
        ).thenReturn(RetroSuccess(GetRepliesResponse(listOf(message))).toRetrofitCall())

        val result = client.getReplies(messageId, limit).execute()

        verifySuccess(result, listOf(message))
    }

    @Test
    fun getRepliesError() {

        val messageId = "message-id"
        val limit = 10

        Mockito.`when`(
            mock.retrofitApi
                .getReplies(
                    messageId,
                    mock.apiKey,
                    mock.userId,
                    mock.connectionId,
                    limit
                )
        ).thenReturn(RetroError<GetRepliesResponse>(mock.serverErrorCode).toRetrofitCall())

        val result = client.getReplies(messageId, limit).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun getRepliesMoreSuccess() {

        val messageId = "message-id"
        val messageText = "message-a"
        val message = Message()
            .apply { text = messageText }
        val limit = 10
        val firstId = "first-id"

        Mockito.`when`(
            mock.retrofitApi
                .getRepliesMore(
                    messageId,
                    mock.apiKey,
                    mock.userId,
                    mock.connectionId,
                    limit,
                    firstId
                )
        ).thenReturn(RetroSuccess(GetRepliesResponse(listOf(message))).toRetrofitCall())

        val result = client.getRepliesMore(messageId, firstId, limit).execute()

        verifySuccess(result, listOf(message))
    }

    @Test
    fun getRepliesMoreError() {

        val messageId = "message-id"
        val limit = 10
        val firstId = "first-id"

        Mockito.`when`(
            mock.retrofitApi
                .getRepliesMore(
                    messageId,
                    mock.apiKey,
                    mock.userId,
                    mock.connectionId,
                    limit,
                    firstId
                )
        ).thenReturn(RetroError<GetRepliesResponse>(mock.serverErrorCode).toRetrofitCall())

        val result = client.getRepliesMore(messageId, firstId, limit).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun deleteReactionSuccess() {

        val messageId = "message-id"
        val messageText = "message-a"
        val message = Message()
            .apply { text = messageText }
        val reactionType = "reactionType"

        Mockito.`when`(
            mock.retrofitApi
                .deleteReaction(
                    messageId,
                    reactionType,
                    mock.apiKey,
                    mock.userId,
                    mock.connectionId
                )
        ).thenReturn(RetroSuccess(MessageResponse(message)).toRetrofitCall())

        val result = client.deleteReaction(messageId, reactionType).execute()

        verifySuccess(result, message)
    }

    @Test
    fun deleteReactionError() {

        val messageId = "message-id"
        val reactionType = "reactionType"

        Mockito.`when`(
            mock.retrofitApi
                .deleteReaction(
                    messageId,
                    reactionType,
                    mock.apiKey,
                    mock.userId,
                    mock.connectionId
                )
        ).thenReturn(RetroError<MessageResponse>(mock.serverErrorCode).toRetrofitCall())

        val result = client.deleteReaction(messageId, reactionType).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun getReactionsSuccess() {

        val messageId = "message-id"
        val reactionType = "reaction-type"
        val score = 10
        val offset = 0
        val limit = 1
        val reaction = Reaction(messageId, reactionType, score)

        Mockito.`when`(
            mock.retrofitApi
                .getReactions(
                    messageId,
                    mock.apiKey,
                    mock.connectionId,
                    offset,
                    limit
                )
        ).thenReturn(RetroSuccess(GetReactionsResponse(listOf(reaction))).toRetrofitCall())

        val result = client.getReactions(messageId, offset, limit).execute()

        verifySuccess(result, listOf(reaction))
    }

    @Test
    fun getReactionsError() {

        val messageId = "message-id"
        val offset = 0
        val limit = 1

        Mockito.`when`(
            mock.retrofitApi
                .getReactions(
                    messageId,
                    mock.apiKey,
                    mock.connectionId,
                    offset,
                    limit
                )
        ).thenReturn(RetroError<GetReactionsResponse>(mock.serverErrorCode).toRetrofitCall())

        val result = client.getReactions(messageId, offset, limit).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun updateMessageSuccess() {

        val messageId = "message-id"
        val messageText = "message-a"
        val message = Message()
            .apply {
                text = messageText
                id = messageId
            }

        Mockito.`when`(
            mock.retrofitApi
                .updateMessage(
                    messageId,
                    mock.apiKey,
                    mock.userId,
                    mock.connectionId,
                    MessageRequest(message)
                )
        ).thenReturn(RetroSuccess(MessageResponse(message)).toRetrofitCall())

        val result = client.updateMessage(message).execute()

        verifySuccess(result, message)
    }

    @Test
    fun updateMessageError() {

        val messageId = "message-id"
        val messageText = "message-a"
        val message = Message()
            .apply {
                text = messageText
                id = messageId
            }

        Mockito.`when`(
            mock.retrofitApi
                .updateMessage(
                    messageId,
                    mock.apiKey,
                    mock.userId,
                    mock.connectionId,
                    MessageRequest(message)
                )
        ).thenReturn(RetroError<MessageResponse>(mock.serverErrorCode).toRetrofitCall())

        val result = client.updateMessage(message).execute()

        verifyError(result, mock.serverErrorCode)
    }
}
