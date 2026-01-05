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

package io.getstream.chat.android.ui.common.feature.channel.header

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.state.extensions.watchChannelAsState
import io.getstream.chat.android.ui.common.state.messages.list.ChannelHeaderViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

// The XML SDK has its own ViewModel for the MessageListHeaderView.
// This class can be later shared after a refactor of the XML SDK.
/**
 * Controller responsible for managing the state of the channel header.
 */
@InternalStreamChatApi
public class ChannelHeaderViewController(
    cid: String,
    private val scope: CoroutineScope,
    private val chatClient: ChatClient = ChatClient.instance(),
    channelState: Flow<ChannelState> = chatClient
        .watchChannelAsState(cid = cid, messageLimit = 0, coroutineScope = scope)
        .filterNotNull(),
) {

    /**
     * A [StateFlow] representing the current state of the channel header.
     */
    public val state: StateFlow<ChannelHeaderViewState> =
        channelState.flatMapLatest { state ->
            combine(
                chatClient.clientState.connectionState,
                state.channelData,
                state.membersCount,
                state.watcherCount,
            ) { connectionState, _, _, _ ->
                ChannelHeaderViewState.Content(
                    currentUser = chatClient.getCurrentUser(),
                    connectionState = connectionState,
                    channel = state.toChannel(),
                )
            }
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_IN_MILLIS),
            initialValue = ChannelHeaderViewState.Loading,
        )
}

private const val STOP_TIMEOUT_IN_MILLIS = 5_000L
