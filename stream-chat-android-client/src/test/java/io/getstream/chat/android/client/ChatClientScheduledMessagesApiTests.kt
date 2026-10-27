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

import io.getstream.chat.android.client.chatclient.BaseChatClientTest
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyNetworkError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.models.QueryScheduledMessagesResult
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomQueryScheduledMessagesResult
import io.getstream.chat.android.randomScheduledMessage
import io.getstream.chat.android.randomString
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ChatClientScheduledMessagesApiTests : BaseChatClientTest() {

    @Test
    fun queryScheduledMessagesSuccess() = runTest {
        // given
        val cid = randomCID()
        val statuses = setOf(ScheduledMessageStatus.Scheduled, ScheduledMessageStatus.Failed)
        val limit = positiveRandomInt()
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
    fun updateScheduledMessageSuccess() = runTest {
        // given
        val id = randomString()
        val message = randomMessage()
        val scheduledAt = randomDate()
        val scheduledMessage = randomScheduledMessage(id = id)
        whenever(api.updateScheduledMessage(any(), anyOrNull(), anyOrNull()))
            .thenReturn(RetroSuccess(scheduledMessage).toRetrofitCall())
        // when
        val result = chatClient.updateScheduledMessage(id, message, scheduledAt).await()
        // then
        verifySuccess(result, scheduledMessage)
        verify(api).updateScheduledMessage(id, message, scheduledAt)
    }

    @Test
    fun updateScheduledMessageError() = runTest {
        // given
        val id = randomString()
        val scheduledAt = randomDate()
        val errorCode = positiveRandomInt()
        whenever(api.updateScheduledMessage(any(), anyOrNull(), anyOrNull()))
            .thenReturn(RetroError<ScheduledMessage>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.updateScheduledMessage(id, scheduledAt = scheduledAt).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(api).updateScheduledMessage(id, null, scheduledAt)
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

    @Test
    fun sendScheduledMessageNowSuccess() = runTest {
        // given
        val id = randomString()
        val scheduledMessage = randomScheduledMessage(id = id, status = ScheduledMessageStatus.Sending)
        whenever(api.sendScheduledMessageNow(any()))
            .thenReturn(RetroSuccess(scheduledMessage).toRetrofitCall())
        // when
        val result = chatClient.sendScheduledMessageNow(id).await()
        // then
        verifySuccess(result, scheduledMessage)
        verify(api).sendScheduledMessageNow(id)
    }

    @Test
    fun sendScheduledMessageNowError() = runTest {
        // given
        val id = randomString()
        val errorCode = positiveRandomInt()
        whenever(api.sendScheduledMessageNow(any()))
            .thenReturn(RetroError<ScheduledMessage>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.sendScheduledMessageNow(id).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(api).sendScheduledMessageNow(id)
    }
}
