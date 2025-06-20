/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.MessageReminder
import io.getstream.chat.android.models.QueryRemindersResult
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMessageReminder
import io.getstream.chat.android.randomQueryMessageRemindersResult
import io.getstream.chat.android.randomString
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ChatClientRemindersApiTests : BaseChatClientTest() {

    @Test
    fun createReminderSuccess() = runTest {
        // given
        val messageId = randomString()
        val remindAt = randomDate()
        val messageReminder = randomMessageReminder()
        whenever(api.createReminder(any(), any()))
            .thenReturn(RetroSuccess(messageReminder).toRetrofitCall())
        // when
        val result = chatClient.createReminder(messageId, remindAt).await()
        // then
        verifySuccess(result, messageReminder)
        verify(api).createReminder(messageId, remindAt)
    }

    @Test
    fun createReminderError() = runTest {
        // given
        val messageId = randomString()
        val remindAt = randomDate()
        val errorCode = positiveRandomInt()
        whenever(api.createReminder(any(), any()))
            .thenReturn(RetroError<MessageReminder>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.createReminder(messageId, remindAt).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(api).createReminder(messageId, remindAt)
    }

    @Test
    fun updateReminderSuccess() = runTest {
        // given
        val messageId = randomString()
        val remindAt = randomDate()
        val messageReminder = randomMessageReminder()
        whenever(api.updateReminder(any(), any()))
            .thenReturn(RetroSuccess(messageReminder).toRetrofitCall())
        // when
        val result = chatClient.updateReminder(messageId, remindAt).await()
        // then
        verifySuccess(result, messageReminder)
        verify(api).updateReminder(messageId, remindAt)
    }

    @Test
    fun updateReminderError() = runTest {
        // given
        val messageId = randomString()
        val remindAt = randomDate()
        val errorCode = positiveRandomInt()
        whenever(api.updateReminder(any(), any()))
            .thenReturn(RetroError<MessageReminder>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.updateReminder(messageId, remindAt).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(api).updateReminder(messageId, remindAt)
    }

    @Test
    fun deleteReminderSuccess() = runTest {
        // given
        val messageId = randomString()
        whenever(api.deleteReminder(any()))
            .thenReturn(RetroSuccess(Unit).toRetrofitCall())
        // when
        val result = chatClient.deleteReminder(messageId).await()
        // then
        verifySuccess(result, Unit)
        verify(api).deleteReminder(messageId)
    }

    @Test
    fun deleteReminderError() = runTest {
        // given
        val messageId = randomString()
        val errorCode = positiveRandomInt()
        whenever(api.deleteReminder(any()))
            .thenReturn(RetroError<Unit>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.deleteReminder(messageId).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(api).deleteReminder(messageId)
    }

    @Test
    fun queryRemindersSuccess() = runTest {
        // given
        val filter = Filters.neutral()
        val limit = positiveRandomInt()
        val next = randomString()
        val sort = QuerySortByField<MessageReminder>()
        val reminderResult = randomQueryMessageRemindersResult()
        whenever(api.queryReminders(any(), any(), any(), any()))
            .thenReturn(RetroSuccess(reminderResult).toRetrofitCall())
        // when
        val result = chatClient.queryReminders(filter, limit, next, sort).await()
        // then
        verifySuccess(result, reminderResult)
        verify(api).queryReminders(filter, limit, next, sort)
    }

    @Test
    fun queryRemindersError() = runTest {
        // given
        val filter = Filters.neutral()
        val limit = positiveRandomInt()
        val next = randomString()
        val sort = QuerySortByField<MessageReminder>()
        val errorCode = positiveRandomInt()
        whenever(api.queryReminders(any(), any(), any(), any()))
            .thenReturn(RetroError<QueryRemindersResult>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.queryReminders(filter, limit, next, sort).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(api).queryReminders(filter, limit, next, sort)
    }
}
