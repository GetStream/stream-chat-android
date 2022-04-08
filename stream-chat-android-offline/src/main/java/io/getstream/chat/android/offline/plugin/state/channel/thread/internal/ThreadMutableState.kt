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

package io.getstream.chat.android.offline.plugin.state.channel.thread.internal

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.extensions.wasCreatedAfterOrAt
import io.getstream.chat.android.offline.plugin.state.channel.internal.ChannelMutableState
import io.getstream.chat.android.offline.plugin.state.channel.thread.ThreadState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class ThreadMutableState(
    override val parentId: String,
    private val channelMutableState: ChannelMutableState,
    scope: CoroutineScope,
) : ThreadState {

    internal val _loadingOlderMessages = MutableStateFlow(false)
    internal val _endOfOlderMessages = MutableStateFlow(false)
    internal val _oldestInThread: MutableStateFlow<Message?> = MutableStateFlow(null)

    override val oldestInThread: StateFlow<Message?> = _oldestInThread

    internal val threadMessages: Flow<List<Message>> =
        channelMutableState.messageList.map { messageList -> messageList.filter { it.id == parentId || it.parentId == parentId } }
    internal val sortedVisibleMessages: StateFlow<List<Message>> = threadMessages.map { threadMessages ->
        threadMessages.sortedBy { m -> m.createdAt ?: m.createdLocallyAt }
            .filter {
                channelMutableState.hideMessagesBefore == null ||
                    it.wasCreatedAfterOrAt(channelMutableState.hideMessagesBefore)
            }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val messages: StateFlow<List<Message>> = sortedVisibleMessages
    override val loadingOlderMessages: StateFlow<Boolean> = _loadingOlderMessages
    override val endOfOlderMessages: StateFlow<Boolean> = _endOfOlderMessages
}

internal fun ThreadState.toMutableState(): ThreadMutableState = this as ThreadMutableState
