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
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Role
import io.getstream.chat.android.models.UserGroup
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Aggregates every source the composer needs for mention suggestions into a single ordered [List] of [Mention].
 *
 * Non-user mentions are gated on a matching channel capability (e.g. [ChannelCapabilities.NOTIFY_CHANNEL]);
 * types without the capability are skipped entirely (no API call, no result). User mentions are always looked up.
 */
internal class MentionLookupHandler(
    private val chatClient: ChatClient,
    private val channelState: StateFlow<ChannelState?>,
    private val userLookupHandler: UserLookupHandler,
) {

    /**
     * Returns the mention suggestions for [query] in popup order:
     * `@channel`, `@here`, roles (alphabetical), groups (alphabetical), users
     * (order is the responsibility of the configured [UserLookupHandler]).
     */
    suspend fun handleMentionLookup(query: String): List<Mention> = coroutineScope {
        val capabilities = channelState.value?.channelData?.value?.ownCapabilities.orEmpty()
        val getGroups = async {
            if (ChannelCapabilities.NOTIFY_GROUP in capabilities) searchGroups(query) else emptyList()
        }
        // User suggestions are intentionally not gated on CREATE_MENTION: on Permissions V1 regular members lack
        // that capability, yet user mentions still work since the server only enforces it on send under V2.
        val getUsers = async { userLookupHandler.handleUserLookup(query) }
        val getRoles = async {
            if (ChannelCapabilities.NOTIFY_ROLE in capabilities) searchRoles(query) else emptyList()
        }

        buildList {
            if (ChannelCapabilities.NOTIFY_CHANNEL in capabilities &&
                Mention.Channel.display.matchesMentionQuery(query)
            ) {
                add(Mention.Channel)
            }
            if (ChannelCapabilities.NOTIFY_HERE in capabilities &&
                Mention.Here.display.matchesMentionQuery(query)
            ) {
                add(Mention.Here)
            }

            getRoles.await().forEach { add(Mention.Role(it)) }
            getGroups.await().forEach { add(Mention.Group(it)) }
            getUsers.await().forEach { add(Mention.User(it)) }
        }
    }

    private suspend fun searchGroups(query: String): List<UserGroup> =
        if (query.isEmpty()) {
            emptyList()
        } else {
            val team = channelState.value?.channelData?.value?.team?.takeIf(String::isNotEmpty)
            chatClient.searchUserGroups(query = query, teamId = team).await()
                .getOrNull()
                .orEmpty()
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER, UserGroup::name))
        }

    private suspend fun searchRoles(query: String): List<String> =
        if (query.isEmpty()) {
            emptyList()
        } else {
            chatClient.searchRoles(query = query).await()
                .getOrNull()
                .orEmpty()
                .mapTo(mutableSetOf(), Role::name)
                .sortedWith(String.CASE_INSENSITIVE_ORDER)
        }

    companion object {
        private fun String.matchesMentionQuery(query: String): Boolean =
            query.isEmpty() || startsWith(query, ignoreCase = true)
    }
}
