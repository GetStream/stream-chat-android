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

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.experimental.plugin.listeners.QueryChannelsListener
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.query.AnyChannelPaginationRequest
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.model.querychannels.pagination.internal.QueryChannelsPaginationRequest
import io.getstream.chat.android.offline.model.querychannels.pagination.internal.toAnyChannelPaginationRequest
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry

/**
 * [QueryChannelsListener] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Handles querying the channel offline and managing local state updates.
 *
 * @param logic [LogicRegistry] provided by the [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 */
internal class QueryChannelsListenerImpl(private val logic: LogicRegistry) : QueryChannelsListener {

    private val logger = ChatLogger.get("QueryChannelsLogic")

    override suspend fun onQueryChannelsPrecondition(request: QueryChannelsRequest): Result<Unit> {
        val loader = logic.queryChannels(request).loadingForCurrentRequest()
        return if (loader.value) {
            logger.logI("Another request to load channels is in progress. Ignoring this request.")
            Result.error(ChatError("Another request to load messages is in progress. Ignoring this request."))
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun onQueryChannelsRequest(request: QueryChannelsRequest) {
        logic.queryChannels(request).run {
            setCurrentQueryChannelsRequest(request)
            queryOffline(request.toPagination())
        }
    }

    override suspend fun onQueryChannelsResult(result: Result<List<Channel>>, request: QueryChannelsRequest) {
        logic.queryChannels(request).onQueryChannelsResult(result, request)
    }

    private companion object {
        private fun QueryChannelsRequest.toPagination(): AnyChannelPaginationRequest =
            QueryChannelsPaginationRequest(
                sort = querySort,
                channelLimit = limit,
                channelOffset = offset,
                messageLimit = messageLimit,
                memberLimit = memberLimit
            ).toAnyChannelPaginationRequest()
    }
}
