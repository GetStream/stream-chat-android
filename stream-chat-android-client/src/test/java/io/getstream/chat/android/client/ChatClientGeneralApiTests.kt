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
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.extensions.ATTACHMENT_TYPE_FILE
import io.getstream.chat.android.client.extensions.ATTACHMENT_TYPE_IMAGE
import io.getstream.chat.android.client.parser.EventArguments
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyGenericError
import io.getstream.chat.android.client.utils.verifyNetworkError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SearchMessagesResult
import io.getstream.chat.android.models.UnreadCounts
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUnreadCounts
import io.getstream.result.call.Call
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date
import kotlin.time.Duration.Companion.days

/**
 * Tests covering the general API functionality of the [ChatClient].
 */
internal class ChatClientGeneralApiTests : BaseChatClientTest() {

    @Test
    fun getSyncHistorySuccess() = runTest {
        // given
        val channelIds = listOf(randomCID())
        val lastSyncAt = Date().apply {
            time -= 29.days.inWholeMilliseconds
        }
        val events = listOf(EventArguments.randomEvent())
        val sut = Fixture()
            .givenGetSyncHistoryResult(RetroSuccess(events).toRetrofitCall())
            .get()
        // when
        val result = sut.getSyncHistory(channelIds, lastSyncAt).await()
        // then
        verifySuccess(result, events)
    }

    @Test
    fun getSyncHistoryWithoutChannels() = runTest {
        // given
        val channelIds = emptyList<String>()
        val lastSyncAt = Date().apply {
            time -= 29.days.inWholeMilliseconds
        }
        val events = listOf(EventArguments.randomEvent())
        val sut = Fixture()
            .givenGetSyncHistoryResult(RetroSuccess(events).toRetrofitCall())
            .get()
        // when
        val result = sut.getSyncHistory(channelIds, lastSyncAt).await()
        // then
        verifyGenericError(result, "channelsIds must contain at least 1 id.")
    }

    @Test
    fun getSyncHistoryLastSyncTooOld() = runTest {
        // given
        val channelIds = listOf(randomCID())
        val lastSyncAt = Date().apply {
            time -= 31.days.inWholeMilliseconds
        }
        val events = listOf(EventArguments.randomEvent())
        val sut = Fixture()
            .givenGetSyncHistoryResult(RetroSuccess(events).toRetrofitCall())
            .get()
        // when
        val result = sut.getSyncHistory(channelIds, lastSyncAt).await()
        // then
        verifyGenericError(result, "lastSyncAt cannot by later than 30 days.")
    }

