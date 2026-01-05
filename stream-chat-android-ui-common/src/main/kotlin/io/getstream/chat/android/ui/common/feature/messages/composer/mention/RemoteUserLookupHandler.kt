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

package io.getstream.chat.android.ui.common.feature.messages.composer.mention

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.log.taggedLogger
import io.getstream.result.Result

/**
 * Remote user lookup handler. It uses the remote API to search for users.
 *
 * @param chatClient Chat client used to query members.
 * @param channelCid The CID of the channel we are querying for members.
 */
public class RemoteUserLookupHandler(
    private val chatClient: ChatClient,
    private val channelCid: String,
) : UserLookupHandler {

    private val logger by taggedLogger("Chat:UserLookupRemote")

    override suspend fun handleUserLookup(query: String): List<User> {
        return when {
            query.isNotEmpty() -> {
                if (DEBUG) logger.v { "[handleUserLookup] search remotely" }
                chatClient.queryMembersByUsername(channelCid = channelCid, query = query)
            }
            else -> {
                if (DEBUG) logger.v { "[handleUserLookup] #empty; query: $query" }
                emptyList()
            }
        }
    }

    /**
     * Queries the backend for channel members whose username contains the string represented by the argument [query].
     *
     * @param channelCid The CID of the channel we are querying for members.
     * @param query The string for which we are querying the backend in order to see if it is contained
     * within a member's username.
     *
     * @return A list of users whose username contains the string represented by [query] or an empty list in case
     * no usernames contain the given string.
     */
    private suspend fun ChatClient.queryMembersByUsername(
        channelCid: String,
        query: String,
    ): List<User> {
        if (DEBUG) logger.d { "[queryMembersByUsername] query: \"$query\"" }
        val (channelType, channelId) = channelCid.cidToTypeAndId()
        val result = queryMembers(
            channelType = channelType,
            channelId = channelId,
            offset = QUERY_MEMBERS_REQUEST_OFFSET,
            limit = QUERY_MEMBERS_REQUEST_LIMIT,
            filter = Filters.autocomplete(fieldName = "name", value = query),
            sort = QuerySortByField.ascByName(fieldName = "name"),
            members = listOf(),
        ).await()

        return when (result) {
            is Result.Success -> {
                if (DEBUG) logger.v { "[queryMembersByUsername] found ${result.value.size} users" }
                result.value.map { it.user }.filter { it.name.contains(query, true) }
            }
            is Result.Failure -> {
                logger.e { "[queryMembersByUsername] failed: ${result.value.message}" }
                emptyList()
            }
        }
    }

    private companion object {
        private const val DEBUG = false

        /**
         * Pagination offset for the member query.
         */
        private const val QUERY_MEMBERS_REQUEST_OFFSET: Int = 0

        /**
         * The upper limit of members the query is allowed to return.
         */
        private const val QUERY_MEMBERS_REQUEST_LIMIT: Int = 30
    }
}
