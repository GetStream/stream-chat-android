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

import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.plugin.listeners.MarkAllReadListener
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.state.StateRegistry

/**
 * [MarkAllReadListener] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Marks all active channels as read if needed.
 *
 * @param logic [LogicRegistry]
 * @param state [StateRegistry]
 */
internal class MarkAllReadListenerState(
    private val logic: LogicRegistry,
    private val state: StateRegistry,
) : MarkAllReadListener {

    /**
     * Marks all active channels as read if needed.
     *
     * @see [LogicRegistry.getActiveChannelsLogic]
     */
    override suspend fun onMarkAllReadRequest() {
        logic.getActiveChannelsLogic().map { channel ->
            val (channelType, channelId) = channel.cid.cidToTypeAndId()
            state.markChannelAsRead(channelType = channelType, channelId = channelId)
        }
    }
}
