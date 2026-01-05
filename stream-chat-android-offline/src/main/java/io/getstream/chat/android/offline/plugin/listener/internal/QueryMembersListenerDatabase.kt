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

package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.extensions.internal.toCid
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.plugin.listeners.QueryMembersListener
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.result.Result

/**
 * [QueryMembersListener] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Handles updating members in the database.
 *
 * @param userRepository [UserRepository] to cache intermediate data and final result related to users.
 * @param channelRepository [ChannelRepository] to cache intermediate data and final result related to channels.
 */
internal class QueryMembersListenerDatabase(
    private val userRepository: UserRepository,
    private val channelRepository: ChannelRepository,
) : QueryMembersListener {

    override suspend fun onQueryMembersResult(
        result: Result<List<Member>>,
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySorter<Member>,
        members: List<Member>,
    ) {
        if (result is Result.Success) {
            val resultMembers = result.value

            userRepository.insertUsers(resultMembers.map(Member::user))
            channelRepository.updateMembersForChannel(Pair(channelType, channelId).toCid(), resultMembers)
        }
    }
}
