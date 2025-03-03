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

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
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
        // when
        val call1 = distinctChatApi.queryChannel("messaging", "1", QueryChannelRequest())
        val call2 = distinctChatApi.queryChannel("messaging", "1", QueryChannelRequest())
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
            // when
            val call1 = distinctChatApi.queryChannel("messaging", "1", QueryChannelRequest())
            // Complete first call
            call1.await()
            val call2 = distinctChatApi.queryChannel("messaging", "1", QueryChannelRequest())
            // then
            // verify different instance of call is returned
            Assert.assertFalse(call1 === call2)
        }

    @Test
    fun `When calling queryChannel with different arguments, Then different instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        // when
        val call1 = distinctChatApi.queryChannel("messaging", "1", QueryChannelRequest())
        val call2 = distinctChatApi.queryChannel("messaging", "2", QueryChannelRequest())
        // then
        // verify different instance of call is returned
        Assert.assertFalse(call1 === call2)
    }

    @Test
    fun `When calling getRepliesMore with same arguments, Then same instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        // when
        val call1 = distinctChatApi.getRepliesMore("1", "2", 10)
        val call2 = distinctChatApi.getRepliesMore("1", "2", 10)
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
            // when
            val call1 = distinctChatApi.getRepliesMore("1", "2", 10)
            // Complete first call
            call1.await()
            val call2 = distinctChatApi.getRepliesMore("1", "2", 10)
            // then
            // verify different instance of call is returned
            Assert.assertFalse(call1 === call2)
        }

    @Test
    fun `When calling getRepliesMore with different arguments, Then different instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        // when
        val call1 = distinctChatApi.getRepliesMore("1", "2", 10)
        val call2 = distinctChatApi.getRepliesMore("1", "3", 10)
        // then
        // verify different instance of call is returned
        Assert.assertFalse(call1 === call2)
    }

    @Test
    fun `When calling getReplies with same arguments, Then same instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        // when
        val call1 = distinctChatApi.getReplies("1", 10)
        val call2 = distinctChatApi.getReplies("1", 10)
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
            // when
            val call1 = distinctChatApi.getReplies("1", 10)
            // Complete first call
            call1.await()
            val call2 = distinctChatApi.getReplies("1", 10)
            // then
            // verify different instance of call is returned
            Assert.assertFalse(call1 === call2)
        }

    @Test
    fun `When calling getReplies with different arguments, Then different instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        // when
        val call1 = distinctChatApi.getReplies("1", 10)
        val call2 = distinctChatApi.getReplies("2", 10)
        // then
        // verify different instance of call is returned
        Assert.assertFalse(call1 === call2)
    }

    @Test
    fun `When calling getNewerReplies with same arguments, Then same instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        // when
        val call1 = distinctChatApi.getNewerReplies("1", 10, "2")
        val call2 = distinctChatApi.getNewerReplies("1", 10, "2")
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
            // when
            val call1 = distinctChatApi.getNewerReplies("1", 10, "2")
            // Complete first call
            call1.await()
            val call2 = distinctChatApi.getNewerReplies("1", 10, "2")
            // then
            // verify different instance of call is returned
            Assert.assertFalse(call1 === call2)
        }

    @Test
    fun `When calling getNewerReplies with different arguments, Then different instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        // when
        val call1 = distinctChatApi.getNewerReplies("1", 10, "2")
        val call2 = distinctChatApi.getNewerReplies("2", 10, "2")
        // then
        // verify different instance of call is returned
        Assert.assertFalse(call1 === call2)
    }

    @Test
    fun `When calling getReactions with same arguments, Then same instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        // when
        val call1 = distinctChatApi.getReactions("1", 0, 10)
        val call2 = distinctChatApi.getReactions("1", 0, 10)
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
            // when
            val call1 = distinctChatApi.getReactions("1", 0, 10)
            // Complete first call
            call1.await()
            val call2 = distinctChatApi.getReactions("1", 0, 10)
            // then
            // verify different instance of call is returned
            Assert.assertFalse(call1 === call2)
        }

    @Test
    fun `When calling getReactions with different arguments, Then different instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        // when
        val call1 = distinctChatApi.getReactions("1", 0, 10)
        val call2 = distinctChatApi.getReactions("2", 0, 10)
        // then
        // verify different instance of call is returned
        Assert.assertFalse(call1 === call2)
    }

    @Test
    fun `When calling getMessage with same arguments, Then same instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        // when
        val call1 = distinctChatApi.getMessage("1")
        val call2 = distinctChatApi.getMessage("1")
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
            // when
            val call1 = distinctChatApi.getMessage("1")
            // Complete first call
            call1.await()
            val call2 = distinctChatApi.getMessage("1")
            // then
            // verify different instance of call is returned
            Assert.assertFalse(call1 === call2)
        }

    @Test
    fun `When calling getMessage with different arguments, Then different instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        // when
        val call1 = distinctChatApi.getMessage("1")
        val call2 = distinctChatApi.getMessage("2")
        // then
        // verify different instance of call is returned
        Assert.assertFalse(call1 === call2)
    }

    @Test
    fun `When calling getPinnedMessages with same arguments, Then same instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        // when
        val call1 = distinctChatApi.getPinnedMessages(
            channelType = "messaging",
            channelId = "1",
            limit = 10,
            sort = QuerySortByField.ascByName("created_at"),
            pagination = PinnedMessagesPagination.AroundMessage("1"),
        )
        val call2 = distinctChatApi.getPinnedMessages(
            channelType = "messaging",
            channelId = "1",
            limit = 10,
            sort = QuerySortByField.ascByName("created_at"),
            pagination = PinnedMessagesPagination.AroundMessage("1"),
        )
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
            // when
            val call1 = distinctChatApi.getPinnedMessages(
                channelType = "messaging",
                channelId = "1",
                limit = 10,
                sort = QuerySortByField.ascByName("created_at"),
                pagination = PinnedMessagesPagination.AroundMessage("1"),
            )
            call1.await()
            val call2 = distinctChatApi.getPinnedMessages(
                channelType = "messaging",
                channelId = "1",
                limit = 10,
                sort = QuerySortByField.ascByName("created_at"),
                pagination = PinnedMessagesPagination.AroundMessage("1"),
            )
            // then
            // verify different instance of call is returned
            Assert.assertFalse(call1 === call2)
        }

    @Test
    fun `When calling getPinnedMessages with different arguments, Then different instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        // when
        val call1 = distinctChatApi.getPinnedMessages(
            channelType = "messaging",
            channelId = "1",
            limit = 10,
            sort = QuerySortByField.ascByName("created_at"),
            pagination = PinnedMessagesPagination.AroundMessage("1"),
        )
        val call2 = distinctChatApi.getPinnedMessages(
            channelType = "messaging",
            channelId = "2",
            limit = 10,
            sort = QuerySortByField.ascByName("created_at"),
            pagination = PinnedMessagesPagination.AroundMessage("1"),
        )
        // then
        // verify different instance of call is returned
        Assert.assertFalse(call1 === call2)
    }

    @Test
    fun `When calling queryChannels with same arguments, Then same instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        // when
        val call1 = distinctChatApi.queryChannels(QueryChannelsRequest(Filters.neutral(), limit = 10))
        val call2 = distinctChatApi.queryChannels(QueryChannelsRequest(Filters.neutral(), limit = 10))
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
            // when
            val call1 = distinctChatApi.queryChannels(QueryChannelsRequest(Filters.neutral(), limit = 10))
            call1.await()
            val call2 = distinctChatApi.queryChannels(QueryChannelsRequest(Filters.neutral(), limit = 10))
            // then
            // verify different instance of call is returned
            Assert.assertFalse(call1 === call2)
        }

    @Test
    fun `When calling queryChannels with different arguments, Then different instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        // when
        val call1 = distinctChatApi.queryChannels(QueryChannelsRequest(Filters.neutral(), limit = 10))
        val call2 = distinctChatApi.queryChannels(QueryChannelsRequest(Filters.ne("name", "Test channel"), limit = 10))
        // then
        // verify different instance of call is returned
        Assert.assertFalse(call1 === call2)
    }

    @Test
    fun `When calling queryBannedUsers with same arguments, Then same instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        // when
        val call1 = distinctChatApi.queryBannedUsers(
            filter = Filters.neutral(),
            sort = QuerySortByField.ascByName("created_at"),
            offset = 0,
            limit = 10,
            createdAtAfter = null,
            createdAtAfterOrEqual = null,
            createdAtBefore = null,
            createdAtBeforeOrEqual = null,
        )
        val call2 = distinctChatApi.queryBannedUsers(
            filter = Filters.neutral(),
            sort = QuerySortByField.ascByName("created_at"),
            offset = 0,
            limit = 10,
            createdAtAfter = null,
            createdAtAfterOrEqual = null,
            createdAtBefore = null,
            createdAtBeforeOrEqual = null,
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
            // when
            val call1 = distinctChatApi.queryBannedUsers(
                filter = Filters.neutral(),
                sort = QuerySortByField.ascByName("created_at"),
                offset = 0,
                limit = 10,
                createdAtAfter = null,
                createdAtAfterOrEqual = null,
                createdAtBefore = null,
                createdAtBeforeOrEqual = null,
            )
            call1.await()
            val call2 = distinctChatApi.queryBannedUsers(
                filter = Filters.neutral(),
                sort = QuerySortByField.ascByName("created_at"),
                offset = 0,
                limit = 10,
                createdAtAfter = null,
                createdAtAfterOrEqual = null,
                createdAtBefore = null,
                createdAtBeforeOrEqual = null,
            )
            // then
            // verify different instance of call is returned
            Assert.assertFalse(call1 === call2)
        }

    @Test
    fun `When calling queryBannedUsers with different arguments, Then different instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        // when
        val call1 = distinctChatApi.queryBannedUsers(
            filter = Filters.neutral(),
            sort = QuerySortByField.ascByName("created_at"),
            offset = 0,
            limit = 10,
            createdAtAfter = null,
            createdAtAfterOrEqual = null,
            createdAtBefore = null,
            createdAtBeforeOrEqual = null,
        )
        val call2 = distinctChatApi.queryBannedUsers(
            filter = Filters.ne("field", "value"),
            sort = QuerySortByField.ascByName("created_at"),
            offset = 0,
            limit = 10,
            createdAtAfter = null,
            createdAtAfterOrEqual = null,
            createdAtBefore = null,
            createdAtBeforeOrEqual = null,
        )
        // then
        // verify different instance of call is returned
        Assert.assertFalse(call1 === call2)
    }

    @Test
    fun `When calling queryMembers with same arguments, Then same instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        // when
        val call1 = distinctChatApi.queryMembers(
            channelType = "messaging",
            channelId = "1",
            offset = 0,
            limit = 10,
            filter = Filters.neutral(),
            sort = QuerySortByField.ascByName("name"),
            members = emptyList(),
        )
        val call2 = distinctChatApi.queryMembers(
            channelType = "messaging",
            channelId = "1",
            offset = 0,
            limit = 10,
            filter = Filters.neutral(),
            sort = QuerySortByField.ascByName("name"),
            members = emptyList(),
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
            // when
            val call1 = distinctChatApi.queryMembers(
                channelType = "messaging",
                channelId = "1",
                offset = 0,
                limit = 10,
                filter = Filters.neutral(),
                sort = QuerySortByField.ascByName("name"),
                members = emptyList(),
            )
            call1.await()
            val call2 = distinctChatApi.queryMembers(
                channelType = "messaging",
                channelId = "1",
                offset = 0,
                limit = 10,
                filter = Filters.neutral(),
                sort = QuerySortByField.ascByName("name"),
                members = emptyList(),
            )
            // then
            // verify different instance of call is returned
            Assert.assertFalse(call1 === call2)
        }

    @Test
    fun `When calling queryMembers with different arguments, Then different instance of Call is returned`() {
        // given
        val distinctChatApi = DistinctChatApi(TestScope(), mock())
        // when
        val call1 = distinctChatApi.queryMembers(
            channelType = "messaging",
            channelId = "1",
            offset = 0,
            limit = 10,
            filter = Filters.neutral(),
            sort = QuerySortByField.ascByName("name"),
            members = emptyList(),
        )
        val call2 = distinctChatApi.queryMembers(
            channelType = "messaging",
            channelId = "2",
            offset = 0,
            limit = 10,
            filter = Filters.neutral(),
            sort = QuerySortByField.ascByName("name"),
            members = emptyList(),
        )
        // then
        // verify different instance of call is returned
        Assert.assertFalse(call1 === call2)
    }
}