    @Test
    fun getSyncHistoryError() = runTest {
        // given
        val channelIds = listOf(randomCID())
        val lastSyncAt = Date().apply {
            time -= 29.days.inWholeMilliseconds
        }
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenGetSyncHistoryResult(RetroError<List<ChatEvent>>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.getSyncHistory(channelIds, lastSyncAt).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun getSyncHistoryWithExactDateSuccess() = runTest {
        // given
        val channelIds = listOf(randomCID())
        val lastSyncAt = Date()
            .apply { time -= 29.days.inWholeMilliseconds }
            .run { StreamDateFormatter().format(this) }
        val events = listOf(EventArguments.randomEvent())
        val sut = Fixture()
            .givenGetSyncHistoryResult(RetroSuccess(events).toRetrofitCall())
            .get()
        // when
        val result = sut.getSyncHistory(channelIds, lastSyncAt).await()
        // then
        verifySuccess(result, events)
    }

    @Test
    fun getSyncHistoryWithExactDateWithoutChannels() = runTest {
        // given
        val channelIds = emptyList<String>()
        val lastSyncAt = Date()
            .apply { time -= 29.days.inWholeMilliseconds }
            .run { StreamDateFormatter().format(this) }
        val events = listOf(EventArguments.randomEvent())
        val sut = Fixture()
            .givenGetSyncHistoryResult(RetroSuccess(events).toRetrofitCall())
            .get()
        // when
        val result = sut.getSyncHistory(channelIds, lastSyncAt).await()
        // then
        verifyGenericError(result, "channelsIds must contain at least 1 id.")
    }

    @Test
    fun getSyncHistoryWithExactDateLastSyncTooOld() = runTest {
        // given
        val channelIds = listOf(randomCID())
        val lastSyncAt = Date()
            .apply { time -= 31.days.inWholeMilliseconds }
            .run { StreamDateFormatter().format(this) }
        val events = listOf(EventArguments.randomEvent())
        val sut = Fixture()
            .givenGetSyncHistoryResult(RetroSuccess(events).toRetrofitCall())
            .get()
        // when
        val result = sut.getSyncHistory(channelIds, lastSyncAt).await()
        // then
        verifyGenericError(result, "lastSyncAt cannot by later than 30 days.")
    }

    @Test
    fun getSyncHistoryWithExactDateInvalidLastSyncAt() = runTest {
        // given
        val channelIds = listOf(randomCID())
        val lastSyncAt = "invalid date"
        val events = listOf(EventArguments.randomEvent())
        val sut = Fixture()
            .givenGetSyncHistoryResult(RetroSuccess(events).toRetrofitCall())
            .get()
        // when
        val result = sut.getSyncHistory(channelIds, lastSyncAt).await()
        // then
        verifyGenericError(
            result,
            "The string for data: $lastSyncAt could not be parsed for format: ${StreamDateFormatter().datePattern}",
        )
    }

    @Test
    fun getSyncHistoryWithExactDateError() = runTest {
        // given
        val channelIds = listOf(randomCID())
        val lastSyncAt = Date()
            .apply { time -= 29.days.inWholeMilliseconds }
            .run { StreamDateFormatter().format(this) }
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenGetSyncHistoryResult(RetroError<List<ChatEvent>>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.getSyncHistory(channelIds, lastSyncAt).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun searchMessagesSuccess() = runTest {
        // given
        val channelFilter = Filters.neutral()
        val messageFilter = Filters.neutral()
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val next = null
        val sort = null
        val response = SearchMessagesResult()
        val sut = Fixture()
            .givenSearchMessagesResult(RetroSuccess(response).toRetrofitCall())
            .get()
        // when
        val result = sut.searchMessages(channelFilter, messageFilter, offset, limit, next, sort).await()
        // then
        verifySuccess(result, response)
    }

    @Test
    fun searchMessagesWithoutOffsetWithSortAndNextSuccess() = runTest {
        // given
        val channelFilter = Filters.neutral()
        val messageFilter = Filters.neutral()
        val offset = null
        val limit = positiveRandomInt()
        val next = "next"
        val sort = QuerySortByField<Message>().asc("createdAt")
        val response = SearchMessagesResult()
        val sut = Fixture()
            .givenSearchMessagesResult(RetroSuccess(response).toRetrofitCall())
            .get()
        // when
        val result = sut.searchMessages(channelFilter, messageFilter, offset, limit, next, sort).await()
        // then
        verifySuccess(result, response)
    }

    @Test
    fun searchMessagesWithOffsetAndSort() = runTest {
        // given
        val channelFilter = Filters.neutral()
        val messageFilter = Filters.neutral()
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val next = null
        val sort = QuerySortByField<Message>().asc("createdAt")
        val response = SearchMessagesResult()
        val sut = Fixture()
            .givenSearchMessagesResult(RetroSuccess(response).toRetrofitCall())
            .get()
        // when
        val result = sut.searchMessages(channelFilter, messageFilter, offset, limit, next, sort).await()
        // then
        verifyGenericError(result, "Cannot specify offset with sort or next parameters")
    }

    @Test
    fun searchMessagesWithOffsetAndNext() = runTest {
        // given
        val channelFilter = Filters.neutral()
        val messageFilter = Filters.neutral()
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val next = "next"
        val sort = null
        val response = SearchMessagesResult()
        val sut = Fixture()
            .givenSearchMessagesResult(RetroSuccess(response).toRetrofitCall())
            .get()
        // when
        val result = sut.searchMessages(channelFilter, messageFilter, offset, limit, next, sort).await()
        // then
        verifyGenericError(result, "Cannot specify offset with sort or next parameters")
    }

    @Test
    fun searchMessagesError() = runTest {
        // given
        val channelFilter = Filters.neutral()
        val messageFilter = Filters.neutral()
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val next = null
        val sort = null
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenSearchMessagesResult(RetroError<SearchMessagesResult>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.searchMessages(channelFilter, messageFilter, offset, limit, next, sort).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun getFileAttachmentsSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val attachmentType = ATTACHMENT_TYPE_FILE
        val attachments = listOf(randomAttachment(type = attachmentType))
        val messages = listOf(randomMessage(attachments = attachments))
        val sut = spy(
            Fixture()
                .givenSearchMessagesResult(RetroSuccess(SearchMessagesResult(messages)).toRetrofitCall())
                .get(),
        )
        // when
        val result = sut.getFileAttachments(channelType, channelId, offset, limit).await()
        // then
        verifySuccess(result, attachments)
        verify(sut).getMessagesWithAttachments(channelType, channelId, offset, limit, listOf(attachmentType))
    }

    @Test
    fun getFileAttachmentsError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val attachmentType = ATTACHMENT_TYPE_FILE
        val errorCode = positiveRandomInt()
        val sut = spy(
            Fixture()
                .givenSearchMessagesResult(RetroError<SearchMessagesResult>(errorCode).toRetrofitCall())
                .get(),
        )
        // when
        val result = sut.getFileAttachments(channelType, channelId, offset, limit).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(sut).getMessagesWithAttachments(channelType, channelId, offset, limit, listOf(attachmentType))
    }

    @Test
    fun getImageAttachmentsSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val attachmentType = ATTACHMENT_TYPE_IMAGE
        val attachments = listOf(randomAttachment(type = attachmentType))
        val messages = listOf(randomMessage(attachments = attachments))
        val sut = spy(
            Fixture()
                .givenSearchMessagesResult(RetroSuccess(SearchMessagesResult(messages)).toRetrofitCall())
                .get(),
        )
        // when
        val result = sut.getImageAttachments(channelType, channelId, offset, limit).await()
        // then
        verifySuccess(result, attachments)
        verify(sut).getMessagesWithAttachments(channelType, channelId, offset, limit, listOf(attachmentType))
    }

    @Test
    fun getImageAttachmentsError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val attachmentType = ATTACHMENT_TYPE_IMAGE
        val errorCode = positiveRandomInt()
        val sut = spy(
            Fixture()
                .givenSearchMessagesResult(RetroError<SearchMessagesResult>(errorCode).toRetrofitCall())
                .get(),
        )
        // when
        val result = sut.getImageAttachments(channelType, channelId, offset, limit).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(sut).getMessagesWithAttachments(channelType, channelId, offset, limit, listOf(attachmentType))
    }

    @Test
    fun getMessagesWithAttachmentsSuccess() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val attachmentTypes = listOf(ATTACHMENT_TYPE_IMAGE, ATTACHMENT_TYPE_FILE)
        val attachments = listOf(
            randomAttachment(type = ATTACHMENT_TYPE_IMAGE),
            randomAttachment(type = ATTACHMENT_TYPE_FILE),
        )
        val messages = listOf(randomMessage(attachments = attachments))
        val sut = spy(
            Fixture()
                .givenSearchMessagesResult(RetroSuccess(SearchMessagesResult(messages)).toRetrofitCall())
                .get(),
        )
        // when
        val result =
            sut.getMessagesWithAttachments(channelType, channelId, offset, limit, attachmentTypes).await()
        // then
        val expectedChannelFilter = Filters.`in`("cid", "$channelType:$channelId")
        val expectedMessageFilter = Filters.`in`("attachments.type", attachmentTypes)
        verifySuccess(result, messages)
        verify(sut).searchMessages(expectedChannelFilter, expectedMessageFilter, offset, limit, null, null)
    }

    @Test
    fun getMessagesWithAttachmentsError() = runTest {
        // given
        val channelType = randomString()
        val channelId = randomString()
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val attachmentTypes = listOf(ATTACHMENT_TYPE_IMAGE, ATTACHMENT_TYPE_FILE)
        val errorCode = positiveRandomInt()
        val sut = spy(
            Fixture()
                .givenSearchMessagesResult(RetroError<SearchMessagesResult>(errorCode).toRetrofitCall())
                .get(),
        )
        // when
        val result =
            sut.getMessagesWithAttachments(channelType, channelId, offset, limit, attachmentTypes).await()
        // then
        val expectedChannelFilter = Filters.`in`("cid", "$channelType:$channelId")
        val expectedMessageFilter = Filters.`in`("attachments.type", attachmentTypes)
        verifyNetworkError(result, errorCode)
        verify(sut).searchMessages(expectedChannelFilter, expectedMessageFilter, offset, limit, null, null)
    }

    @Test
    fun queryMembersSuccess() = runTest {
        // given
        val channelType = randomCID()
        val channelId = randomCID()
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val filter = Filters.neutral()
        val sort = QuerySortByField<Member>().asc("createdAt")
        val members = listOf(randomMember())
        val response = listOf(randomMember())
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenQueryMembersResult(RetroSuccess(response).toRetrofitCall())
            .givenPlugin(plugin)
            .get()
        // when
        val result = sut.queryMembers(channelType, channelId, offset, limit, filter, sort, members).await()
        // then
        verifySuccess(result, response)
        verify(plugin).onQueryMembersResult(result, channelType, channelId, offset, limit, filter, sort, members)
    }

    @Test
    fun queryMembersError() = runTest {
        // given
        val channelType = randomCID()
        val channelId = randomCID()
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val filter = Filters.neutral()
        val sort = QuerySortByField<Member>().asc("createdAt")
        val members = listOf(randomMember())
        val errorCode = positiveRandomInt()
        val plugin = mock<Plugin>()
        val sut = Fixture()
            .givenQueryMembersResult(RetroError<List<Member>>(errorCode).toRetrofitCall())
            .givenPlugin(plugin)
            .get()
        // when
        val result = sut.queryMembers(channelType, channelId, offset, limit, filter, sort, members).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(plugin).onQueryMembersResult(result, channelType, channelId, offset, limit, filter, sort, members)
    }

    @Test
    fun getUnreadCountsSuccess() = runTest {
        val unreadCounts = randomUnreadCounts()
        val sut = Fixture()
            .givenGetUnreadCountsResult(RetroSuccess(unreadCounts).toRetrofitCall())
            .get()

        val result = sut.getUnreadCounts().await()

        verifySuccess(result, unreadCounts)
    }

    @Test
    fun getUnreadCountsError() = runTest {
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenGetUnreadCountsResult(RetroError<UnreadCounts>(errorCode).toRetrofitCall())
            .get()

        val result = sut.getUnreadCounts().await()

        verifyNetworkError(result, errorCode)
    }

    internal inner class Fixture {

        fun givenGetSyncHistoryResult(result: Call<List<ChatEvent>>) = apply {
            whenever(api.getSyncHistory(any(), any())).thenReturn(result)
        }

        fun givenSearchMessagesResult(result: Call<SearchMessagesResult>) = apply {
            whenever(api.searchMessages(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()))
                .thenReturn(result)
        }

        fun givenQueryMembersResult(result: Call<List<Member>>) = apply {
            whenever(api.queryMembers(any(), any(), any(), any(), any(), any(), any())).thenReturn(result)
        }

        fun givenGetUnreadCountsResult(result: Call<UnreadCounts>) = apply {
            whenever(api.getUnreadCounts()).thenReturn(result)
        }

        fun givenPlugin(plugin: Plugin) = apply {
            plugins.add(plugin)
        }

        fun get(): ChatClient = chatClient
    }
}
