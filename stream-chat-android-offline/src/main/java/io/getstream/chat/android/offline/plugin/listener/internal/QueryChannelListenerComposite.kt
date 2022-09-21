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
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.plugin.listeners.QueryChannelListener
import io.getstream.chat.android.client.utils.Result

/**
 * This class act as an composition of multiple [QueryChannelListener]. This is only necessary
 * along StatePlugin lives inside OfflinePlugin. When both plugins are separated, this class can
 * and should be deleted.
 */
internal class QueryChannelListenerComposite(
    private val queryChannelListenerList: List<QueryChannelListener>,
) : QueryChannelListener {

    override suspend fun onQueryChannelPrecondition(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Result<Unit> {
        return queryChannelListenerList.map { listener ->
            listener.onQueryChannelPrecondition(channelType, channelId, request)
        }.foldResults()
    }

    override suspend fun onQueryChannelRequest(channelType: String, channelId: String, request: QueryChannelRequest) {
        queryChannelListenerList.forEach { listener ->
            listener.onQueryChannelRequest(channelType, channelId, request)
        }
    }

    override suspend fun onQueryChannelResult(
        result: Result<Channel>,
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ) {
        queryChannelListenerList.forEach { listener ->
            listener.onQueryChannelResult(result, channelType, channelId, request)
        }
    }
}
