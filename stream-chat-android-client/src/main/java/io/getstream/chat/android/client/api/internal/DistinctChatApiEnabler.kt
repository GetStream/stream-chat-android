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

package io.getstream.chat.android.client.api.internal

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.models.BannedUser
import io.getstream.chat.android.models.BannedUsersSort
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.PendingMessage
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.result.call.Call
import java.util.Date

/**
 * Enables/Disables [DistinctChatApi] based on [distinctCallsEnabled] return value.
 */
@Suppress("UNCHECKED_CAST")
internal class DistinctChatApiEnabler(
    private val distinctApi: DistinctChatApi,
    private val distinctCallsEnabled: () -> Boolean,
) : ChatApi by distinctApi.delegate {

    private val originalApi = distinctApi.delegate

    override fun getRepliesMore(messageId: String, firstId: String, limit: Int): Call<List<Message>> = getApi().getRepliesMore(messageId, firstId, limit)

    override fun getReplies(messageId: String, limit: Int): Call<List<Message>> = getApi().getReplies(messageId, limit)

    override fun getNewerReplies(parentId: String, limit: Int, lastId: String?): Call<List<Message>> = getApi().getNewerReplies(parentId, limit, lastId)

    override fun getReactions(messageId: String, offset: Int, limit: Int): Call<List<Reaction>> = getApi().getReactions(messageId, offset, limit)

    override fun getMessage(messageId: String): Call<Message> = getApi().getMessage(messageId)

    override fun getPendingMessage(messageId: String): Call<PendingMessage> = getApi().getPendingMessage(messageId)

    override fun getPinnedMessages(
        channelType: String,
        channelId: String,
        limit: Int,
        sort: QuerySorter<Message>,
        pagination: PinnedMessagesPagination,
    ): Call<List<Message>> = getApi().getPinnedMessages(channelType, channelId, limit, sort, pagination)

    override fun queryChannels(query: QueryChannelsRequest): Call<List<Channel>> = getApi().queryChannels(query)

    override fun queryBannedUsers(
        filter: FilterObject,
        sort: QuerySorter<BannedUsersSort>,
        offset: Int?,
        limit: Int?,
        createdAtAfter: Date?,
        createdAtAfterOrEqual: Date?,
        createdAtBefore: Date?,
        createdAtBeforeOrEqual: Date?,
    ): Call<List<BannedUser>> = getApi().queryBannedUsers(
        filter,
        sort,
        offset,
        limit,
        createdAtAfter,
        createdAtAfterOrEqual,
        createdAtBefore,
        createdAtBeforeOrEqual,
    )

    override fun queryMembers(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySorter<Member>,
        members: List<Member>,
    ): Call<List<Member>> = getApi().queryMembers(channelType, channelId, offset, limit, filter, sort, members)

    override fun queryChannel(channelType: String, channelId: String, query: QueryChannelRequest): Call<Channel> = getApi().queryChannel(channelType, channelId, query)

    private fun getApi() = if (distinctCallsEnabled()) distinctApi else originalApi
}
