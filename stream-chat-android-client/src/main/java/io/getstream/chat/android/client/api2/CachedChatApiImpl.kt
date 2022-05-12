/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.CachedChatApi
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.cache.CacheCoordinator
import io.getstream.chat.android.client.cache.CallCacheCoordinator
import io.getstream.chat.android.client.cache.hash.GetPinnedMessagesHash
import io.getstream.chat.android.client.cache.hash.GetReactionsHash
import io.getstream.chat.android.client.cache.hash.GetRepliesHash
import io.getstream.chat.android.client.cache.hash.QueryBanedUsersHash
import io.getstream.chat.android.client.cache.hash.QueryChannelsHash
import io.getstream.chat.android.client.cache.hash.QueryMembersHash
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.BannedUser
import io.getstream.chat.android.client.models.BannedUsersSort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import java.util.Date

private const val DEFAULT_CACHE_MILLIS = 300

/**
 * Cached version of ChatApi.
 *
 * @param chatApi [ChatApi]. The [ChatApi] without cache.
 * @param callCacheCoordinator [CacheCoordinator] The coordinator of the cache.
 */
internal class CachedChatApiImpl(
    private val chatApi: ChatApi,
    private val callCacheCoordinator: CacheCoordinator = CallCacheCoordinator(DEFAULT_CACHE_MILLIS),
) : ChatApi by chatApi, CachedChatApi {

    override fun getRepliesMore(
        messageId: String,
        firstId: String,
        limit: Int,
        forceRefresh: Boolean,
    ): Call<List<Message>> {
        val hash = GetRepliesHash(messageId, firstId, limit).hashCode()
        return callCacheCoordinator.cachedCall(hash, forceRefresh, getRepliesMore(messageId, firstId, limit))
    }

    override fun getReplies(messageId: String, limit: Int, forceRefresh: Boolean): Call<List<Message>> {
        val hash = GetRepliesHash(messageId, null, limit).hashCode()
        return callCacheCoordinator.cachedCall(hash, forceRefresh, getReplies(messageId, limit))
    }

    override fun getReactions(messageId: String, offset: Int, limit: Int, forceRefresh: Boolean): Call<List<Reaction>> {
        val hash = GetReactionsHash(messageId, offset, limit).hashCode()
        return callCacheCoordinator.cachedCall(hash, forceRefresh, getReactions(messageId, offset, limit))
    }

    override fun getMessage(messageId: String, forceRefresh: Boolean): Call<Message> {
        val hash = messageId.hashCode()
        return callCacheCoordinator.cachedCall(hash, forceRefresh, getMessage(messageId))
    }

    override fun getPinnedMessages(
        channelType: String,
        channelId: String,
        limit: Int,
        sort: QuerySort<Message>,
        pagination: PinnedMessagesPagination,
        forceRefresh: Boolean,
    ): Call<List<Message>> {
        val hash = GetPinnedMessagesHash(channelType, channelId, limit, sort, pagination).hashCode()
        return callCacheCoordinator.cachedCall(
            hash,
            forceRefresh,
            getPinnedMessages(channelType, channelId, limit, sort, pagination)
        )
    }

    override fun queryChannels(query: QueryChannelsRequest, forceRefresh: Boolean): Call<List<Channel>> {
        return callCacheCoordinator.cachedCall(query.hashCode(), forceRefresh, queryChannels(query))
    }

    override fun queryChannel(
        channelType: String,
        channelId: String,
        query: QueryChannelRequest,
        forceRefresh: Boolean,
    ): Call<Channel> {
        val hash = QueryChannelsHash(channelType, channelId, query).hashCode()
        return callCacheCoordinator.cachedCall(hash, forceRefresh, queryChannel(channelType, channelId, query))
    }

    override fun queryMembers(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort<Member>,
        members: List<Member>,
        forceRefresh: Boolean,
    ): Call<List<Member>> {
        val hash = QueryMembersHash(channelType, channelId, offset, limit, filter, sort, members).hashCode()
        return callCacheCoordinator.cachedCall(
            hash,
            forceRefresh,
            queryMembers(channelType, channelId, offset, limit, filter, sort, members)
        )
    }

    override fun queryBannedUsers(
        filter: FilterObject,
        sort: QuerySort<BannedUsersSort>,
        offset: Int?,
        limit: Int?,
        createdAtAfter: Date?,
        createdAtAfterOrEqual: Date?,
        createdAtBefore: Date?,
        createdAtBeforeOrEqual: Date?,
        forceRefresh: Boolean,
    ): Call<List<BannedUser>> {
        val hash = QueryBanedUsersHash(
            filter,
            sort,
            offset,
            limit,
            createdAtAfter,
            createdAtAfterOrEqual,
            createdAtBefore,
            createdAtBeforeOrEqual
        ).hashCode()

        val call = queryBannedUsers(
            filter,
            sort,
            offset,
            limit,
            createdAtAfter,
            createdAtAfterOrEqual,
            createdAtBefore,
            createdAtBeforeOrEqual
        )
        return callCacheCoordinator.cachedCall(hash, forceRefresh, call)
    }
}
