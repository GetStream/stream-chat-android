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
import io.getstream.chat.android.offline.plugin.state.channel.thread.ThreadState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class ThreadMutableStateImpl(
    override val parentId: String,
    scope: CoroutineScope,
) : ThreadMutableState {
    private val _messages = MutableStateFlow(emptyMap<String, Message>())
    private val _loading = MutableStateFlow(false)
    private val _loadingOlderMessages = MutableStateFlow(false)
    private val _endOfOlderMessages = MutableStateFlow(false)
    private val _oldestInThread: MutableStateFlow<Message?> = MutableStateFlow(null)

    /** raw version of messages. */
    override var rawMessages: Map<String, Message>
        get() = _messages.value
        set(value) { _messages.value = value }

    /** Sorted version of messages. */
    override val sortedMessages: StateFlow<List<Message>> = _messages
        .map { it.values.toList() }
        .map { threadMessages ->
            threadMessages.sortedBy { m -> m.createdAt ?: m.createdLocallyAt }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val messages: StateFlow<List<Message>> = sortedMessages
    override val loading: StateFlow<Boolean> = _loading
    override val loadingOlderMessages: StateFlow<Boolean> = _loadingOlderMessages
    override val endOfOlderMessages: StateFlow<Boolean> = _endOfOlderMessages
    override val oldestInThread: StateFlow<Message?> = _oldestInThread

    override fun setLoading(isLoading: Boolean) {
        _loading.value = isLoading
    }

    override fun setLoadingOlderMessages(isLoading: Boolean) {
        _loadingOlderMessages.value = isLoading
    }

    override fun setEndOfOlderMessages(isEnd: Boolean) {
        _endOfOlderMessages.value = isEnd
    }

    override fun setOldestInThread(message: Message?) {
        _oldestInThread.value = message
    }
}

internal fun ThreadState.toMutableState(): ThreadMutableState = this as ThreadMutableState
