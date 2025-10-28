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

package io.getstream.chat.android.state.plugin.logic.querychannels.internal

import io.getstream.chat.android.client.extensions.internal.applyPagination
import io.getstream.chat.android.client.persistance.repository.ChannelConfigRepository
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.QueryChannelsRepository
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelConfig

@Suppress("LongParameterList")
internal class QueryChannelsDatabaseLogic(
    private val queryChannelsRepository: QueryChannelsRepository,
    private val channelConfigRepository: ChannelConfigRepository,
    private val channelRepository: ChannelRepository,
    private val repositoryFacade: RepositoryFacade,
) {

    internal suspend fun storeStateForChannels(channels: Collection<Channel>) {
        repositoryFacade.storeStateForChannels(channels)
    }

    /**
     * Fetch channels from database
     *
     * @param pagination [AnyChannelPaginationRequest]
     * @param queryChannelsSpec [QueryChannelsSpec]
     */
    internal suspend fun fetchChannelsFromCache(
        pagination: AnyChannelPaginationRequest,
        queryChannelsSpec: QueryChannelsSpec?,
    ): List<Channel> {
        val query = queryChannelsSpec?.run {
            queryChannelsRepository.selectBy(queryChannelsSpec.filter, queryChannelsSpec.querySort)
        } ?: return emptyList()

        return repositoryFacade.selectChannels(query.cids.toList(), pagination).applyPagination(pagination)
    }

    /**
     * Select a channel from database without fetching messages
     *
     * @param cid String
     */
    internal suspend fun selectChannel(cid: String): Channel? = channelRepository.selectChannel(cid)

    /**
     * Select channels from database without fetching messages
     *
     * @param cids List<String>
     */
    internal suspend fun selectChannels(cids: List<String>): List<Channel> = channelRepository.selectChannels(cids)

    /**
     * Insert a query spec that was made in the database.
     *
     * @param queryChannelsSpec QueryChannelsSpec
     */
    internal suspend fun insertQueryChannels(queryChannelsSpec: QueryChannelsSpec) = queryChannelsRepository.insertQueryChannels(queryChannelsSpec)

    /**
     * Insert the configs of the channels
     *
     * @param configs Collection<ChannelConfig>
     */
    internal suspend fun insertChannelConfigs(configs: Collection<ChannelConfig>) = channelConfigRepository.insertChannelConfigs(configs)
}
