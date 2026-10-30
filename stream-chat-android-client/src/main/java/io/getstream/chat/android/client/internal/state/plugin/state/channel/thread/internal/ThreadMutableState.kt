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

package io.getstream.chat.android.client.internal.state.plugin.state.channel.thread.internal

import io.getstream.chat.android.client.api.state.ThreadState
import io.getstream.chat.android.client.extensions.getCreatedAtOrNull
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.client.utils.message.isLocalOnly
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.TypingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicInteger

internal class ThreadMutableState(
    override val parentId: String,
    scope: CoroutineScope,
) : ThreadState {
    private var _messages: MutableStateFlow<Map<String, Message>>? = MutableStateFlow(emptyMap())
    private var _loading: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _endOfOlderMessages: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _endOfNewerMessages: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _oldestInThread: MutableStateFlow<Message?>? = MutableStateFlow(null)
    private var _newestInThread: MutableStateFlow<Message?>? = MutableStateFlow(null)
    private val _typing = MutableStateFlow(TypingEvent(channelId = "", users = emptyList()))

    val rawMessage: StateFlow<Map<String, Message>> = _messages!!
    override val messages: StateFlow<List<Message>> = rawMessage
        .map { it.values }
        .map { threadMessages -> threadMessages.sortedBy { it.getCreatedAtOrNull() } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())
    override val loading: StateFlow<Boolean> = _loading!!
    override val endOfOlderMessages: StateFlow<Boolean> = _endOfOlderMessages!!
    override val endOfNewerMessages: StateFlow<Boolean> = _endOfNewerMessages!!
    override val oldestInThread: StateFlow<Message?> = _oldestInThread!!
    override val newestInThread: StateFlow<Message?> = _newestInThread!!
    override val typing: StateFlow<TypingEvent> = _typing

    @Volatile
    override var isJumpPage: Boolean = false

    private val _heldReplies = MutableStateFlow<Map<String, Message>>(emptyMap())
    private val _replaceCount = AtomicInteger()

    /** How many times the loaded replies were replaced by a new page. */
    val replaceCount: Int
        get() = _replaceCount.get()

    /** New replies that arrived while a jumped-to page without the newest replies was shown. */
    val heldReplies: Map<String, Message>
        get() = _heldReplies.value

    /**
     * Retrieves the parent message of the thread.
     */
    val parentMessage: Message?
        get() = _messages?.value?.get(parentId)

    fun setTyping(typingEvent: TypingEvent) {
        _typing.value = typingEvent
    }

    fun setLoading(isLoading: Boolean) {
        _loading?.value = isLoading
    }

    fun setEndOfOlderMessages(isEnd: Boolean) {
        _endOfOlderMessages?.value = isEnd
    }

    fun setOldestInThread(message: Message?) {
        _oldestInThread?.value = message
    }

    fun setEndOfNewerMessages(isEnd: Boolean) {
        _endOfNewerMessages?.value = isEnd
    }

    fun setNewestInThread(message: Message?) {
        _newestInThread?.value = message
    }

    fun deleteMessage(message: Message) {
        _messages?.update { it - message.id }
    }

    fun upsertMessages(messages: List<Message>) {
        _messages?.update { loaded -> loaded + (messages.associateBy(Message::id) - loaded.deletedIds()) }
    }

    /**
     * Replaces the loaded replies in one atomic step. The parent message and the local-only replies
     * (not yet on the server) are always kept.
     *
     * @param replies Computes the new replies from the loaded messages. It can run more than once.
     */
    fun setReplies(replies: (loaded: Map<String, Message>) -> List<Message>) {
        _messages?.update { loaded ->
            loaded.filterValues { it.id == parentId || it.isLocalOnly() } + replies(loaded).associateBy(Message::id)
        }
        _replaceCount.incrementAndGet()
    }

    fun holdReply(message: Message) {
        _heldReplies.update { it + (message.id to message) }
    }

    fun removeHeldReply(messageId: String) {
        _heldReplies.update { it - messageId }
    }

    /** Returns the held replies and clears them in one atomic step. */
    fun takeHeldReplies(): List<Message> = _heldReplies.getAndUpdate { emptyMap() }.values.toList()

    /**
     * Updates the poll object related to the parent message of the thread.
     * Note: This is relevant only for the parent message, as polls cannot be added to replies.
     *
     * @param poll The updated poll object.
     */
    fun updateParentMessagePoll(poll: Poll?) {
        val parent = parentMessage ?: return
        val parentPoll = parent.poll ?: return
        // Allow deleting poll (when poll == null), or overriding the poll (when poll.id == parent.poll.id)
        if (poll == null || poll.id == parentPoll.id) {
            val updatedParent = parent.copy(poll = poll)
            upsertMessages(listOf(updatedParent))
        }
    }

    fun destroy() {
        _messages = null
        _loading = null
        _endOfOlderMessages = null
        _oldestInThread = null
    }
}

private fun Map<String, Message>.deletedIds(): Set<String> =
    values.filter { it.isDeleted() }.mapTo(mutableSetOf(), Message::id)
