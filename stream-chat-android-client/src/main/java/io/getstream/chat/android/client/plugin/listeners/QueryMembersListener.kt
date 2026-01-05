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

package io.getstream.chat.android.client.plugin.listeners

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.result.Result

/**
 * Listener of [ChatClient.queryMembers] requests.
 */
public interface QueryMembersListener {

    /**
     * Runs this function on the [Result] of this request.
     *
     * @param result Result of this request.
     * @param channelType The type of channel.
     * @param channelId The id of the channel.
     * @param offset Offset limit.
     * @param limit Number of members to fetch.
     * @param filter [FilterObject] to filter members of certain type.
     * @param sort Sort the list of members.
     * @param members List of members.
     */
    @Suppress("LongParameterList")
    public suspend fun onQueryMembersResult(
        result: Result<List<Member>>,
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySorter<Member>,
        members: List<Member>,
    )
}
