package io.getstream.chat.android.client

import io.getstream.chat.android.client.api.models.SearchMessagesRequest
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.SearchMessagesResult
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.test.TestCoroutineExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito

internal class MessagesApiCallsTests {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    lateinit var mock: MockClientBuilder
    lateinit var client: ChatClient

    @BeforeEach
    fun before() {
        mock = MockClientBuilder(testCoroutines.scope)
        client = mock.build()
    }

    @Test
    fun getMessageSuccess() {
        val messageId = "message-id"
        val message = Message(text = "a-message")

        Mockito.`when`(
            mock.api.getMessage(messageId)
        ).thenReturn(
            RetroSuccess(message).toRetrofitCall()
        )

        val result = client.getMessage(messageId).execute()

        verifySuccess(result, message)
    }

    @Test
    fun getMessageError() {
        val messageId = "message-id"

        Mockito.`when`(
            mock.api.getMessage(messageId)
        ).thenReturn(
            RetroError<Message>(mock.serverErrorCode).toRetrofitCall()
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
        val message = Message(text = "a-message")

        Mockito.`when`(
            mock.api.deleteMessage(messageId)
        ).thenReturn(
            RetroSuccess(message).toRetrofitCall()
        )

        val result = client.deleteMessage(messageId).execute()

        verifySuccess(result, message)
    }

    @Test
    fun deleteMessageError() {
        val messageId = "message-id"

        Mockito.`when`(
            mock.api.deleteMessage(messageId)
        ).thenReturn(
            RetroError<Message>(mock.serverErrorCode).toRetrofitCall()
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
        val message = Message(
            text = messageText,
            user = user
        )

        val messageFilter = Filters.eq("text", "search-text")
        val channelFilter = Filters.eq("cid", "cid")

        Mockito.`when`(
            mock.api.searchMessages(channelFilter, messageFilter, 0, 1, null, null)
        ).thenReturn(
            RetroSuccess(
                SearchMessagesResult(
                    messages = listOf(Message(text = messageText, user = user)),
                    next = "next-page",
                    previous = "prev-page",
                    resultsWarning = null,
                )
            ).toRetrofitCall()
        )

        val result = client.searchMessages(channelFilter, messageFilter, 0, 1).execute()

        verifySuccess(result, SearchMessagesResult(messages = listOf(message), next = "next-page", previous = "prev-page"))
    }

    @Test
    fun searchMessageError() {
        val messageFilter = Filters.eq("text", "search-text")
        val channelFilter = Filters.eq("cid", "cid")

        val searchRequest = SearchMessagesRequest(0, 1, channelFilter, messageFilter)

        Mockito.`when`(
            mock.api.searchMessages(searchRequest)
        ).thenReturn(RetroError<List<Message>>(mock.serverErrorCode).toRetrofitCall())

        val result = client.searchMessages(channelFilter, messageFilter, 0, 1).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun sendMessageSuccess() {
        val messageText = "message-a"
        val message = Message(text = messageText)

        Mockito.`when`(
            mock.api.sendMessage(
                mock.channelType,
                mock.channelId,
                message,
            )
        ).thenReturn(RetroSuccess(message).toRetrofitCall())

        val result = client.sendMessage(mock.channelType, mock.channelId, message).execute()

        verifySuccess(result, message)
    }

    @Test
    fun sendMessageError() {
        val messageText = "message-a"
        val message = Message(text = messageText)

        Mockito.`when`(
            mock.api.sendMessage(
                mock.channelType,
                mock.channelId,
                message,
            )
        ).thenReturn(RetroError<Message>(mock.serverErrorCode).toRetrofitCall())

        val result = client.sendMessage(mock.channelType, mock.channelId, message).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun sendActionSuccess() {
        val messageId = "message-id"
        val messageText = "message-a"
        val message = Message(text = messageText)

        val request = SendActionRequest(mock.channelId, messageId, "type", emptyMap())

        Mockito.`when`(
            mock.api.sendAction(request)
        ).thenReturn(RetroSuccess(message).toRetrofitCall())

        val result = client.sendAction(request).execute()

        verifySuccess(result, message)
    }

    @Test
    fun sendActionError() {
        val messageId = "message-id"
        val request = SendActionRequest(mock.channelId, messageId, "type", emptyMap())

        Mockito.`when`(
            mock.api.sendAction(request)
        ).thenReturn(RetroError<Message>(mock.serverErrorCode).toRetrofitCall())

        val result = client.sendAction(request).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun getRepliesSuccess() {
        val messageId = "message-id"
        val messageText = "message-a"
        val message = Message(text = messageText)
        val limit = 10

        Mockito.`when`(
            mock.api.getReplies(
                messageId,
                limit,
            )
        ).thenReturn(RetroSuccess(listOf(message)).toRetrofitCall())

        val result = client.getReplies(messageId, limit).execute()

        verifySuccess(result, listOf(message))
    }

    @Test
    fun getRepliesError() {
        val messageId = "message-id"
        val limit = 10

        Mockito.`when`(
            mock.api.getReplies(
                messageId,
                limit,
            )
        ).thenReturn(RetroError<List<Message>>(mock.serverErrorCode).toRetrofitCall())

        val result = client.getReplies(messageId, limit).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun getRepliesMoreSuccess() {
        val messageId = "message-id"
        val messageText = "message-a"
        val message = Message(text = messageText)
        val limit = 10
        val firstId = "first-id"

        Mockito.`when`(
            mock.api.getRepliesMore(
                messageId,
                firstId,
                limit,
            )
        ).thenReturn(RetroSuccess(listOf(message)).toRetrofitCall())

        val result = client.getRepliesMore(messageId, firstId, limit).execute()

        verifySuccess(result, listOf(message))
    }

    @Test
    fun getRepliesMoreError() {
        val messageId = "message-id"
        val limit = 10
        val firstId = "first-id"

        Mockito.`when`(
            mock.api.getRepliesMore(
                messageId,
                firstId,
                limit,
            )
        ).thenReturn(RetroError<List<Message>>(mock.serverErrorCode).toRetrofitCall())

        val result = client.getRepliesMore(messageId, firstId, limit).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun deleteReactionSuccess() {
        val messageId = "message-id"
        val messageText = "message-a"
        val message = Message(text = messageText)
        val reactionType = "reactionType"

        Mockito.`when`(
            mock.api.deleteReaction(
                messageId,
                reactionType,
            )
        ).thenReturn(RetroSuccess(message).toRetrofitCall())

        val result = client.deleteReaction(messageId, reactionType).execute()

        verifySuccess(result, message)
    }

    @Test
    fun deleteReactionError() {
        val messageId = "message-id"
        val reactionType = "reactionType"

        Mockito.`when`(
            mock.api.deleteReaction(
                messageId,
                reactionType,
            )
        ).thenReturn(RetroError<Message>(mock.serverErrorCode).toRetrofitCall())

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
            mock.api.getReactions(
                messageId,
                offset,
                limit,
            )
        ).thenReturn(RetroSuccess(listOf(reaction)).toRetrofitCall())

        val result = client.getReactions(messageId, offset, limit).execute()

        verifySuccess(result, listOf(reaction))
    }

    @Test
    fun getReactionsError() {
        val messageId = "message-id"
        val offset = 0
        val limit = 1

        Mockito.`when`(
            mock.api.getReactions(
                messageId,
                offset,
                limit,
            )
        ).thenReturn(RetroError<List<Reaction>>(mock.serverErrorCode).toRetrofitCall())

        val result = client.getReactions(messageId, offset, limit).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun updateMessageSuccess() {
        val messageId = "message-id"
        val messageText = "message-a"
        val message = Message(
            text = messageText,
            id = messageId,
        )

        Mockito.`when`(
            mock.api.updateMessage(message)
        ).thenReturn(RetroSuccess(message).toRetrofitCall())

        val result = client.updateMessage(message).execute()

        verifySuccess(result, message)
    }

    @Test
    fun updateMessageError() {
        val messageId = "message-id"
        val messageText = "message-a"
        val message = Message(
            text = messageText,
            id = messageId,
        )

        Mockito.`when`(
            mock.api.updateMessage(message)
        ).thenReturn(RetroError<Message>(mock.serverErrorCode).toRetrofitCall())

        val result = client.updateMessage(message).execute()

        verifyError(result, mock.serverErrorCode)
    }
}
