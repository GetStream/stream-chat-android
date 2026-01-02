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

package io.getstream.chat.android.client.api.internal

import android.annotation.SuppressLint
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
import kotlinx.coroutines.test.TestScope
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

internal class DistinctChatApiEnablerTest {

    private val messageId = randomString()
    private val firstId = randomString()
    private val parentId = randomString()
    private val lastId = randomString()
    private val limit = randomInt()
    private val offset = randomInt()
    private val channelType = randomString()
    private val channelId = randomString()
    private val messageSort = QuerySortByField.ascByName<Message>("created_at")
    private val pagination = PinnedMessagesPagination.AroundMessage(randomString())
    private val channelRequest = Mother.randomQueryChannelRequest()
    private val channelsRequest = Mother.randomQueryChannelsRequest()
    private val filter = Filters.neutral()
    private val bannedUserSort = QuerySortByField.ascByName<BannedUsersSort>("created_at")
    private val createdAtAfter = randomDate()
    private val createdAtAfterOrEqual = randomDate()
    private val createdAtBefore = randomDate()
    private val createdAtBeforeOrEqual = randomDate()
    private val memberSort = QuerySortByField.ascByName<Member>("name")
    private val members = listOf(randomMember())

    @SuppressLint("CheckResult")
    @Suppress("LongMethod")
    @Test
    fun testDistinctApiEnabled() {
        // given
        val api = mock<ChatApi>()
        val distinctApi = spy(DistinctChatApi(TestScope(), api))
        val enabler = DistinctChatApiEnabler(distinctApi) { true }
        // when
        enabler.getRepliesMore(messageId, firstId, limit)
        enabler.getReplies(messageId, limit)
        enabler.getNewerReplies(parentId, limit, lastId)
        enabler.getReactions(messageId, offset, limit)
        enabler.getMessage(messageId)
        enabler.getPendingMessage(messageId)
        enabler.getPinnedMessages(channelType, channelId, limit, messageSort, pagination)
        enabler.queryChannels(channelsRequest)
        enabler.queryBannedUsers(
            filter = filter,
            sort = bannedUserSort,
            offset = offset,
            limit = limit,
            createdAtAfter = createdAtAfter,
            createdAtAfterOrEqual = createdAtAfterOrEqual,
            createdAtBefore = createdAtBefore,
            createdAtBeforeOrEqual = createdAtBeforeOrEqual,
        )
        enabler.queryMembers(
            channelType = channelType,
            channelId = channelId,
            offset = offset,
            limit = limit,
            filter = filter,
            sort = memberSort,
            members = members,
        )
        enabler.queryChannel(channelType, channelId, channelRequest)
        // then
        verify(distinctApi, times(1)).getRepliesMore(messageId, firstId, limit)
        verify(distinctApi, times(1)).getReplies(messageId, limit)
        verify(distinctApi, times(1)).getNewerReplies(parentId, limit, lastId)
        verify(distinctApi, times(1)).getReactions(messageId, offset, limit)
        verify(distinctApi, times(1)).getMessage(messageId)
        verify(distinctApi, times(1)).getPendingMessage(messageId)
        verify(distinctApi, times(1)).getPinnedMessages(
            channelType = channelType,
            channelId = channelId,
            limit = limit,
            sort = messageSort,
            pagination = pagination,
        )
        verify(distinctApi, times(1)).queryChannels(channelsRequest)
        verify(distinctApi, times(1)).queryBannedUsers(
            filter = filter,
            sort = bannedUserSort,
            offset = offset,
            limit = limit,
            createdAtAfter = createdAtAfter,
            createdAtAfterOrEqual = createdAtAfterOrEqual,
            createdAtBefore = createdAtBefore,
            createdAtBeforeOrEqual = createdAtBeforeOrEqual,
        )
        verify(distinctApi, times(1)).queryMembers(
            channelType = channelType,
            channelId = channelId,
            offset = offset,
            limit = limit,
            filter = filter,
            sort = memberSort,
            members = members,
        )
        verify(distinctApi, times(1)).queryChannel(channelType, channelId, channelRequest)
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
        enabler.getRepliesMore(messageId, firstId, limit)
        enabler.getReplies(messageId, limit)
        enabler.getNewerReplies(parentId, limit, lastId)
        enabler.getReactions(messageId, offset, limit)
        enabler.getMessage(messageId)
        enabler.getPendingMessage(messageId)
        enabler.getPinnedMessages(channelType, channelId, limit, messageSort, pagination)
        enabler.queryChannels(channelsRequest)
        enabler.queryBannedUsers(
            filter = filter,
            sort = bannedUserSort,
            offset = offset,
            limit = limit,
            createdAtAfter = createdAtAfter,
            createdAtAfterOrEqual = createdAtAfterOrEqual,
            createdAtBefore = createdAtBefore,
            createdAtBeforeOrEqual = createdAtBeforeOrEqual,
        )
        enabler.queryMembers(
            channelType = channelType,
            channelId = channelId,
            offset = offset,
            limit = limit,
            filter = filter,
            sort = memberSort,
            members = members,
        )
        enabler.queryChannel(channelType, channelId, channelRequest)

        // then
        verify(api, times(1)).getRepliesMore(messageId, firstId, limit)
        verify(api, times(1)).getReplies(messageId, limit)
        verify(api, times(1)).getNewerReplies(parentId, limit, lastId)
        verify(api, times(1)).getReactions(messageId, offset, limit)
        verify(api, times(1)).getMessage(messageId)
        verify(api, times(1)).getPendingMessage(messageId)
        verify(api, times(1)).getPinnedMessages(channelType, channelId, limit, messageSort, pagination)
        verify(api, times(1)).queryChannels(channelsRequest)
        verify(api, times(1)).queryBannedUsers(
            filter = filter,
            sort = bannedUserSort,
            offset = offset,
            limit = limit,
            createdAtAfter = createdAtAfter,
            createdAtAfterOrEqual = createdAtAfterOrEqual,
            createdAtBefore = createdAtBefore,
            createdAtBeforeOrEqual = createdAtBeforeOrEqual,
        )
        verify(api, times(1)).queryMembers(
            channelType = channelType,
            channelId = channelId,
            offset = offset,
            limit = limit,
            filter = filter,
            sort = memberSort,
            members = members,
        )
        verify(api, times(1)).queryChannel(channelType, channelId, channelRequest)
        verify(distinctApi, times(0)).getRepliesMore(any(), any(), any())
        verify(distinctApi, times(0)).getReplies(any(), any())
        verify(distinctApi, times(0)).getNewerReplies(any(), any(), any())
        verify(distinctApi, times(0)).getReactions(any(), any(), any())
        verify(distinctApi, times(0)).getMessage(any())
        verify(distinctApi, times(0)).getPendingMessage(any())
        verify(distinctApi, times(0)).getPinnedMessages(any(), any(), any(), any(), any())
        verify(distinctApi, times(0)).queryChannels(any())
        verify(distinctApi, times(0)).queryBannedUsers(any(), any(), any(), any(), any(), any(), any(), any())
        verify(distinctApi, times(0)).queryMembers(any(), any(), any(), any(), any(), any(), any())
        verify(distinctApi, times(0)).queryChannel(any(), any(), any())
    }
}
