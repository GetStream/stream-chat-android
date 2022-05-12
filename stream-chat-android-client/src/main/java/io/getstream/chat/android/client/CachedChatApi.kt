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

package io.getstream.chat.android.client

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.BannedUser
import io.getstream.chat.android.client.models.BannedUsersSort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import java.util.Date

/**
 * Cached version of [ChatApi]
 */
internal interface CachedChatApi : ChatApi {

    fun getRepliesMore(
        messageId: String,
        firstId: String,
        limit: Int,
        forceRefresh: Boolean,
    ): Call<List<Message>>

    fun getReplies(messageId: String, limit: Int, forceRefresh: Boolean): Call<List<Message>>

    fun getReactions(messageId: String, offset: Int, limit: Int, forceRefresh: Boolean): Call<List<Reaction>>

    fun getMessage(messageId: String, forceRefresh: Boolean): Call<Message>

    fun getPinnedMessages(
        channelType: String,
        channelId: String,
        limit: Int,
        sort: QuerySort<Message>,
        pagination: PinnedMessagesPagination,
        forceRefresh: Boolean
    ): Call<List<Message>>

    fun queryChannels(query: QueryChannelsRequest, forceRefresh: Boolean): Call<List<Channel>>

    fun queryChannel(
        channelType: String,
        channelId: String = "",
        query: QueryChannelRequest,
        forceRefresh: Boolean
    ): Call<Channel>

    fun queryMembers(
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort<Member>,
        members: List<Member>,
        forceRefresh: Boolean
    ): Call<List<Member>>

    fun queryBannedUsers(
        filter: FilterObject,
        sort: QuerySort<BannedUsersSort>,
        offset: Int?,
        limit: Int?,
        createdAtAfter: Date?,
        createdAtAfterOrEqual: Date?,
        createdAtBefore: Date?,
        createdAtBeforeOrEqual: Date?,
        forceRefresh: Boolean
    ): Call<List<BannedUser>>
}
