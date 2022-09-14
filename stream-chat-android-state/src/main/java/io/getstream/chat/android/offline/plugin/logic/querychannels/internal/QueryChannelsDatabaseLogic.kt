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

package io.getstream.chat.android.offline.plugin.logic.querychannels.internal

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelConfig
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest

internal interface QueryChannelsDatabaseLogic {

    /**
     * Store the state oc the channels in the database.
     *
     * @param configs Collection<ChannelConfig>
     * @param users List<User>
     * @param channels: Collection<Channel>
     * @param messages: List<Message>
     * @param cacheForMessages: Boolean
     */
    suspend fun storeStateForChannels(
        configs: Collection<ChannelConfig>? = null,
        users: List<User>,
        channels: Collection<Channel>,
        messages: List<Message>,
        cacheForMessages: Boolean = false,
    )

    /**
     * Fetch channels from database
     *
     * @param pagination [AnyChannelPaginationRequest]
     * @param queryChannelsSpec [QueryChannelsSpec]
     */
    suspend fun fetchChannelsFromCache(
        pagination: AnyChannelPaginationRequest,
        queryChannelsSpec: QueryChannelsSpec?
    ): List<Channel>

    /**
     * Select channels from database without fetching messages
     *
     * @param cid String
     */
    suspend fun selectChannelWithoutMessages(cid: String): Channel?

    /**
     * Insert a query spec that was made in the database.
     *
     * @param queryChannelsSpec QueryChannelsSpec
     */
    suspend fun insertQueryChannels(queryChannelsSpec: QueryChannelsSpec)

    /**
     * Insert the configs of the channels
     *
     * @param configs Collection<ChannelConfig>
     */
    suspend fun insertChannelConfigs(configs: Collection<ChannelConfig>)
}
