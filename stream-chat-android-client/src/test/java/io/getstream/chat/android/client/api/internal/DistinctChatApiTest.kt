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

package io.getstream.chat.android.client.api.internal

import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.models.BannedUsersSort
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCall
import io.getstream.result.Result
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class DistinctChatApiTest {

    @Test
    fun `When calling queryChannel with same arguments, Then same instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        val channelType = randomString()
        val channelId = randomString()
        val request = Mother.randomQueryChannelRequest()
        // when
        val call1 = distinctChatApi.queryChannel(channelType, channelId, request)
        val call2 = distinctChatApi.queryChannel(channelType, channelId, request)
        // then
        // verify same instance of call is reused
        Assert.assertTrue(call1 === call2)
    }

    @Test
    fun `When calling queryChannel with same argument and first call finishes, Then different instance of Call is returned`() =
        runTest {
            // given
            val delegateApi = mock<ChatApi>()
            whenever(delegateApi.queryChannel(any(), any(), any())).thenReturn(mock())
            val distinctChatApi = DistinctChatApi(backgroundScope, delegateApi)
            val channelType = randomString()
            val channelId = randomString()
            val request = Mother.randomQueryChannelRequest()
            // when
            val call1 = distinctChatApi.queryChannel(channelType, channelId, request)
            // Complete first call
            call1.await()
            val call2 = distinctChatApi.queryChannel(channelType, channelId, request)
            // then
            // verify different instance of call is returned
            Assert.assertFalse(call1 === call2)
        }

    @Test
    fun `When calling queryChannel with different arguments, Then different instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        val channelType = randomString()
        val channelId = randomString()
        val request1 = Mother.randomQueryChannelRequest()
        val request2 = Mother.randomQueryChannelRequest()
        // when
        val call1 = distinctChatApi.queryChannel(channelType, channelId, request1)
        val call2 = distinctChatApi.queryChannel(channelType, channelId, request2)
        // then
        // verify different instance of call is returned
        Assert.assertFalse(call1 === call2)
    }

    @Test
    fun `When calling getRepliesMore with same arguments, Then same instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        val messageId = randomString()
        val firstId = randomString()
        val limit = randomInt()
        // when
        val call1 = distinctChatApi.getRepliesMore(messageId, firstId, limit)
        val call2 = distinctChatApi.getRepliesMore(messageId, firstId, limit)
        // then
        // verify same instance of call is reused
        Assert.assertTrue(call1 === call2)
    }

    @Test
    fun `When calling getRepliesMore with same argument and first call finishes, Then different instance of Call is returned`() =
        runTest {
            // given
            val delegateApi = mock<ChatApi>()
            whenever(delegateApi.getRepliesMore(any(), any(), any())).thenReturn(mock())
            val distinctChatApi = DistinctChatApi(backgroundScope, delegateApi)
            val messageId = randomString()
            val firstId = randomString()
            val limit = randomInt()
            // when
            val call1 = distinctChatApi.getRepliesMore(messageId, firstId, limit)
            // Complete first call
            call1.await()
            val call2 = distinctChatApi.getRepliesMore(messageId, firstId, limit)
            // then
            // verify different instance of call is returned
            Assert.assertFalse(call1 === call2)
        }

    @Test
    fun `When calling getRepliesMore with different arguments, Then different instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        val messageId = randomString()
        val firstId1 = randomString()
        val firstId2 = randomString()
        val limit = randomInt()
        // when
        val call1 = distinctChatApi.getRepliesMore(messageId, firstId1, limit)
        val call2 = distinctChatApi.getRepliesMore(messageId, firstId2, limit)
        // then
        // verify different instance of call is returned
        Assert.assertFalse(call1 === call2)
    }

    @Test
    fun `When calling getReplies with same arguments, Then same instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        val messageId = randomString()
        val limit = randomInt()
        // when
        val call1 = distinctChatApi.getReplies(messageId, limit)
        val call2 = distinctChatApi.getReplies(messageId, limit)
        // then
        // verify same instance of call is reused
        Assert.assertTrue(call1 === call2)
    }

    @Test
    fun `When calling getReplies with same argument and first call finishes, Then different instance of Call is returned`() =
        runTest {
            // given
            val delegateApi = mock<ChatApi>()
            whenever(delegateApi.getReplies(any(), any())).thenReturn(mock())
            val distinctChatApi = DistinctChatApi(backgroundScope, delegateApi)
            val messageId = randomString()
            val limit = randomInt()
            // when
            val call1 = distinctChatApi.getReplies(messageId, limit)
            // Complete first call
            call1.await()
            val call2 = distinctChatApi.getReplies(messageId, limit)
            // then
            // verify different instance of call is returned
            Assert.assertFalse(call1 === call2)
        }

    @Test
    fun `When calling getReplies with different arguments, Then different instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        val messageId1 = randomString()
        val messageId2 = randomString()
        val limit = randomInt()
        // when
        val call1 = distinctChatApi.getReplies(messageId1, limit)
        val call2 = distinctChatApi.getReplies(messageId2, limit)
        // then
        // verify different instance of call is returned
        Assert.assertFalse(call1 === call2)
    }

    @Test
    fun `When calling getNewerReplies with same arguments, Then same instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        val parentId = randomString()
        val limit = randomInt()
        val lastId = randomString()
        // when
        val call1 = distinctChatApi.getNewerReplies(parentId, limit, lastId)
        val call2 = distinctChatApi.getNewerReplies(parentId, limit, lastId)
        // then
        // verify same instance of call is reused
        Assert.assertTrue(call1 === call2)
    }

    @Test
    fun `When calling getNewerReplies with same arguments and first call finishes, Then different instance of Call is returned`() =
        runTest {
            // given
            val delegateApi = mock<ChatApi>()
            whenever(delegateApi.getNewerReplies(any(), any(), any())).thenReturn(mock())
            val distinctChatApi = DistinctChatApi(backgroundScope, delegateApi)
            val parentId = randomString()
            val limit = randomInt()
            val lastId = randomString()
            // when
            val call1 = distinctChatApi.getNewerReplies(parentId, limit, lastId)
            // Complete first call
            call1.await()
            val call2 = distinctChatApi.getNewerReplies(parentId, limit, lastId)
            // then
            // verify different instance of call is returned
            Assert.assertFalse(call1 === call2)
        }

    @Test
    fun `When calling getNewerReplies with different arguments, Then different instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        val parentId1 = randomString()
        val parentId2 = randomString()
        val limit = randomInt()
        val lastId = randomString()
        // when
        val call1 = distinctChatApi.getNewerReplies(parentId1, limit, lastId)
        val call2 = distinctChatApi.getNewerReplies(parentId2, limit, lastId)
        // then
        // verify different instance of call is returned
        Assert.assertFalse(call1 === call2)
    }

    @Test
    fun `When calling getReactions with same arguments, Then same instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        val messageId = randomString()
        val offset = randomInt()
        val limit = randomInt()
        // when
        val call1 = distinctChatApi.getReactions(messageId, offset, limit)
        val call2 = distinctChatApi.getReactions(messageId, offset, limit)
        // then
        // verify same instance of call is reused
        Assert.assertTrue(call1 === call2)
    }

    @Test
    fun `When calling getReactions with same arguments and first call finishes, Then different instance of Call is returned`() =
        runTest {
            // given
            val delegateApi = mock<ChatApi>()
            whenever(delegateApi.getReactions(any(), any(), any())).thenReturn(mock())
            val distinctChatApi = DistinctChatApi(backgroundScope, delegateApi)
            val messageId = randomString()
            val offset = randomInt()
            val limit = randomInt()
            // when
            val call1 = distinctChatApi.getReactions(messageId, offset, limit)
            // Complete first call
            call1.await()
            val call2 = distinctChatApi.getReactions(messageId, offset, limit)
            // then
            // verify different instance of call is returned
            Assert.assertFalse(call1 === call2)
        }

    @Test
    fun `When calling getReactions with different arguments, Then different instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        val messageId1 = randomString()
        val messageId2 = randomString()
        val offset = randomInt()
        val limit = randomInt()
        // when
        val call1 = distinctChatApi.getReactions(messageId1, offset, limit)
        val call2 = distinctChatApi.getReactions(messageId2, offset, limit)
        // then
        // verify different instance of call is returned
        Assert.assertFalse(call1 === call2)
    }

    @Test
    fun `When calling getMessage with same arguments, Then same instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        val messageId = randomString()
        // when
        val call1 = distinctChatApi.getMessage(messageId)
        val call2 = distinctChatApi.getMessage(messageId)
        // then
        // verify same instance of call is reused
        Assert.assertTrue(call1 === call2)
    }

    @Test
    fun `When calling getMessage with same arguments and first call finishes, Then different instance of Call is returned`() =
        runTest {
            // given
            val delegateApi = mock<ChatApi>()
            whenever(delegateApi.getMessage(any())).thenReturn(mock())
            val distinctChatApi = DistinctChatApi(backgroundScope, delegateApi)
            val messageId = randomString()
            // when
            val call1 = distinctChatApi.getMessage(messageId)
            // Complete first call
            call1.await()
            val call2 = distinctChatApi.getMessage(messageId)
            // then
            // verify different instance of call is returned
            Assert.assertFalse(call1 === call2)
        }

    @Test
    fun `When calling getMessage with different arguments, Then different instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        val messageId1 = randomString()
        val messageId2 = randomString()
        // when
        val call1 = distinctChatApi.getMessage(messageId1)
        val call2 = distinctChatApi.getMessage(messageId2)
        // then
        // verify different instance of call is returned
        Assert.assertFalse(call1 === call2)
    }

    @Test
    fun `When calling getPinnedMessages with same arguments, Then same instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        val channelType = randomString()
        val channelId = randomString()
        val limit = randomInt()
        val sort = QuerySortByField.ascByName<Message>("created_at")
        val pagination = PinnedMessagesPagination.AroundMessage(randomString())
        // when
        val call1 = distinctChatApi.getPinnedMessages(channelType, channelId, limit, sort, pagination)
        val call2 = distinctChatApi.getPinnedMessages(channelType, channelId, limit, sort, pagination)
        // then
        // verify same instance of call is reused
        Assert.assertTrue(call1 === call2)
    }

    @Test
    fun `When calling getPinnedMessages with same arguments and first call finishes, Then different instance of Call is returned`() =
        runTest {
            // given
            val delegateApi = mock<ChatApi>()
            whenever(delegateApi.getPinnedMessages(any(), any(), any(), any(), any())).thenReturn(mock())
            val distinctChatApi = DistinctChatApi(backgroundScope, delegateApi)
            val channelType = randomString()
            val channelId = randomString()
            val limit = randomInt()
            val sort = QuerySortByField.ascByName<Message>("created_at")
            val pagination = PinnedMessagesPagination.AroundMessage(randomString())
            // when
            val call1 = distinctChatApi.getPinnedMessages(channelType, channelId, limit, sort, pagination)
            call1.await()
            val call2 = distinctChatApi.getPinnedMessages(channelType, channelId, limit, sort, pagination)
            // then
            // verify different instance of call is returned
            Assert.assertFalse(call1 === call2)
        }

    @Test
    fun `When calling getPinnedMessages with different arguments, Then different instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        val channelType = randomString()
        val channelId = randomString()
        val limit = randomInt()
        val sort = QuerySortByField.ascByName<Message>("created_at")
        val pagination1 = PinnedMessagesPagination.AroundMessage(randomString())
        val pagination2 = PinnedMessagesPagination.AroundMessage(randomString())
        // when
        val call1 = distinctChatApi.getPinnedMessages(channelType, channelId, limit, sort, pagination1)
        val call2 = distinctChatApi.getPinnedMessages(channelType, channelId, limit, sort, pagination2)
        // then
        // verify different instance of call is returned
        Assert.assertFalse(call1 === call2)
    }

    @Test
    fun `When calling queryChannels with same arguments, Then same instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        val request = Mother.randomQueryChannelsRequest()
        // when
        val call1 = distinctChatApi.queryChannels(request)
        val call2 = distinctChatApi.queryChannels(request)
        // then
        // verify same instance of call is reused
        Assert.assertTrue(call1 === call2)
    }

    @Test
    fun `When calling queryChannels with same arguments and first call finishes, Then different instance of Call is returned`() =
        runTest {
            // given
            val delegateApi = mock<ChatApi>()
            whenever(delegateApi.queryChannels(any())).thenReturn(mock())
            val distinctChatApi = DistinctChatApi(backgroundScope, delegateApi)
            val request = Mother.randomQueryChannelsRequest()
            // when
            val call1 = distinctChatApi.queryChannels(request)
            call1.await()
            val call2 = distinctChatApi.queryChannels(request)
            // then
            // verify different instance of call is returned
            Assert.assertFalse(call1 === call2)
        }

    @Test
    fun `When calling queryChannels with different arguments, Then different instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        val request1 = Mother.randomQueryChannelsRequest()
        val request2 = Mother.randomQueryChannelsRequest()
        // when
        val call1 = distinctChatApi.queryChannels(request1)
        val call2 = distinctChatApi.queryChannels(request2)
        // then
        // verify different instance of call is returned
        Assert.assertFalse(call1 === call2)
    }

    @Test
    fun `When calling queryBannedUsers with same arguments, Then same instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        val filter = Filters.neutral()
        val sort = QuerySortByField.ascByName<BannedUsersSort>("created_at")
        val offset = randomInt()
        val limit = randomInt()
        val createdAtAfter = randomDate()
        val createdAtAfterOrEqual = randomDate()
        val createdAtBefore = randomDate()
        val createdAtBeforeOrEqual = randomDate()
        // when
        val call1 = distinctChatApi.queryBannedUsers(
            filter = filter,
            sort = sort,
            offset = offset,
            limit = limit,
            createdAtAfter = createdAtAfter,
            createdAtAfterOrEqual = createdAtAfterOrEqual,
            createdAtBefore = createdAtBefore,
            createdAtBeforeOrEqual = createdAtBeforeOrEqual,
        )
        val call2 = distinctChatApi.queryBannedUsers(
            filter = filter,
            sort = sort,
            offset = offset,
            limit = limit,
            createdAtAfter = createdAtAfter,
            createdAtAfterOrEqual = createdAtAfterOrEqual,
            createdAtBefore = createdAtBefore,
            createdAtBeforeOrEqual = createdAtBeforeOrEqual,
        )
        // then
        // verify same instance of call is reused
        Assert.assertTrue(call1 === call2)
    }

    @Test
    fun `When calling queryBannedUsers with same arguments and first call finishes, Then different instance of Call is returned`() =
        runTest {
            // given
            val api = mock<ChatApi>()
            whenever(
                api.queryBannedUsers(any(), any(), any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()),
            ).thenReturn(TestCall(Result.Success(emptyList())))
            val distinctChatApi = DistinctChatApi(backgroundScope, api)
            val filter = Filters.neutral()
            val sort = QuerySortByField.ascByName<BannedUsersSort>("created_at")
            val offset = randomInt()
            val limit = randomInt()
            val createdAtAfter = randomDate()
            val createdAtAfterOrEqual = randomDate()
            val createdAtBefore = randomDate()
            val createdAtBeforeOrEqual = randomDate()
            // when
            val call1 = distinctChatApi.queryBannedUsers(
                filter = filter,
                sort = sort,
                offset = offset,
                limit = limit,
                createdAtAfter = createdAtAfter,
                createdAtAfterOrEqual = createdAtAfterOrEqual,
                createdAtBefore = createdAtBefore,
                createdAtBeforeOrEqual = createdAtBeforeOrEqual,
            )
            call1.await()
            val call2 = distinctChatApi.queryBannedUsers(
                filter = filter,
                sort = sort,
                offset = offset,
                limit = limit,
                createdAtAfter = createdAtAfter,
                createdAtAfterOrEqual = createdAtAfterOrEqual,
                createdAtBefore = createdAtBefore,
                createdAtBeforeOrEqual = createdAtBeforeOrEqual,
            )
            // then
            // verify different instance of call is returned
            Assert.assertFalse(call1 === call2)
        }

    @Test
    fun `When calling queryBannedUsers with different arguments, Then different instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        val filter1 = Filters.neutral()
        val filter2 = Filters.ne("field", "value")
        val sort = QuerySortByField.ascByName<BannedUsersSort>("created_at")
        val offset = randomInt()
        val limit = randomInt()
        val createdAtAfter = randomDate()
        val createdAtAfterOrEqual = randomDate()
        val createdAtBefore = randomDate()
        val createdAtBeforeOrEqual = randomDate()
        // when
        val call1 = distinctChatApi.queryBannedUsers(
            filter = filter1,
            sort = sort,
            offset = offset,
            limit = limit,
            createdAtAfter = createdAtAfter,
            createdAtAfterOrEqual = createdAtAfterOrEqual,
            createdAtBefore = createdAtBefore,
            createdAtBeforeOrEqual = createdAtBeforeOrEqual,
        )
        val call2 = distinctChatApi.queryBannedUsers(
            filter = filter2,
            sort = sort,
            offset = offset,
            limit = limit,
            createdAtAfter = createdAtAfter,
            createdAtAfterOrEqual = createdAtAfterOrEqual,
            createdAtBefore = createdAtBefore,
            createdAtBeforeOrEqual = createdAtBeforeOrEqual,
        )
        // then
        // verify different instance of call is returned
        Assert.assertFalse(call1 === call2)
    }

    @Test
    fun `When calling queryMembers with same arguments, Then same instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        val channelType = randomString()
        val channelId = randomString()
        val offset = randomInt()
        val limit = randomInt()
        val filter = Filters.neutral()
        val sort = QuerySortByField.ascByName<Member>("name")
        val members = listOf(randomMember())
        // when
        val call1 = distinctChatApi.queryMembers(
            channelType = channelType,
            channelId = channelId,
            offset = offset,
            limit = limit,
            filter = filter,
            sort = sort,
            members = members,
        )
        val call2 = distinctChatApi.queryMembers(
            channelType = channelType,
            channelId = channelId,
            offset = offset,
            limit = limit,
            filter = filter,
            sort = sort,
            members = members,
        )
        // then
        // verify same instance of call is reused
        Assert.assertTrue(call1 === call2)
    }

    @Test
    fun `When calling queryMembers with same arguments and first call finishes, Then different instance of Call is returned`() =
        runTest {
            // given
            val api = mock<ChatApi>()
            whenever(
                api.queryMembers(any(), any(), any(), any(), any(), any(), any()),
            ).thenReturn(TestCall(Result.Success(emptyList())))
            val distinctChatApi = DistinctChatApi(backgroundScope, api)
            val channelType = randomString()
            val channelId = randomString()
            val offset = randomInt()
            val limit = randomInt()
            val filter = Filters.neutral()
            val sort = QuerySortByField.ascByName<Member>("name")
            val members = listOf(randomMember())
            // when
            val call1 = distinctChatApi.queryMembers(
                channelType = channelType,
                channelId = channelId,
                offset = offset,
                limit = limit,
                filter = filter,
                sort = sort,
                members = members,
            )
            call1.await()
            val call2 = distinctChatApi.queryMembers(
                channelType = channelType,
                channelId = channelId,
                offset = offset,
                limit = limit,
                filter = filter,
                sort = sort,
                members = members,
            )
            // then
            // verify different instance of call is returned
            Assert.assertFalse(call1 === call2)
        }

    @Test
    fun `When calling queryMembers with different arguments, Then different instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        val channelType = randomString()
        val channelId = randomString()
        val offset = randomInt()
        val limit = randomInt()
        val filter = Filters.neutral()
        val sort = QuerySortByField.ascByName<Member>("name")
        val members1 = listOf(randomMember())
        val members2 = listOf(randomMember())
        // when
        val call1 = distinctChatApi.queryMembers(
            channelType = channelType,
            channelId = channelId,
            offset = offset,
            limit = limit,
            filter = filter,
            sort = sort,
            members = members1,
        )
        val call2 = distinctChatApi.queryMembers(
            channelType = channelType,
            channelId = channelId,
            offset = offset,
            limit = limit,
            filter = filter,
            sort = sort,
            members = members2,
        )
        // then
        // verify different instance of call is returned
        Assert.assertFalse(call1 === call2)
    }
}
