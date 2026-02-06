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

package io.getstream.chat.android.ui.viewmodel.channels.internal

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.state.watchChannelAsState
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.models.Channel
import io.getstream.log.taggedLogger
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
    private val cid: String,
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    private val logger by taggedLogger("Chat:ChannelActionsVM")

    /**
     * Holds information about the current channel and is actively updated.
     */
    private val channelState: Flow<ChannelState> = observeChannelState()

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

    private fun observeChannelState(): Flow<ChannelState> {
        val messageLimit = DEFAULT_MESSAGE_LIMIT
        logger.d { "[observeChannelState] cid: $cid, messageLimit: $messageLimit" }
        return chatClient.watchChannelAsState(
            cid = cid,
            messageLimit = messageLimit,
            coroutineScope = viewModelScope,
        ).filterNotNull()
    }

    private companion object {

        /**
         * The default limit for messages count in requests.
         */
        private const val DEFAULT_MESSAGE_LIMIT: Int = 0
    }
}
