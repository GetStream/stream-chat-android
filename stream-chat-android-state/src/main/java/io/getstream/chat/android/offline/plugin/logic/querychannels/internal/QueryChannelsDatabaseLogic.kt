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

import io.getstream.chat.android.client.extensions.internal.applyPagination
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelConfig
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistance.repository.ChannelConfigRepository
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.QueryChannelsRepository
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest

@Suppress("LongParameterList")
internal class QueryChannelsDatabaseLogic(
    private val queryChannelsRepository: QueryChannelsRepository,
    private val channelConfigRepository: ChannelConfigRepository,
    private val channelRepository: ChannelRepository,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val repositoryFacade: RepositoryFacade,
) {

    internal suspend fun storeStateForChannels(
        configs: Collection<ChannelConfig>?,
        users: List<User>,
        channels: Collection<Channel>,
        messages: List<Message>,
        cacheForMessages: Boolean,
    ) {
        configs?.let { channelConfigRepository.insertChannelConfigs(it) }
        userRepository.insertUsers(users)
        channelRepository.insertChannels(channels)
        messageRepository.insertMessages(messages, cacheForMessages)
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
     * Select channel from database without fetching messages
     *
     * @param cid String
     */
    internal suspend fun selectChannelWithoutMessages(cid: String): Channel? {
        return channelRepository.selectChannelWithoutMessages(cid)
    }

    /**
     * Select channels from database without fetching messages
     *
     * @param cids String
     */
    internal suspend fun selectChannelsWithoutMessages(cids: List<String>): List<Channel> {
        return channelRepository.selectChannelsWithoutMessages(cids)
    }

    /**
     * Insert a query spec that was made in the database.
     *
     * @param queryChannelsSpec QueryChannelsSpec
     */
    internal suspend fun insertQueryChannels(queryChannelsSpec: QueryChannelsSpec) {
        return queryChannelsRepository.insertQueryChannels(queryChannelsSpec)
    }

    /**
     * Insert the configs of the channels
     *
     * @param configs Collection<ChannelConfig>
     */
    internal suspend fun insertChannelConfigs(configs: Collection<ChannelConfig>) {
        return channelConfigRepository.insertChannelConfigs(configs)
    }
}
