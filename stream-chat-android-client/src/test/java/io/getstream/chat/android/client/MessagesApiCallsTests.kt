/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client

import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.SearchMessagesResult
import io.getstream.chat.android.models.User
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Result
import io.getstream.result.StreamError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
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
        mock = MockClientBuilder(testCoroutines)

        client = mock.build()
    }

    @Test
    fun getMessageSuccess() = runTest {
        val messageId = "message-id"
        val message = Message(text = "a-message")

        whenever(mock.api.getMessage(messageId)).thenReturn(RetroSuccess(message).toRetrofitCall())
        whenever(mock.attachmentSender.sendAttachments(any(), any(), any(), any(), any()))
            .doReturn(Result.Success(message))

        val result = client.getMessage(messageId).await()

        verifySuccess(result, message)
    }

    @Test
    fun getMessageError() = runTest {
        val messageId = "message-id"

        Mockito.`when`(
            mock.api.getMessage(messageId)
        ).thenReturn(
            RetroError<Message>(mock.serverErrorCode).toRetrofitCall()
        )

        val result = client.getMessage(messageId).await()

        verifyError(
            result,
            mock.serverErrorCode
        )
    }

    @Test
    fun deleteMessageSuccess() = runTest {
        val messageId = "message-id"
        val message = Message(text = "a-message")

        Mockito.`when`(
            mock.api.deleteMessage(messageId)
        ).thenReturn(
            RetroSuccess(message).toRetrofitCall()
        )

        val result = client.deleteMessage(messageId).await()

        verifySuccess(result, message)
    }

    @Test
    fun deleteMessageError() = runTest {
        val messageId = "message-id"

        Mockito.`when`(
            mock.api.deleteMessage(messageId)
        ).thenReturn(
            RetroError<Message>(mock.serverErrorCode).toRetrofitCall()
        )

        val result = client.deleteMessage(messageId).await()

        verifyError(
            result,
            mock.serverErrorCode
        )
    }

    @Test
    fun searchMessageSuccess() = runTest {
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

        val result = client.searchMessages(channelFilter, messageFilter, 0, 1).await()

        verifySuccess(
            result,
            SearchMessagesResult(messages = listOf(message), next = "next-page", previous = "prev-page")
        )
    }

    @Test
    fun searchMessageError() = runTest {
        val messageFilter = Filters.eq("text", "search-text")
        val channelFilter = Filters.eq("cid", "cid")

        Mockito.`when`(
            mock.api.searchMessages(channelFilter, messageFilter, 0, 1, null, null)
        ).thenReturn(RetroError<SearchMessagesResult>(mock.serverErrorCode).toRetrofitCall())

        val result = client.searchMessages(channelFilter, messageFilter, 0, 1).await()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun sendMessageSuccess() = runTest {
        val messageText = "message-a"
        val message = Message(text = messageText)

        whenever(
            mock.api.sendMessage(
                mock.channelType,
                mock.channelId,
                message,
            )
        ).thenReturn(RetroSuccess(message).toRetrofitCall())

        whenever(mock.attachmentSender.sendAttachments(any(), any(), any(), any(), any()))
            .doReturn(Result.Success(message))

        val result = client.sendMessage(mock.channelType, mock.channelId, message).await()

        verifySuccess(result, message)
    }

    @Test
    fun sendMessageError() = runTest {
        val messageText = "message-a"
        val message = Message(text = messageText)
        val requestResult = Result.Failure(StreamError.GenericError(message = ""))

        whenever(
            mock.api.sendMessage(
                mock.channelType,
                mock.channelId,
                message,
            )
        ).thenReturn(RetroError<Message>(mock.serverErrorCode).toRetrofitCall())

        whenever(mock.attachmentSender.sendAttachments(any(), any(), any(), any(), any()))
            .doReturn(requestResult)

        val result = client.sendMessage(mock.channelType, mock.channelId, message).await()

        result `should be equal to` requestResult
    }

    @Test
    fun sendActionSuccess() = runTest {
        val messageId = "message-id"
        val messageText = "message-a"
        val message = Message(text = messageText)

        val request = SendActionRequest(mock.channelId, messageId, "type", emptyMap())

        Mockito.`when`(
            mock.api.sendAction(request)
        ).thenReturn(RetroSuccess(message).toRetrofitCall())

        val result = client.sendAction(request).await()

        verifySuccess(result, message)
    }

    @Test
    fun sendActionError() = runTest {
        val messageId = "message-id"
        val request = SendActionRequest(mock.channelId, messageId, "type", emptyMap())

        Mockito.`when`(
            mock.api.sendAction(request)
        ).thenReturn(RetroError<Message>(mock.serverErrorCode).toRetrofitCall())

        val result = client.sendAction(request).await()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun getRepliesSuccess() = runTest {
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

        val result = client.getReplies(messageId, limit).await()

        verifySuccess(result, listOf(message))
    }

    @Test
    fun getRepliesError() = runTest {
        val messageId = "message-id"
        val limit = 10

        Mockito.`when`(
            mock.api.getReplies(
                messageId,
                limit,
            )
        ).thenReturn(RetroError<List<Message>>(mock.serverErrorCode).toRetrofitCall())

        val result = client.getReplies(messageId, limit).await()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun getRepliesMoreSuccess() = runTest {
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

        val result = client.getRepliesMore(messageId, firstId, limit).await()

        verifySuccess(result, listOf(message))
    }

    @Test
    fun getRepliesMoreError() = runTest {
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

        val result = client.getRepliesMore(messageId, firstId, limit).await()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun deleteReactionSuccess() = runTest {
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

        val result = client.deleteReaction(messageId, reactionType).await()

        verifySuccess(result, message)
    }

    @Test
    fun deleteReactionError() = runTest {
        val messageId = "message-id"
        val reactionType = "reactionType"

        Mockito.`when`(
            mock.api.deleteReaction(
                messageId,
                reactionType,
            )
        ).thenReturn(RetroError<Message>(mock.serverErrorCode).toRetrofitCall())

        val result = client.deleteReaction(messageId, reactionType).await()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun getReactionsSuccess() = runTest {
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

        val result = client.getReactions(messageId, offset, limit).await()

        verifySuccess(result, listOf(reaction))
    }

    @Test
    fun getReactionsError() = runTest {
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

        val result = client.getReactions(messageId, offset, limit).await()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun updateMessageSuccess() = runTest {
        val messageId = "message-id"
        val messageText = "message-a"
        val message = Message(
            text = messageText,
            id = messageId,
        )

        Mockito.`when`(
            mock.api.updateMessage(message)
        ).thenReturn(RetroSuccess(message).toRetrofitCall())

        val result = client.updateMessage(message).await()

        verifySuccess(result, message)
    }

    @Test
    fun updateMessageError() = runTest {
        val messageId = "message-id"
        val messageText = "message-a"
        val message = Message(
            text = messageText,
            id = messageId,
        )

        Mockito.`when`(
            mock.api.updateMessage(message)
        ).thenReturn(RetroError<Message>(mock.serverErrorCode).toRetrofitCall())

        val result = client.updateMessage(message).await()

        verifyError(result, mock.serverErrorCode)
    }
}
