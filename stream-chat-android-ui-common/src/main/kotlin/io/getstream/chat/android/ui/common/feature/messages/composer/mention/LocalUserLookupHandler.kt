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
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.extensions.state
import io.getstream.chat.android.ui.common.feature.messages.composer.query.filter.DefaultQueryFilter
import io.getstream.chat.android.ui.common.feature.messages.composer.query.filter.QueryFilter
import io.getstream.log.taggedLogger

/**
 * Local user lookup handler. It uses the local state to search for users.
 *
 * @param chatClient Chat client used to query members.
 * @param channelCid The CID of the channel we are querying for members.
 * @param filter The filter used to filter the users.
 */
public class LocalUserLookupHandler @JvmOverloads constructor(
    private val chatClient: ChatClient,
    private val channelCid: String,
    private val filter: QueryFilter<User> = DefaultQueryFilter { it.name.ifBlank { it.id } },
) : UserLookupHandler {

    private val logger by taggedLogger("Chat:UserLookupLocal")

    override suspend fun handleUserLookup(query: String): List<User> {
        try {
            if (DEBUG) logger.d { "[handleUserLookup] query: \"$query\"" }
            val (channelType, channelId) = channelCid.cidToTypeAndId()
            val channelState = chatClient.state.channel(channelType, channelId)
            val localUsers = channelState.members.value.map { it.user }
            val membersCount = channelState.membersCount.value
            return when (membersCount == localUsers.size) {
                true -> filter.filter(localUsers, query).also {
                    if (DEBUG) logger.v { "[handleUserLookup] found ${it.size} users" }
                }
                else -> {
                    if (DEBUG) logger.v { "[handleUserLookup] #empty; users: ${localUsers.size} out of $membersCount" }
                    emptyList()
                }
            }
        } catch (e: Exception) {
            logger.e(e) { "[handleUserLookup] failed: $e" }
            return emptyList()
        }
    }

    private companion object {
        private const val DEBUG = false
    }
}
