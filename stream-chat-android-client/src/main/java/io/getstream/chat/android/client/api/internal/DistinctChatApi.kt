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

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api2.optimisation.hash.ChannelQueryKey
import io.getstream.chat.android.client.api2.optimisation.hash.GetNewerRepliesHash
import io.getstream.chat.android.client.api2.optimisation.hash.GetPinnedMessagesHash
import io.getstream.chat.android.client.api2.optimisation.hash.GetReactionsHash
import io.getstream.chat.android.client.api2.optimisation.hash.GetRepliesHash
import io.getstream.chat.android.client.api2.optimisation.hash.QueryBanedUsersHash
import io.getstream.chat.android.client.api2.optimisation.hash.QueryMembersHash
import io.getstream.chat.android.models.BannedUser
import io.getstream.chat.android.models.BannedUsersSort
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.PendingMessage
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.log.StreamLog
import io.getstream.result.call.Call
import io.getstream.result.call.DistinctCall
import kotlinx.coroutines.CoroutineScope
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

/**
 * Prevents simultaneous network calls of the same request.
 */
@Suppress("UNCHECKED_CAST")
internal class DistinctChatApi(
    private val scope: CoroutineScope,
    internal val delegate: ChatApi,
) : ChatApi by delegate {

    private val distinctCalls = ConcurrentHashMap<Int, DistinctCall<out Any>>()

    override fun queryChannel(channelType: String, channelId: String, query: QueryChannelRequest): Call<Channel> {
        val uniqueKey = ChannelQueryKey.from(channelType, channelId, query).hashCode()
        StreamLog.d(TAG) { "[queryChannel] channelType: $channelType, channelId: $channelId, uniqueKey: $uniqueKey" }
        return getOrCreate(uniqueKey) {
            delegate.queryChannel(channelType, channelId, query)
        }
    }

    override fun getRepliesMore(messageId: String, firstId: String, limit: Int): Call<List<Message>> {
        val uniqueKey = GetRepliesHash(messageId, firstId, limit).hashCode()
        StreamLog.d(TAG) {
            "[getRepliesMore] messageId: $messageId, firstId: $firstId, limit: $limit, uniqueKey: $uniqueKey"
        }
        return getOrCreate(uniqueKey) {
            delegate.getRepliesMore(messageId, firstId, limit)
        }
    }

    override fun getReplies(messageId: String, limit: Int): Call<List<Message>> {
        val uniqueKey = GetRepliesHash(messageId, null, limit).hashCode()
        StreamLog.d(TAG) { "[getReplies] messageId: $messageId, limit: $limit, uniqueKey: $uniqueKey" }
        return getOrCreate(uniqueKey) {
            delegate.getReplies(messageId, limit)
        }
    }

    override fun getNewerReplies(parentId: String, limit: Int, lastId: String?): Call<List<Message>> {
        val uniqueKey = GetNewerRepliesHash(parentId, limit, lastId).hashCode()
        StreamLog.d(TAG) {
            "[getNewerReplies] parentId: $parentId, limit: $limit, lastId: $lastId, uniqueKey: $uniqueKey"
        }
        return getOrCreate(uniqueKey) {
            delegate.getNewerReplies(parentId, limit, lastId)
        }
    }

    override fun getReactions(messageId: String, offset: Int, limit: Int): Call<List<Reaction>> {
        val uniqueKey = GetReactionsHash(messageId, offset, limit).hashCode()
        StreamLog.d(TAG) {
            "[getReactions] messageId: $messageId, offset: $offset, limit: $limit, uniqueKey: $uniqueKey"
        }
        return getOrCreate(uniqueKey) {
            delegate.getReactions(messageId, offset, limit)
        }
    }

    override fun getMessage(messageId: String): Call<Message> {
        val uniqueKey = messageId.hashCode()
        StreamLog.d(TAG) { "[getMessage] messageId: $messageId, uniqueKey: $uniqueKey" }
        return getOrCreate(uniqueKey) {
            delegate.getMessage(messageId)
        }
    }

    override fun getPendingMessage(messageId: String): Call<PendingMessage> {
        val uniqueKey = messageId.hashCode()
        StreamLog.d(TAG) { "[getPendingMessage] messageId: $messageId, uniqueKey: $uniqueKey" }
        return getOrCreate(uniqueKey) {
            delegate.getPendingMessage(messageId)
        }
    }

    override fun getPinnedMessages(
        channelType: String,
        channelId: String,
        limit: Int,
        sort: QuerySorter<Message>,
        pagination: PinnedMessagesPagination,
    ): Call<List<Message>> {
        val uniqueKey = GetPinnedMessagesHash(channelType, channelId, limit, sort, pagination).hashCode()
        StreamLog.d(TAG) {
            "[getPinnedMessages] channelType: $channelType, channelId: $channelId, " +
                "limit: $limit, sort: $sort, pagination: $pagination, uniqueKey: $uniqueKey"
        }
        return getOrCreate(uniqueKey) {
            delegate.getPinnedMessages(channelType, channelId, limit, sort, pagination)
        }
    }

    override fun queryChannels(query: QueryChannelsRequest): Call<List<Channel>> {
        val uniqueKey = query.hashCode()
        StreamLog.d(TAG) { "[queryChannels] query: $query, uniqueKey: $uniqueKey" }
        return getOrCreate(uniqueKey) {
            delegate.queryChannels(query)
        }
    }

    override fun queryBannedUsers(
        filter: FilterObject,
        sort: QuerySorter<BannedUsersSort>,
        offset: Int?,
        limit: Int?,
        createdAtAfter: Date?,
        createdAtAfterOrEqual: Date?,
        createdAtBefore: Date?,
        createdAtBeforeOrEqual: Date?,
    ): Call<List<BannedUser>> {
        val uniqueKey = QueryBanedUsersHash(
            filter,
            sort,
            offset,
            limit,
            createdAtAfter,
            createdAtAfterOrEqual,
            createdAtBefore,
            createdAtBeforeOrEqual,
        ).hashCode()

        StreamLog.d(TAG) { "[queryBannedUsers] uniqueKey: $uniqueKey" }

        return getOrCreate(uniqueKey) {
            delegate.queryBannedUsers(
                filter,
                sort,
                offset,
                limit,
                createdAtAfter,
                createdAtAfterOrEqual,
                createdAtBefore,
                createdAtBeforeOrEqual,
            )
        }
    }

    override fun queryMembers(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySorter<Member>,
        members: List<Member>,
    ): Call<List<Member>> {
        val uniqueKey = QueryMembersHash(channelType, channelId, offset, limit, filter, sort, members)
            .hashCode()

        StreamLog.d(TAG) { "[queryMembers] uniqueKey: $uniqueKey" }

        return getOrCreate(uniqueKey) {
            delegate.queryMembers(channelType, channelId, offset, limit, filter, sort, members)
        }
    }

    private fun <T : Any> getOrCreate(
        uniqueKey: Int,
        callBuilder: () -> Call<T>,
    ): Call<T> {
        return distinctCalls[uniqueKey] as? DistinctCall<T>
            ?: DistinctCall(scope = scope, callBuilder = callBuilder) {
                distinctCalls.remove(uniqueKey)
            }.also {
                distinctCalls[uniqueKey] = it
            }
    }

    private companion object {
        private const val TAG = "Chat:DistinctApi"
    }
}
