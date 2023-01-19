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

package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.internal.users
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.plugin.listeners.QueryChannelListener
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.onSuccessSuspend
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelConfig

/**
 * Implementation for [QueryChannelListener] that handles database update.
 */
internal class QueryChannelListenerDatabase(private val repos: RepositoryFacade) : QueryChannelListener {

    override suspend fun onQueryChannelPrecondition(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Result<Unit> = Result.Success(Unit)

    override suspend fun onQueryChannelRequest(channelType: String, channelId: String, request: QueryChannelRequest) {
        /*
         * Nothing to do. This class handles only the result of the API request, which can be separated from the
         * state logic.
         */
    }

    /**
     * Updates the database of the SDK once the query for channel is complete successfully.
     *
     * @param result Result<Channel>
     * @param channelType String
     * @param channelId String
     * @param request [QueryChannelRequest]
     */
    override suspend fun onQueryChannelResult(
        result: Result<Channel>,
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ) {
        result.onSuccessSuspend { channel ->
            // first thing here needs to be updating configs otherwise we have a race with receiving events
            repos.insertChannelConfig(ChannelConfig(channel.type, channel.config))
            storeStateForChannel(channel, !request.isFilteringMessages())
        }
    }

    private suspend fun storeStateForChannel(channel: Channel, isFirstPage: Boolean) {
        val users = channel.users().associateBy { it.id }.toMutableMap()
        val configs: MutableCollection<ChannelConfig> = mutableSetOf(ChannelConfig(channel.type, channel.config))
        channel.messages.forEach { message ->
            message.enrichWithCid(channel.cid)
            users.putAll(message.users().associateBy { it.id })
        }

        repos.storeStateForChannels(
            configs = configs,
            users = users.values.toList(),
            channels = listOf(channel),
            updateFirstMessage = isFirstPage,
            messages = channel.messages,
        )
    }
}
