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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.plugin.listeners.QueryChannelsListener
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.state.model.querychannels.pagination.internal.QueryChannelsPaginationRequest
import io.getstream.chat.android.state.model.querychannels.pagination.internal.toAnyChannelPaginationRequest
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.result.Result
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * [QueryChannelsListener] implementation for [StatePlugin].
 * Handles querying the channel offline and managing local state updates.
 *
 * This class is considered to handle state. Although this class interacts with the database, it doesn't make sense
 * to use it only with the database and not state module. Therefore this class is considered as a state class and should
 * be initialized inside `StreamStatePluginFactory`. It is important to noticed that this class will be affected
 * by the inclusion/exclusion of the offline plugin. When using offline plugin, the database will be
 * used to update the state of the SDK, but when not, the state will be updated only with the results of the internet
 * request.
 *
 * @param logic [LogicRegistry] provided by the [StreamStatePluginFactory].
 */
internal class QueryChannelsListenerState(
    private val logicProvider: LogicRegistry,
    private val queryingChannelsFree: MutableStateFlow<Boolean>,
) : QueryChannelsListener {

    override suspend fun onQueryChannelsPrecondition(request: QueryChannelsRequest): Result<Unit> {
        return Result.Success(Unit)
    }

    override suspend fun onQueryChannelsRequest(request: QueryChannelsRequest) {
        queryingChannelsFree.value = false
        logicProvider.queryChannels(request).run {
            setCurrentRequest(request)
            queryOffline(request.toPagination())
        }
    }

    override suspend fun onQueryChannelsResult(result: Result<List<Channel>>, request: QueryChannelsRequest) {
        logicProvider.queryChannels(request).onQueryChannelsResult(result, request)
        queryingChannelsFree.value = true
    }

    private companion object {

        private fun QueryChannelsRequest.toPagination(): AnyChannelPaginationRequest =
            QueryChannelsPaginationRequest(
                sort = querySort,
                channelLimit = limit,
                channelOffset = offset,
                messageLimit = messageLimit ?: 10,
                memberLimit = memberLimit ?: 30,
            ).toAnyChannelPaginationRequest()
    }
}
