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

package io.getstream.chat.android.offline.utils.internal

import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.StateRegistry

/**
 * Checks if the channel can be marked as read and marks it locally if needed.
 *
 * @param logic [LogicRegistry]
 * @param state [StateRegistry]
 * @param clientState [ClientState]
 */
internal class ChannelMarkReadHelper(
    private val logic: LogicRegistry,
    private val state: StateRegistry,
) {

    /**
     * Marks channel as read locally if different conditions are met:
     * 1. Channel has read events enabled
     * 2. Channel has messages not marked as read yet
     * 3. Current user is set
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     *
     * @return The flag to determine if the channel was marked as read locally.
     */
    internal fun markChannelReadLocallyIfNeeded(channelType: String, channelId: String): Boolean {
        return state.mutableChannel(channelType = channelType, channelId = channelId)
            .markChannelAsRead()
    }
}
