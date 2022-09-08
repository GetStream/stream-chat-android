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

import io.getstream.chat.android.client.plugin.listeners.HideChannelListener
import io.getstream.chat.android.client.utils.Result

/**
 * This class act as an composition of multiple [HideChannelListener]. This is only necessary
 * along StatePlugin lives inside OfflinePlugin. When both plugins are separated, this class can
 * and should be deleted.
 *
 * @param hideChannelListenerList List<HideChannelListener>
 */
internal class HideChannelListenerComposite(
    private val hideChannelListenerList: List<HideChannelListener>,
) : HideChannelListener {

    override suspend fun onHideChannelPrecondition(
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ): Result<Unit> {
        return hideChannelListenerList.map { listener ->
            listener.onHideChannelPrecondition(channelType, channelId, clearHistory)
        }.fold(Result.success(Unit)) { acc, result ->
            if (acc.isError) acc else result
        }
    }

    override suspend fun onHideChannelRequest(channelType: String, channelId: String, clearHistory: Boolean) {
        hideChannelListenerList.forEach { listener ->
            listener.onHideChannelRequest(channelType, channelId, clearHistory)
        }
    }

    override suspend fun onHideChannelResult(
        result: Result<Unit>,
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ) {
        hideChannelListenerList.forEach { listener ->
            listener.onHideChannelResult(result, channelType, channelId, clearHistory)
        }
    }
}
