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

package io.getstream.chat.android.ui.viewmodel.channels.internal

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.state.extensions.watchChannelAsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Used by [ChannelActionsDialogFragment] to provide the correct state
 * containing information about the channel.
 *
 * @param cid The full channel id, i.e. "messaging:123".
 * @param chatClient The main entry point for all low-level chat operations.
 */
internal class ChannelActionsViewModel(
    cid: String,
    chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    /**
     * Holds information about the current channel and is actively updated.
     */
    private val channelState: Flow<ChannelState> =
        chatClient.watchChannelAsState(
            cid = cid,
            messageLimit = DEFAULT_MESSAGE_LIMIT,
            coroutineScope = viewModelScope,
        ).filterNotNull()

    /**
     * The current [Channel] created from [ChannelState]. It emits new data either when
     * channel data or the list of members in [ChannelState] updates.
     *
     * Combining the two is important because members changing online status does not result in
     * channel events being received.
     */
    val channel: LiveData<Channel> =
        channelState.flatMapLatest { state ->
            combine(
                state.channelData,
                state.members,
                state.watcherCount,
            ) { _, _, _ ->
                state.toChannel()
            }
        }.asLiveData()

    private companion object {

        /**
         * The default limit for messages count in requests.
         */
        private const val DEFAULT_MESSAGE_LIMIT: Int = 0
    }
}
