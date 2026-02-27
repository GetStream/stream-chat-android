/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.channel.ChannelMessagesUpdateLogic
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.channel.state.ChannelStateLogicProvider
import io.getstream.chat.android.client.chatclient.BaseChatClientTest
import io.getstream.chat.android.client.clientstate.UserState
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.verifyNetworkError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.PendingMessage
import io.getstream.chat.android.models.QueryReactionsResult
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomPendingMessage
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.asCall
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File

/**
 * Tests for the [ChatClient] message endpoints.
 */
@Suppress("LargeClass")
internal class ChatClientMessageApiTests : BaseChatClientTest() {

    @Test
    fun getMessageSuccess() = runTest {
        // given
        val messageId = randomString()
        val plugin = mock<Plugin>()
        val message = randomMessage(id = messageId)
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenGetMessageResult(message.asCall())
            .get()
        // when
        val result = sut.getMessage(messageId).await()
        // then
        verifySuccess(result, message)
        verify(plugin).onGetMessageResult(messageId, result)
    }

    @Test
    fun getMessageError() = runTest {
        // given
        val messageId = randomString()
        val plugin = mock<Plugin>()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenGetMessageResult(RetroError<Message>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.getMessage(messageId).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(plugin).onGetMessageResult(messageId, result)
    }

    @Test
    fun getPendingMessageSuccess() = runTest {
        // given
        val messageId = randomString()
        val pendingMessage = randomPendingMessage()
        val sut = Fixture()
            .givenGetPendingMessageResult(pendingMessage.asCall())
            .get()
        // when
        val result = sut.getPendingMessage(messageId).await()
        // then
        verifySuccess(result, pendingMessage)
    }

    @Test
    fun getPendingMessageError() = runTest {
        // given
        val messageId = randomString()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenGetPendingMessageResult(RetroError<PendingMessage>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.getPendingMessage(messageId).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun deleteMessageSuccess() = runTest {
        // given
        val messageId = randomString()
        val plugin = mock<Plugin>()
        val message = randomMessage(id = messageId)
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenDeleteMessageResult(message.asCall())
            .get()
        // when
        val result = sut.deleteMessage(messageId).await()
        // then
        verifySuccess(result, message)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onMessageDeletePrecondition(messageId)
        inOrder.verify(plugin).onMessageDeleteRequest(messageId)
        inOrder.verify(plugin).onMessageDeleteResult(messageId, result)
    }

    @Test
    fun deleteMessageError() = runTest {
        // given
        val messageId = randomString()
        val plugin = mock<Plugin>()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenDeleteMessageResult(RetroError<Message>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.deleteMessage(messageId).await()
        // then
        verifyNetworkError(result, errorCode)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onMessageDeletePrecondition(messageId)
        inOrder.verify(plugin).onMessageDeleteRequest(messageId)
        inOrder.verify(plugin).onMessageDeleteResult(messageId, result)
    }

    @Test
    fun deleteMessageForMeSuccess() = runTest {
        // given
        val messageId = randomString()
        val plugin = mock<Plugin>()
        val message = randomMessage(id = messageId)
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenDeleteMessageResult(message.asCall())
            .get()
        // when
        val result = sut.deleteMessageForMe(messageId).await()
        // then
        verifySuccess(result, message)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onDeleteMessageForMePrecondition(messageId)
        inOrder.verify(plugin).onDeleteMessageForMeRequest(messageId)
        inOrder.verify(plugin).onDeleteMessageForMeResult(messageId, result)
    }

    @Test
    fun deleteMessageForMeError() = runTest {
        // given
        val messageId = randomString()
        val plugin = mock<Plugin>()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenDeleteMessageResult(RetroError<Message>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.deleteMessageForMe(messageId).await()
        // then
        verifyNetworkError(result, errorCode)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onDeleteMessageForMePrecondition(messageId)
        inOrder.verify(plugin).onDeleteMessageForMeRequest(messageId)
        inOrder.verify(plugin).onDeleteMessageForMeResult(messageId, result)
    }

    @Test
    fun sendMessageSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val message = randomMessage()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenSendMessageResult(message.asCall())
            .givenSendAttachmentsResult(Result.Success(message))
            .get()
        // when
        val result = sut.sendMessage(channelType, channelId, message).await()
        // then
        verifySuccess(result, message)
        verify(plugin).onMessageSendResult(result, channelType, channelId, message)
    }

    @Test
    fun sendMessageError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val messageText = randomString()
        val plugin = mock<Plugin>()
        val errorCode = positiveRandomInt()
        val message = randomMessage(text = messageText)
        val requestResult = Result.Failure(Error.GenericError(message = randomString()))
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenSendMessageResult(RetroError<Message>(errorCode).toRetrofitCall())
            .givenSendAttachmentsResult(requestResult)
            .get()
        // when
        val result = sut.sendMessage(channelType, channelId, message).await()
        // then
        assertEquals(requestResult, result)
        verify(plugin, never()).onMessageSendResult(any(), any(), any(), any())
    }

    @Test
    fun sendActionSuccess() = runTest {
        // given
        val message = randomMessage()
        val request = Mother.randomSendActionRequest()
        val sut = Fixture()
            .givenSendActionResult(message.asCall())
            .get()
        // when
        val result = sut.sendAction(request).await()
        // then
        verifySuccess(result, message)
    }

    @Test
    fun sendActionError() = runTest {
        // given
        val request = Mother.randomSendActionRequest()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenSendActionResult(RetroError<Message>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.sendAction(request).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun sendGiphySuccess() = runTest {
        // given
        val inputMessage = randomMessage()
        val resultMessage = randomMessage()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenSendActionResult(resultMessage.asCall())
            .get()
        // when
        val result = sut.sendGiphy(inputMessage).await()
        // then
        val expectedAction = SendActionRequest(
            channelId = inputMessage.cid,
            messageId = inputMessage.id,
            type = inputMessage.type,
            formData = mapOf("image_action" to "send"),
        )
        verifySuccess(result, resultMessage)
        verify(api).sendAction(expectedAction)
        verify(plugin).onGiphySendResult(inputMessage.cid, result)
    }

    @Test
    fun sendGiphyError() = runTest {
        // given
        val inputMessage = randomMessage()
        val plugin = mock<Plugin>()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenSendActionResult(RetroError<Message>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.sendGiphy(inputMessage).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(plugin).onGiphySendResult(inputMessage.cid, result)
    }

    @Test
    fun shuffleGiphySuccess() = runTest {
        // given
        val inputMessage = randomMessage()
        val resultMessage = randomMessage()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenSendActionResult(resultMessage.asCall())
            .get()
        // when
        val result = sut.shuffleGiphy(inputMessage).await()
        // then
        val expectedAction = SendActionRequest(
            channelId = inputMessage.cid,
            messageId = inputMessage.id,
            type = inputMessage.type,
            formData = mapOf("image_action" to "shuffle"),
        )
        verifySuccess(result, resultMessage)
        verify(api).sendAction(expectedAction)
        verify(plugin).onShuffleGiphyResult(inputMessage.cid, result)
    }

    @Test
    fun shuffleGiphyError() = runTest {
        // given
        val inputMessage = randomMessage()
        val errorCode = positiveRandomInt()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenSendActionResult(RetroError<Message>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.shuffleGiphy(inputMessage).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(plugin).onShuffleGiphyResult(inputMessage.cid, result)
    }

    @Test
    fun getRepliesSuccess() = runTest {
        // given
        val messageId = randomString()
        val messageText = randomString()
        val message = Message(id = messageId, text = messageText)
        val limit = randomInt()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenGetRepliesResult(listOf(message).asCall())
            .get()
        // when
        val result = sut.getReplies(messageId, limit).await()
        // then
        verifySuccess(result, listOf(message))
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onGetRepliesPrecondition(messageId)
        inOrder.verify(plugin).onGetRepliesRequest(messageId, limit)
        inOrder.verify(plugin).onGetRepliesResult(result, messageId, limit)
    }

    @Test
    fun getRepliesError() = runTest {
        // given
        val messageId = randomString()
        val limit = randomInt()
        val plugin = mock<Plugin>()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenGetRepliesResult(RetroError<List<Message>>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.getReplies(messageId, limit).await()
        // then
        verifyNetworkError(result, errorCode)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onGetRepliesPrecondition(messageId)
        inOrder.verify(plugin).onGetRepliesRequest(messageId, limit)
        inOrder.verify(plugin).onGetRepliesResult(result, messageId, limit)
    }

    @Test
    fun getNewerRepliesSuccess() = runTest {
        // given
        val parentId = randomString()
        val limit = randomInt()
        val lastId = randomString()
        val plugin = mock<Plugin>()
        val replies = listOf(randomMessage())
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenGetNewerRepliesResult(replies.asCall())
            .get()
        // when
        val result = sut.getNewerReplies(parentId, limit, lastId).await()
        // then
        verifySuccess(result, replies)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onGetRepliesPrecondition(parentId)
        inOrder.verify(plugin).onGetNewerRepliesRequest(parentId, limit, lastId)
        inOrder.verify(plugin).onGetNewerRepliesResult(result, parentId, limit, lastId)
    }

    @Test
    fun getNewerRepliesError() = runTest {
        // given
        val parentId = randomString()
        val limit = randomInt()
        val lastId = randomString()
        val plugin = mock<Plugin>()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenGetNewerRepliesResult(RetroError<List<Message>>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.getNewerReplies(parentId, limit, lastId).await()
        // then
        verifyNetworkError(result, errorCode)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onGetRepliesPrecondition(parentId)
        inOrder.verify(plugin).onGetNewerRepliesRequest(parentId, limit, lastId)
        inOrder.verify(plugin).onGetNewerRepliesResult(result, parentId, limit, lastId)
    }

    @Test
    fun getRepliesMoreSuccess() = runTest {
        // given
        val messageId = randomString()
        val limit = randomInt()
        val firstId = randomString()
        val plugin = mock<Plugin>()
        val messages = listOf(randomMessage())
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenGetRepliesMoreResult(messages.asCall())
            .get()
        // when
        val result = sut.getRepliesMore(messageId, firstId, limit).await()
        // then
        verifySuccess(result, messages)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onGetRepliesPrecondition(messageId)
        inOrder.verify(plugin).onGetRepliesMoreRequest(messageId, firstId, limit)
        inOrder.verify(plugin).onGetRepliesMoreResult(result, messageId, firstId, limit)
    }

    @Test
    fun getRepliesMoreError() = runTest {
        // given
        val messageId = randomString()
        val limit = randomInt()
        val firstId = randomString()
        val plugin = mock<Plugin>()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenGetRepliesMoreResult(RetroError<List<Message>>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.getRepliesMore(messageId, firstId, limit).await()
        // then
        verifyNetworkError(result, errorCode)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onGetRepliesPrecondition(messageId)
        inOrder.verify(plugin).onGetRepliesMoreRequest(messageId, firstId, limit)
        inOrder.verify(plugin).onGetRepliesMoreResult(result, messageId, firstId, limit)
    }

    @Test
    fun sendReactionSuccess() = runTest {
        // given
        val currentUser = randomUser()
        val reaction = randomReaction()
        val enforceUnique = randomBoolean()
        val skipPush = randomBoolean()
        val cid = randomCID()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenUser(currentUser)
            .givenPlugin(plugin)
            .givenSendReactionResult(reaction.asCall())
            .get()
        // when
        val result = sut.sendReaction(reaction, enforceUnique, cid, skipPush).await()
        // then
        verifySuccess(result, reaction)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onSendReactionPrecondition(cid, currentUser, reaction)
        inOrder.verify(plugin).onSendReactionRequest(eq(cid), any(), eq(enforceUnique), eq(skipPush), eq(currentUser))
        inOrder.verify(plugin).onSendReactionResult(eq(cid), any(), eq(enforceUnique), eq(currentUser), eq(result))
    }

    @Test
    fun sendReactionError() = runTest {
        // given
        val currentUser = randomUser()
        val reaction = randomReaction()
        val enforceUnique = randomBoolean()
        val cid = randomCID()
        val skipPush = randomBoolean()
        val plugin = mock<Plugin>()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenUser(currentUser)
            .givenPlugin(plugin)
            .givenSendReactionResult(RetroError<Reaction>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.sendReaction(reaction, enforceUnique, cid, skipPush).await()
        // then
        verifyNetworkError(result, errorCode)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onSendReactionPrecondition(cid, currentUser, reaction)
        inOrder.verify(plugin).onSendReactionRequest(eq(cid), any(), eq(enforceUnique), eq(skipPush), eq(currentUser))
        inOrder.verify(plugin).onSendReactionResult(eq(cid), any(), eq(enforceUnique), eq(currentUser), eq(result))
    }

    @Test
    fun deleteReactionSuccess() = runTest {
        // given
        val currentUser = randomUser()
        val messageId = randomString()
        val reactionType = randomString()
        val cid = randomCID()
        val plugin = mock<Plugin>()
        val message = randomMessage()
        val sut = Fixture()
            .givenUser(currentUser)
            .givenPlugin(plugin)
            .givenDeleteReactionResult(message.asCall())
            .get()
        // when
        val result = sut.deleteReaction(messageId, reactionType, cid).await()
        // then
        verifySuccess(result, message)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onDeleteReactionPrecondition(currentUser)
        inOrder.verify(plugin).onDeleteReactionRequest(cid, messageId, reactionType, currentUser)
        inOrder.verify(plugin).onDeleteReactionResult(cid, messageId, reactionType, currentUser, result)
    }

    @Test
    fun deleteReactionError() = runTest {
        // given
        val currentUser = randomUser()
        val messageId = randomString()
        val reactionType = randomString()
        val cid = randomCID()
        val plugin = mock<Plugin>()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenUser(currentUser)
            .givenPlugin(plugin)
            .givenDeleteReactionResult(RetroError<Message>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.deleteReaction(messageId, reactionType, cid).await()
        // then
        verifyNetworkError(result, errorCode)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onDeleteReactionPrecondition(currentUser)
        inOrder.verify(plugin).onDeleteReactionRequest(cid, messageId, reactionType, currentUser)
        inOrder.verify(plugin).onDeleteReactionResult(cid, messageId, reactionType, currentUser, result)
    }

    @Test
    fun getReactionsSuccess() = runTest {
        // given
        val messageId = randomString()
        val offset = randomInt()
        val limit = positiveRandomInt()
        val reaction = randomReaction(messageId = messageId)
        val sut = Fixture()
            .givenGetReactionsResult(listOf(reaction).asCall())
            .get()
        // when
        val result = sut.getReactions(messageId, offset, limit).await()
        // then
        verifySuccess(result, listOf(reaction))
    }

    @Test
    fun getReactionsError() = runTest {
        // given
        val messageId = randomString()
        val offset = randomInt()
        val limit = positiveRandomInt()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenGetReactionsResult(RetroError<List<Reaction>>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.getReactions(messageId, offset, limit).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun queryReactionsSuccess() = runTest {
        // given
        val messageId = randomString()
        val filter = Filters.neutral()
        val limit = positiveRandomInt()
        val next = randomString()
        val sort = QuerySortByField<Reaction>()
        val reactions = listOf(randomReaction(messageId = messageId))
        val queryResult = QueryReactionsResult(reactions = reactions, next = randomString())
        val sut = Fixture()
            .givenQueryReactionsResult(queryResult.asCall())
            .get()
        // when
        val result = sut.queryReactions(messageId, filter, limit, next, sort).await()
        // then
        verifySuccess(result, queryResult)
    }

    @Test
    fun queryReactionsError() = runTest {
        // given
        val messageId = randomString()
        val filter = Filters.neutral()
        val limit = positiveRandomInt()
        val next = randomString()
        val sort = QuerySortByField<Reaction>()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenQueryReactionsResult(RetroError<QueryReactionsResult>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.queryReactions(messageId, filter, limit, next, sort).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun updateMessageSuccess() = runTest {
        // given
        val message = randomMessage()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenUpdateMessageResult(message.asCall())
            .get()
        // when
        val result = sut.updateMessage(message).await()
        // then
        verifySuccess(result, message)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onMessageEditRequest(message)
        inOrder.verify(plugin).onMessageEditResult(message, result)
    }

    @Test
    fun updateMessageError() = runTest {
        // given
        val message = randomMessage()
        val plugin = mock<Plugin>()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenUpdateMessageResult(RetroError<Message>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.updateMessage(message).await()
        // then
        verifyNetworkError(result, errorCode)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onMessageEditRequest(message)
        inOrder.verify(plugin).onMessageEditResult(message, result)
    }

    @Test
    fun partialUpdateMessageSuccess() = runTest {
        // given
        val messageId = randomString()
        val set = emptyMap<String, Any>()
        val unset = emptyList<String>()
        val message = randomMessage()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenPartialUpdateMessageResult(message.asCall())
            .get()
        // when
        val result = sut.partialUpdateMessage(messageId, set, unset).await()
        // then
        verifySuccess(result, message)
        verify(plugin).onMessageEditResult(message, result)
    }

    @Test
    fun partialUpdateMessageError() = runTest {
        // given
        val messageId = randomString()
        val set = emptyMap<String, Any>()
        val unset = emptyList<String>()
        val errorCode = positiveRandomInt()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenPartialUpdateMessageResult(RetroError<Message>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.partialUpdateMessage(messageId, set, unset).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(plugin, never()).onMessageEditResult(any(), any())
    }

    @Test
    fun editMessageWithoutNewAttachmentsDelegatesToUpdateMessage() = runTest {
        // given
        val message = randomMessage()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenUpdateMessageResult(message.asCall())
            .get()
        // when
        val result = sut.editMessage(randomString(), randomString(), message).await()
        // then
        verifySuccess(result, message)
        val inOrder = Mockito.inOrder(plugin)
        inOrder.verify(plugin).onMessageEditRequest(any())
        inOrder.verify(plugin).onMessageEditResult(any(), any())
    }

    @Test
    fun editMessageWithNewAttachmentsUploadsAndUpdates() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val localFile = File.createTempFile("test", ".jpg")
        val originalMessage = randomMessage()
        val messageWithAttachment = originalMessage.copy(
            attachments = mutableListOf(Attachment(upload = localFile)),
        )
        val uploadedMessage = messageWithAttachment.copy(
            attachments = mutableListOf(
                Attachment(assetUrl = "https://cdn.stream.io/uploaded.jpg"),
            ),
        )
        val serverMessage = uploadedMessage.copy()
        val plugin = mock<Plugin>()
        val channelLogic = mock<ChannelMessagesUpdateLogic>()
        val channelState = mock<ChannelState>()
        whenever(channelLogic.channelState()) doReturn channelState
        whenever(channelState.getMessageById(originalMessage.id)) doReturn originalMessage
        val sut = Fixture()
            .givenPlugin(plugin)
            .givenSendAttachmentsResult(Result.Success(uploadedMessage))
            .givenUpdateMessageResult(serverMessage.asCall())
            .get()
        val logicProvider = mock<ChannelStateLogicProvider>()
        whenever(logicProvider.channelStateLogic(channelType, channelId)) doReturn channelLogic
        sut.logicRegistry = logicProvider
        // when
        val result = sut.editMessage(channelType, channelId, messageWithAttachment).await()
        // then
        verifySuccess(result, serverMessage)
        verify(attachmentsSender).sendAttachments(any(), eq(channelType), eq(channelId), eq(false))
        verify(api).updateMessage(any())
    }

    @Test
    fun editMessageWithUploadFailureRevertsOriginal() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val localFile = File.createTempFile("test", ".jpg")
        val originalMessage = randomMessage()
        val messageWithAttachment = originalMessage.copy(
            attachments = mutableListOf(Attachment(upload = localFile)),
        )
        val channelLogic = mock<ChannelMessagesUpdateLogic>()
        val channelState = mock<ChannelState>()
        whenever(channelLogic.channelState()) doReturn channelState
        whenever(channelState.getMessageById(originalMessage.id)) doReturn originalMessage
        val uploadFailure = Result.Failure(Error.GenericError("Upload failed"))
        val sut = Fixture()
            .givenSendAttachmentsResult(uploadFailure)
            .get()
        val logicProvider = mock<ChannelStateLogicProvider>()
        whenever(logicProvider.channelStateLogic(channelType, channelId)) doReturn channelLogic
        sut.logicRegistry = logicProvider
        // when
        val result = sut.editMessage(channelType, channelId, messageWithAttachment).await()
        // then
        assert(result is Result.Failure)
        verify(api, never()).updateMessage(any())
        verify(channelLogic).upsertMessage(originalMessage)
    }

    @Test
    fun pinMessageWithExpirationDateSuccess() = runTest {
        // given
        val message = randomMessage()
        val expirationDate = randomDate()
        val sut = Fixture()
            .givenPartialUpdateMessageResult(message.asCall())
            .get()
        // when
        val result = sut.pinMessage(message, expirationDate).await()
        // then
        val expectedSet = mapOf(
            "pinned" to true,
            "pin_expires" to expirationDate,
        )
        verifySuccess(result, message)
        verify(api).partialUpdateMessage(message.id, expectedSet, emptyList())
    }

    @Test
    fun pinMessageWithExpirationDateError() = runTest {
        // given
        val message = randomMessage()
        val expirationDate = randomDate()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenPartialUpdateMessageResult(RetroError<Message>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.pinMessage(message, expirationDate).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun pinMessageWithTimeoutSuccess() = runTest {
        // given
        val message = randomMessage()
        val timeout = positiveRandomInt()
        val sut = Fixture()
            .givenPartialUpdateMessageResult(message.asCall())
            .get()
        // when
        val result = sut.pinMessage(message, timeout).await()
        // then
        verifySuccess(result, message)
    }

    @Test
    fun pinMessageWithTimeoutError() = runTest {
        // given
        val message = randomMessage()
        val timeout = positiveRandomInt()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenPartialUpdateMessageResult(RetroError<Message>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.pinMessage(message, timeout).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun unpinMessageSuccess() = runTest {
        // given
        val message = randomMessage()
        val sut = Fixture()
            .givenPartialUpdateMessageResult(message.asCall())
            .get()
        // when
        val result = sut.unpinMessage(message).await()
        // then
        val expectedSet = mapOf("pinned" to false)
        verifySuccess(result, message)
        verify(api).partialUpdateMessage(message.id, expectedSet, emptyList())
    }

    @Test
    fun unpinMessageError() = runTest {
        // given
        val message = randomMessage()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenPartialUpdateMessageResult(RetroError<Message>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.unpinMessage(message).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun getPinnedMessagesSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val limit = positiveRandomInt()
        val sort = QuerySortByField.ascByName<Message>("pinned_at")
        val pagination = PinnedMessagesPagination.BeforeDate(randomDate(), inclusive = randomBoolean())
        val pinnedMessages = listOf(randomMessage())
        val sut = Fixture()
            .givenGetPinnedMessagesResult(pinnedMessages.asCall())
            .get()
        // when
        val result = sut.getPinnedMessages(channelType, channelId, limit, sort, pagination).await()
        // then
        verifySuccess(result, pinnedMessages)
    }

    @Test
    fun getPinnedMessagesError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val limit = positiveRandomInt()
        val sort = QuerySortByField.ascByName<Message>("pinned_at")
        val pagination = PinnedMessagesPagination.BeforeDate(randomDate(), inclusive = randomBoolean())
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenGetPinnedMessagesResult(RetroError<List<Message>>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.getPinnedMessages(channelType, channelId, limit, sort, pagination).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun translateSuccess() = runTest {
        // given
        val messageId = randomString()
        val language = randomString()
        val message = randomMessage()
        val sut = Fixture()
            .givenTranslateResult(message.asCall())
            .get()
        // when
        val result = sut.translate(messageId, language).await()
        // then
        verifySuccess(result, message)
    }

    @Test
    fun translateError() = runTest {
        // given
        val messageId = randomString()
        val language = randomString()
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenTranslateResult(RetroError<Message>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.translate(messageId, language).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    internal inner class Fixture {

        fun givenGetMessageResult(result: Call<Message>) = apply {
            whenever(api.getMessage(any())).thenReturn(result)
        }

        fun givenGetPendingMessageResult(result: Call<PendingMessage>) = apply {
            whenever(api.getPendingMessage(any())).thenReturn(result)
        }

        fun givenDeleteMessageResult(result: Call<Message>) = apply {
            whenever(api.deleteMessage(messageId = any(), hard = any(), deleteForMe = any())).thenReturn(result)
        }

        fun givenSendMessageResult(result: Call<Message>) = apply {
            whenever(api.sendMessage(any(), any(), any())).thenReturn(result)
        }

        suspend fun givenSendAttachmentsResult(result: Result<Message>) = apply {
            whenever(attachmentsSender.sendAttachments(any(), any(), any(), any())).doReturn(result)
        }

        fun givenSendActionResult(result: Call<Message>) = apply {
            whenever(api.sendAction(any())).thenReturn(result)
        }

        fun givenGetRepliesResult(result: Call<List<Message>>) = apply {
            whenever(api.getReplies(any(), any())).thenReturn(result)
        }

        fun givenGetNewerRepliesResult(result: Call<List<Message>>) = apply {
            whenever(api.getNewerReplies(any(), any(), any())).thenReturn(result)
        }

        fun givenGetRepliesMoreResult(result: Call<List<Message>>) = apply {
            whenever(api.getRepliesMore(any(), any(), any())).thenReturn(result)
        }

        fun givenSendReactionResult(result: Call<Reaction>) = apply {
            whenever(api.sendReaction(any(), any(), any())).thenReturn(result)
        }

        fun givenDeleteReactionResult(result: Call<Message>) = apply {
            whenever(api.deleteReaction(any(), any())).thenReturn(result)
        }

        fun givenGetReactionsResult(result: Call<List<Reaction>>) = apply {
            whenever(api.getReactions(any(), any(), any())).thenReturn(result)
        }

        fun givenQueryReactionsResult(result: Call<QueryReactionsResult>) = apply {
            whenever(api.queryReactions(any(), any(), any(), any(), any())).thenReturn(result)
        }

        fun givenUpdateMessageResult(result: Call<Message>) = apply {
            whenever(api.updateMessage(any())).thenReturn(result)
        }

        fun givenPartialUpdateMessageResult(result: Call<Message>) = apply {
            whenever(api.partialUpdateMessage(any(), any(), any(), any())).thenReturn(result)
        }

        fun givenGetPinnedMessagesResult(result: Call<List<Message>>) = apply {
            whenever(api.getPinnedMessages(any(), any(), any(), any(), any())).thenReturn(result)
        }

        fun givenTranslateResult(result: Call<Message>) = apply {
            whenever(api.translate(any(), any())).thenReturn(result)
        }

        fun givenUser(user: User) = apply {
            whenever(userStateService.state) doReturn UserState.UserSet(user)
            whenever(mutableClientState.user) doReturn MutableStateFlow(user)
        }

        fun givenPlugin(plugin: Plugin) = apply {
            plugins.add(plugin)
        }

        fun get(): ChatClient = chatClient
    }
}
