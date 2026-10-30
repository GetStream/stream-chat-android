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

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QueryChannelsResult
import io.getstream.chat.android.client.chatclient.BaseChatClientTest
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyNetworkError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.QueryScheduledMessagesResult
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageQueryStatus
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomQueryScheduledMessagesResult
import io.getstream.chat.android.randomScheduledMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldContainSame
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

internal class ChatClientScheduledMessagesApiTests : BaseChatClientTest() {

    @Test
    fun queryScheduledMessagesSuccess() = runTest {
        // given
        val cid = randomCID()
        val statuses = setOf(ScheduledMessageQueryStatus.Scheduled, ScheduledMessageQueryStatus.Failed)
        val limit = positiveRandomInt(maxInt = 100)
        val next = randomString()
        val queryResult = randomQueryScheduledMessagesResult()
        whenever(api.queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull()))
            .thenReturn(RetroSuccess(queryResult).toRetrofitCall())
        // when
        val result = chatClient.queryScheduledMessages(cid, statuses, limit, next).await()
        // then
        verifySuccess(result, queryResult)
        verify(api).queryScheduledMessages(cid, statuses, limit, next)
    }

    @Test
    fun queryScheduledMessagesDefaults() = runTest {
        // given
        val queryResult = randomQueryScheduledMessagesResult()
        whenever(api.queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull()))
            .thenReturn(RetroSuccess(queryResult).toRetrofitCall())
        // when
        val result = chatClient.queryScheduledMessages().await()
        // then
        verifySuccess(result, queryResult)
        verify(api).queryScheduledMessages(cid = null, statuses = emptySet(), limit = 100, next = null)
    }

    @Test
    fun queryScheduledMessagesError() = runTest {
        // given
        val cid = randomCID()
        val errorCode = positiveRandomInt()
        whenever(api.queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull()))
            .thenReturn(RetroError<QueryScheduledMessagesResult>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.queryScheduledMessages(cid = cid).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(api).queryScheduledMessages(cid, emptySet(), 100, null)
    }

    @Test
    fun queryScheduledMessagesSavesTheResults() = runTest {
        // given
        val queryResult = randomQueryScheduledMessagesResult()
        whenever(api.queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull()))
            .thenReturn(RetroSuccess(queryResult).toRetrofitCall())
        // when
        chatClient.queryScheduledMessages().await()
        // then
        chatClient.scheduledMessages.value.map(ScheduledMessage::id) shouldContainSame
            queryResult.scheduledMessages.map(ScheduledMessage::id)
    }

    @Test
    fun channelQueriesSaveTheScheduledMessagesOnlyWhenIncluded() = runTest {
        // given
        val scheduledMessage = randomScheduledMessage()
        val channel = randomChannel().copy(scheduledMessages = listOf(scheduledMessage))
        whenever(api.queryChannels(any()))
            .thenReturn(RetroSuccess(QueryChannelsResult(listOf(channel), predefinedFilter = null)).toRetrofitCall())
        // when
        chatClient.queryChannelsInternal(QueryChannelsRequest(Filters.neutral(), limit = 1)).await()
        val notIncluded = chatClient.scheduledMessages.value
        chatClient.queryChannelsInternal(
            QueryChannelsRequest(Filters.neutral(), limit = 1).apply { includeScheduledMessages = true },
        ).await()
        // then
        notIncluded.shouldBeEmpty()
        chatClient.scheduledMessages.value shouldBeEqualTo listOf(scheduledMessage)
    }

    @Test
    fun channelQueriesRemoveTheScheduledMessagesTheServerNoLongerHasOnlyWhenItSendsThem() = runTest {
        // given
        val channel = randomChannel()
        val scheduledMessage = randomScheduledMessage(cid = channel.cid)
        val request = QueryChannelsRequest(Filters.neutral(), limit = 1).apply { includeScheduledMessages = true }
        val queryWith = { scheduledMessages: List<ScheduledMessage>? ->
            val result = QueryChannelsResult(listOf(channel.copy(scheduledMessages = scheduledMessages)), null)
            whenever(api.queryChannels(any())).thenReturn(RetroSuccess(result).toRetrofitCall())
            chatClient.queryChannelsInternal(request)
        }
        queryWith(listOf(scheduledMessage)).await()
        // when
        queryWith(null).await()
        val notSent = chatClient.scheduledMessages.value
        queryWith(emptyList()).await()
        // then
        notSent shouldBeEqualTo listOf(scheduledMessage)
        chatClient.scheduledMessages.value.shouldBeEmpty()
    }

    @Test
    fun channelQueriesRemoveOnlyFromTheChannelsReturnedWithTheirScheduledMessages() = runTest {
        // given
        val withField = randomChannel()
        val withoutField = randomChannel()
        val inWithField = randomScheduledMessage(cid = withField.cid)
        val inWithoutField = randomScheduledMessage(cid = withoutField.cid)
        val request = QueryChannelsRequest(Filters.neutral(), limit = 2).apply { includeScheduledMessages = true }
        val queryWith = { channels: List<Channel> ->
            whenever(api.queryChannels(any()))
                .thenReturn(RetroSuccess(QueryChannelsResult(channels, predefinedFilter = null)).toRetrofitCall())
            chatClient.queryChannelsInternal(request)
        }
        queryWith(
            listOf(
                withField.copy(scheduledMessages = listOf(inWithField)),
                withoutField.copy(scheduledMessages = listOf(inWithoutField)),
            ),
        ).await()
        // when
        queryWith(
            listOf(withField.copy(scheduledMessages = emptyList()), withoutField.copy(scheduledMessages = null)),
        ).await()
        // then
        chatClient.scheduledMessages.value shouldBeEqualTo listOf(inWithoutField)
    }

    @Test
    fun scheduledMessagesOfAChannelDoNotEmitForChangesElsewhere() = runTest {
        // given
        val cid = randomCID()
        val (channelType, channelId) = randomCID().cidToTypeAndId()
        whenever(mutableClientState.user).thenReturn(MutableStateFlow(randomUser()))
        val emissions = mutableListOf<List<ScheduledMessage>>()
        backgroundScope.launch { chatClient.scheduledMessages(cid).toList(emissions) }
        runCurrent()
        // when
        chatClient.scheduleMessage(channelType, channelId, Message(text = randomString()), randomDate()).await()
        runCurrent()
        // then
        emissions shouldBeEqualTo listOf(emptyList())
    }

    @Test
    fun scheduleMessageSavesItLocallyFirst() = runTest {
        // given
        val cid = randomCID()
        val (channelType, channelId) = cid.cidToTypeAndId()
        val parentId = randomString()
        val scheduledAt = randomDate()
        val message = Message(text = randomString(), parentId = parentId)
        whenever(mutableClientState.user).thenReturn(MutableStateFlow(randomUser()))
        // when
        val result = chatClient.scheduleMessage(channelType, channelId, message, scheduledAt).await()
        // then
        val scheduledMessage = (result as Result.Success).value
        scheduledMessage.status shouldBeEqualTo ScheduledMessageStatus.PreparingToSchedule
        scheduledMessage.scheduledAt shouldBeEqualTo scheduledAt
        scheduledMessage.message.text shouldBeEqualTo message.text
        chatClient.scheduledMessages.value shouldBeEqualTo listOf(scheduledMessage)
        chatClient.scheduledMessages(cid, parentId).first() shouldBeEqualTo listOf(scheduledMessage)
        chatClient.scheduledMessages(cid).first().shouldBeEmpty()
        verifyNoInteractions(api)
    }

    @Test
    fun changingAnUnknownScheduledMessageFailsWithNoRequest() = runTest {
        // given
        val id = randomString()
        // when
        val results = listOf(
            chatClient.updateScheduledMessage(id, randomMessage()).await(),
            chatClient.rescheduleMessage(id, randomDate()).await(),
            chatClient.sendScheduledMessageNow(id).await(),
            chatClient.resendScheduledMessage(id).await(),
        )
        // then
        results.forEach { it.shouldBeInstanceOf<Result.Failure>() }
        verifyNoInteractions(api)
    }

    @Test
    fun deleteScheduledMessageSuccess() = runTest {
        // given
        val id = randomString()
        whenever(api.deleteScheduledMessage(any()))
            .thenReturn(RetroSuccess(Unit).toRetrofitCall())
        // when
        val result = chatClient.deleteScheduledMessage(id).await()
        // then
        verifySuccess(result, Unit)
        verify(api).deleteScheduledMessage(id)
    }

    @Test
    fun deleteScheduledMessageError() = runTest {
        // given
        val id = randomString()
        val errorCode = positiveRandomInt()
        whenever(api.deleteScheduledMessage(any()))
            .thenReturn(RetroError<Unit>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.deleteScheduledMessage(id).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(api).deleteScheduledMessage(id)
    }
}
