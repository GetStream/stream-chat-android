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

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.extensions.internal.applyPagination
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelConfig
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistance.repository.ChannelConfigRepository
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.QueryChannelsRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.client.query.pagination.isRequestingMoreThanLastMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

@Suppress("LongParameterList")
internal class QueryChannelsDatabaseLogicImpl(
    private val queryChannelsRepository: QueryChannelsRepository,
    private val channelConfigRepository: ChannelConfigRepository,
    private val channelRepository: ChannelRepository,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val scope: CoroutineScope,
    private val defaultConfig: Config,
) : QueryChannelsDatabaseLogic {

    /**
     * Store the state oc the channels in the database.
     *
     * @param configs Collection<ChannelConfig>
     * @param users List<User>
     * @param channels: Collection<Channel>
     * @param messages: List<Message>
     * @param cacheForMessages: Boolean
     */
    private suspend fun selectChannels(
        channelIds: List<String>,
        pagination: AnyChannelPaginationRequest?,
        forceCache: Boolean = false,
    ): List<Channel> {
        // fetch the channel entities from room
        val channels = channelRepository.selectChannels(channelIds, forceCache)
        // TODO why it is not compared this way?
        //  pagination?.isRequestingMoreThanLastMessage() == true
        val messagesMap = if (pagination?.isRequestingMoreThanLastMessage() != false) {
            // with postgres this could be optimized into a single query instead of N, not sure about sqlite on android
            // sqlite has window functions: https://sqlite.org/windowfunctions.html
            // but android runs a very dated version: https://developer.android.com/reference/android/database/sqlite/package-summary
            channelIds.map { cid ->
                scope.async { cid to messageRepository.selectMessagesForChannel(cid, pagination) }
            }.awaitAll().toMap()
        } else {
            emptyMap()
        }

        return channels.onEach { channel ->
            channel.enrichChannel(messagesMap, defaultConfig)
        }
    }

    override suspend fun storeStateForChannels(
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
    override suspend fun fetchChannelsFromCache(
        pagination: AnyChannelPaginationRequest,
        queryChannelsSpec: QueryChannelsSpec?,
    ): List<Channel> {
        val query = queryChannelsSpec?.run {
            queryChannelsRepository.selectBy(queryChannelsSpec.filter, queryChannelsSpec.querySort)
        } ?: return emptyList()

        return selectChannels(query.cids.toList(), pagination).applyPagination(pagination)
    }

    /**
     * Select channels from database without fetching messages
     *
     * @param cid String
     */
    override suspend fun selectChannelWithoutMessages(cid: String): Channel? {
        return channelRepository.selectChannelWithoutMessages(cid)
    }

    /**
     * Insert a query spec that was made in the database.
     *
     * @param queryChannelsSpec QueryChannelsSpec
     */
    override suspend fun insertQueryChannels(queryChannelsSpec: QueryChannelsSpec) {
        return queryChannelsRepository.insertQueryChannels(queryChannelsSpec)
    }

    /**
     * Insert the configs of the channels
     *
     * @param configs Collection<ChannelConfig>
     */
    override suspend fun insertChannelConfigs(configs: Collection<ChannelConfig>) {
        return channelConfigRepository.insertChannelConfigs(configs)
    }

    @VisibleForTesting
    fun Channel.enrichChannel(messageMap: Map<String, List<Message>>, defaultConfig: Config) {
        config = channelConfigRepository.selectChannelConfig(type)?.config ?: defaultConfig
        messages = if (messageMap.containsKey(cid)) {
            val fullList = (messageMap[cid] ?: error("Messages must be in the map")) + messages
            fullList.distinctBy(Message::id)
        } else {
            messages
        }
    }
}
