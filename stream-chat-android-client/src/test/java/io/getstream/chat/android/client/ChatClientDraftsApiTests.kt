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
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.QueryDraftsResult
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomDraftMessage
import io.getstream.chat.android.randomQueryDraftsResult
import io.getstream.chat.android.randomString
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

/**
 * Test class for the drafts functionality of the [ChatClient].
 */
internal class ChatClientDraftsApiTests : BaseChatClientTest() {

    @Test
    fun createDraftSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val draft = randomDraftMessage()
        whenever(api.createDraftMessage(any(), any(), any()))
            .doReturn(RetroSuccess(draft).toRetrofitCall())
        // when
        val result = chatClient.createDraftMessage(channelType, channelId, draft).await()
        // then
        verifySuccess(result, draft)
    }

    @Test
    fun createDraftError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val draft = randomDraftMessage()
        val errorCode = positiveRandomInt()
        whenever(api.createDraftMessage(any(), any(), any()))
            .doReturn(RetroError<DraftMessage>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.createDraftMessage(channelType, channelId, draft).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun deleteDraftSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val draft = randomDraftMessage()
        whenever(api.deleteDraftMessage(any(), any(), any()))
            .doReturn(RetroSuccess(Unit).toRetrofitCall())
        // when
        val result = chatClient.deleteDraftMessages(channelType, channelId, draft).await()
        // then
        verifySuccess(result, Unit)
    }

    @Test
    fun deleteDraftError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val draft = randomDraftMessage()
        val errorCode = positiveRandomInt()
        whenever(api.deleteDraftMessage(any(), any(), any()))
            .doReturn(RetroError<Unit>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.deleteDraftMessages(channelType, channelId, draft).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun queryDraftMessagesSuccess() = runTest {
        // given
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val drafts = listOf(randomDraftMessage())
        whenever(api.queryDraftMessages(any(), any()))
            .doReturn(RetroSuccess(drafts).toRetrofitCall())
        // when
        val result = chatClient.queryDraftMessages(offset, limit).await()
        // then
        verifySuccess(result, drafts)
    }

    @Test
    fun queryDraftMessagesError() = runTest {
        // given
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val errorCode = positiveRandomInt()
        whenever(api.queryDraftMessages(any(), any()))
            .doReturn(RetroError<List<DraftMessage>>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.queryDraftMessages(offset, limit).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun queryDraftsSuccess() = runTest {
        // given
        val filter = Filters.neutral()
        val limit = positiveRandomInt()
        val queryDraftsResult = randomQueryDraftsResult()
        whenever(api.queryDrafts(any(), anyOrNull(), anyOrNull(), any()))
            .doReturn(RetroSuccess(queryDraftsResult).toRetrofitCall())
        // when
        val result = chatClient.queryDrafts(filter, limit).await()
        // then
        verifySuccess(result, queryDraftsResult)
    }

    @Test
    fun queryDraftsError() = runTest {
        // given
        val filter = Filters.neutral()
        val limit = positiveRandomInt()
        val errorCode = positiveRandomInt()
        whenever(api.queryDrafts(any(), anyOrNull(), anyOrNull(), any()))
            .doReturn(RetroError<QueryDraftsResult>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.queryDrafts(filter, limit).await()
        // then
        verifyNetworkError(result, errorCode)
    }
}
