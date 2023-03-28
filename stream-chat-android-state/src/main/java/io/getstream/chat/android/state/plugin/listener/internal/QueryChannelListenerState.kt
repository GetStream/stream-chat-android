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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.plugin.listeners.QueryChannelListener
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.result.Result

/**
 * Implementation of [QueryChannelListener] that handles state updates in the SDK.
 *
 * @param logic [LogicRegistry]
 */
internal class QueryChannelListenerState(private val logic: LogicRegistry) : QueryChannelListener {

    override suspend fun onQueryChannelPrecondition(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Result<Unit> = Result.Success(Unit)

    /**
     * The method that should be called before a call to query channel is made.
     *
     * @param channelType [String]
     * @param channelId [String]
     * @param request [QueryChannelRequest]
     */
    override suspend fun onQueryChannelRequest(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ) {
        logic.channel(channelType, channelId).updateStateFromDatabase(request)
    }

    /**
     * The method that should be called after a call to query channel is made.
     *
     * @param result Result<Channel>
     * @param channelType [String]
     * @param channelId [String]
     * @param request [QueryChannelRequest]
     */
    override suspend fun onQueryChannelResult(
        result: Result<Channel>,
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ) {
        val channelStateLogic = logic.channel(channelType, channelId).stateLogic()

        result.onSuccess { channel -> channelStateLogic.propagateChannelQuery(channel, request) }
            .onError(channelStateLogic::propagateQueryError)
    }
}
