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
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.feature.messages.composer.query.filter.DefaultQueryFilter
import io.getstream.chat.android.ui.common.feature.messages.composer.query.filter.QueryFilter
import io.getstream.log.taggedLogger

/**
 * Default implementation for [UserLookupHandler].
 *
 * @param localHandler The local user lookup handler.
 * @param remoteHandler The remote user lookup handler.
 */
public class DefaultUserLookupHandler(
    private val localHandler: UserLookupHandler,
    private val remoteHandler: UserLookupHandler,
) : UserLookupHandler {

    /**
     * Secondary constructor for [DefaultUserLookupHandler].
     *
     * @param chatClient Chat client used to query members.
     * @param channelCid The CID of the channel we are querying for members.
     * @param localFilter The filter used to filter the cached users during the local lookup.
     */
    @JvmOverloads
    public constructor(
        chatClient: ChatClient,
        channelCid: String,
        localFilter: QueryFilter<User> = DefaultQueryFilter { it.name.ifBlank { it.id } },
    ) : this(
        localHandler = LocalUserLookupHandler(chatClient, channelCid, localFilter),
        remoteHandler = RemoteUserLookupHandler(chatClient, channelCid),
    )

    private val logger by taggedLogger("Chat:UserLookupHandler")

    override suspend fun handleUserLookup(query: String): List<User> {
        logger.d { "[handleUserLookup] query: \"$query\"" }
        return localHandler.handleUserLookup(query).ifEmpty {
            logger.v { "[handleUserLookup] no local results" }
            remoteHandler.handleUserLookup(query)
        }.also {
            logger.v { "[handleUserLookup] found ${it.size} users" }
        }
    }
}
