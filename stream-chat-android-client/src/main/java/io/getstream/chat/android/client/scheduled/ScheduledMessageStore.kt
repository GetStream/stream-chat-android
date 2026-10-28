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

package io.getstream.chat.android.client.scheduled

import io.getstream.chat.android.client.persistance.repository.ScheduledMessageRepository
import io.getstream.chat.android.client.persistance.repository.noop.NoOpScheduledMessageRepository
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Keeps the current user's scheduled messages in memory and in the [ScheduledMessageRepository]. Every change goes
 * through [update], one at a time, so while it is loaded, the repository holds the same messages as the memory.
 */
internal class ScheduledMessageStore {

    private val mutex = Mutex()
    private var repository: ScheduledMessageRepository = NoOpScheduledMessageRepository
    private val _messages = MutableStateFlow<Map<String, LocalScheduledMessage>>(emptyMap())
    private val _scheduledMessages = MutableStateFlow<List<ScheduledMessage>>(emptyList())

    /** The scheduled messages by ID, including the deleted ones that are not removed yet. */
    val messages: StateFlow<Map<String, LocalScheduledMessage>> = _messages

    /** The scheduled messages to show: the pending ones first, then the failed ones, each by date. */
    val scheduledMessages: StateFlow<List<ScheduledMessage>> = _scheduledMessages

    /**
     * Loads the scheduled messages saved in [repository], and saves every later change there. The ones changed before,
     * for example by an event, win over the saved ones and are saved too.
     */
    suspend fun load(repository: ScheduledMessageRepository) = mutex.withLock {
        this.repository = repository
        _messages.value.values.forEach { repository.insertScheduledMessage(it) }
        setMessages(repository.selectScheduledMessages().associateBy(LocalScheduledMessage::id) + _messages.value)
    }

    /**
     * Removes the scheduled messages from memory, and stops saving changes. The repository keeps them.
     */
    suspend fun clear() = mutex.withLock {
        repository = NoOpScheduledMessageRepository
        setMessages(emptyMap())
    }

    /**
     * Replaces the scheduled message with [id] by the result of [transform], which gets the current one, if any.
     * A null result removes it.
     *
     * @return The new scheduled message, or null when it was removed.
     */
    suspend fun update(
        id: String,
        transform: (LocalScheduledMessage?) -> LocalScheduledMessage?,
    ): LocalScheduledMessage? = mutex.withLock {
        val current = _messages.value[id]
        val new = transform(current)
        if (new != current) {
            if (new == null) repository.deleteScheduledMessage(id) else repository.insertScheduledMessage(new)
            setMessages(if (new == null) _messages.value - id else _messages.value + (id to new))
        }
        new
    }

    /**
     * Removes the scheduled messages that match [predicate].
     */
    suspend fun removeAll(predicate: (LocalScheduledMessage) -> Boolean) = mutex.withLock {
        val (removed, kept) = _messages.value.values.partition(predicate)
        removed.forEach { repository.deleteScheduledMessage(it.id) }
        setMessages(kept.associateBy(LocalScheduledMessage::id))
    }

    private fun setMessages(messages: Map<String, LocalScheduledMessage>) {
        _messages.value = messages
        _scheduledMessages.value = messages.values
            .filterNot(LocalScheduledMessage::isDeleted)
            .map(LocalScheduledMessage::toScheduledMessage)
            .sortedWith(
                compareBy<ScheduledMessage> { it.status == ScheduledMessageStatus.Failed }
                    .thenBy(ScheduledMessage::scheduledAt),
            )
    }
}
