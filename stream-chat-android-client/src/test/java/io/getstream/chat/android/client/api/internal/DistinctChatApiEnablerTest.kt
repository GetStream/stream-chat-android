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

import android.annotation.SuppressLint
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import kotlinx.coroutines.test.TestScope
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

internal class DistinctChatApiEnablerTest {

    @SuppressLint("CheckResult")
    @Suppress("LongMethod")
    @Test
    fun testDistinctApiEnabled() {
        // given
        val api = mock<ChatApi>()
        val distinctApi = spy(DistinctChatApi(TestScope(), api))
        val enabler = DistinctChatApiEnabler(distinctApi) { true }
        // when
        enabler.getRepliesMore("messageId", "firstId", 10)
        enabler.getReplies("messageId", 10)
        enabler.getNewerReplies("parentId", 10, "lastId")
        enabler.getReactions("messageId", 0, 10)
        enabler.getMessage("messageId")
        enabler.getPinnedMessages(
            channelType = "messaging",
            channelId = "2",
            limit = 10,
            sort = QuerySortByField.ascByName("created_at"),
            pagination = PinnedMessagesPagination.AroundMessage("1"),
        )
        enabler.queryChannels(QueryChannelsRequest(Filters.neutral(), limit = 10))
        enabler.queryBannedUsers(
            filter = Filters.ne("field", "value"),
            sort = QuerySortByField.ascByName("created_at"),
            offset = 0,
            limit = 10,
            createdAtAfter = null,
            createdAtAfterOrEqual = null,
            createdAtBefore = null,
            createdAtBeforeOrEqual = null,
        )
        enabler.queryMembers(
            channelType = "messaging",
            channelId = "1",
            offset = 0,
            limit = 10,
            filter = Filters.neutral(),
            sort = QuerySortByField.ascByName("name"),
            members = emptyList(),
        )
        enabler.queryChannel("channelType", "channelId", QueryChannelRequest())
        // then
        verify(distinctApi, times(1)).getRepliesMore("messageId", "firstId", 10)
        verify(distinctApi, times(1)).getReplies("messageId", 10)
        verify(distinctApi, times(1)).getNewerReplies("parentId", 10, "lastId")
        verify(distinctApi, times(1)).getReactions("messageId", 0, 10)
        verify(distinctApi, times(1)).getMessage("messageId")
        verify(distinctApi, times(1)).getPinnedMessages(
            channelType = "messaging",
            channelId = "2",
            limit = 10,
            sort = QuerySortByField.ascByName("created_at"),
            pagination = PinnedMessagesPagination.AroundMessage("1"),
        )
        verify(distinctApi, times(1)).queryChannels(QueryChannelsRequest(Filters.neutral(), limit = 10))
        verify(distinctApi, times(1)).queryBannedUsers(
            filter = Filters.ne("field", "value"),
            sort = QuerySortByField.ascByName("created_at"),
            offset = 0,
            limit = 10,
            createdAtAfter = null,
            createdAtAfterOrEqual = null,
            createdAtBefore = null,
            createdAtBeforeOrEqual = null,
        )
        verify(distinctApi, times(1)).queryMembers(
            channelType = "messaging",
            channelId = "1",
            offset = 0,
            limit = 10,
            filter = Filters.neutral(),
            sort = QuerySortByField.ascByName("name"),
            members = emptyList(),
        )
        verify(distinctApi, times(1)).queryChannel("channelType", "channelId", QueryChannelRequest())
        verifyNoInteractions(api)
    }

    @SuppressLint("CheckResult")
    @Suppress("LongMethod")
    @Test
    fun testDistinctApiDisabled() {
        // given
        val api = mock<ChatApi>()
        val distinctApi = spy(DistinctChatApi(TestScope(), api))
        val enabler = DistinctChatApiEnabler(distinctApi) { false }
        // when
        enabler.getRepliesMore("messageId", "firstId", 10)
        enabler.getReplies("messageId", 10)
        enabler.getNewerReplies("parentId", 10, "lastId")
        enabler.getReactions("messageId", 0, 10)
        enabler.getMessage("messageId")
        enabler.getPinnedMessages(
            channelType = "messaging",
            channelId = "2",
            limit = 10,
            sort = QuerySortByField.ascByName("created_at"),
            pagination = PinnedMessagesPagination.AroundMessage("1"),
        )
        enabler.queryChannels(QueryChannelsRequest(Filters.neutral(), limit = 10))
        enabler.queryBannedUsers(
            filter = Filters.ne("field", "value"),
            sort = QuerySortByField.ascByName("created_at"),
            offset = 0,
            limit = 10,
            createdAtAfter = null,
            createdAtAfterOrEqual = null,
            createdAtBefore = null,
            createdAtBeforeOrEqual = null,
        )
        enabler.queryMembers(
            channelType = "messaging",
            channelId = "1",
            offset = 0,
            limit = 10,
            filter = Filters.neutral(),
            sort = QuerySortByField.ascByName("name"),
            members = emptyList(),
        )
        enabler.queryChannel("channelType", "channelId", QueryChannelRequest())

        // then
        verify(api, times(1)).getRepliesMore("messageId", "firstId", 10)
        verify(api, times(1)).getReplies("messageId", 10)
        verify(api, times(1)).getNewerReplies("parentId", 10, "lastId")
        verify(api, times(1)).getReactions("messageId", 0, 10)
        verify(api, times(1)).getMessage("messageId")
        verify(api, times(1)).getPinnedMessages(
            channelType = "messaging",
            channelId = "2",
            limit = 10,
            sort = QuerySortByField.ascByName("created_at"),
            pagination = PinnedMessagesPagination.AroundMessage("1"),
        )
        verify(api, times(1)).queryChannels(QueryChannelsRequest(Filters.neutral(), limit = 10))
        verify(api, times(1)).queryBannedUsers(
            filter = Filters.ne("field", "value"),
            sort = QuerySortByField.ascByName("created_at"),
            offset = 0,
            limit = 10,
            createdAtAfter = null,
            createdAtAfterOrEqual = null,
            createdAtBefore = null,
            createdAtBeforeOrEqual = null,
        )
        verify(api, times(1)).queryMembers(
            channelType = "messaging",
            channelId = "1",
            offset = 0,
            limit = 10,
            filter = Filters.neutral(),
            sort = QuerySortByField.ascByName("name"),
            members = emptyList(),
        )
        verify(api, times(1)).queryChannel("channelType", "channelId", QueryChannelRequest())
        verify(distinctApi, times(0)).getRepliesMore(any(), any(), any())
        verify(distinctApi, times(0)).getReplies(any(), any())
        verify(distinctApi, times(0)).getNewerReplies(any(), any(), any())
        verify(distinctApi, times(0)).getReactions(any(), any(), any())
        verify(distinctApi, times(0)).getMessage(any())
        verify(distinctApi, times(0)).getPinnedMessages(any(), any(), any(), any(), any())
        verify(distinctApi, times(0)).queryChannels(any())
        verify(distinctApi, times(0)).queryBannedUsers(any(), any(), any(), any(), any(), any(), any(), any())
        verify(distinctApi, times(0)).queryMembers(any(), any(), any(), any(), any(), any(), any())
        verify(distinctApi, times(0)).queryChannel(any(), any(), any())
    }
}
