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
    private var isLoaded = false
    private val removedBeforeLoad = mutableSetOf<String>()
    private val _messages = MutableStateFlow<Map<String, LocalScheduledMessage>>(emptyMap())
    private val _scheduledMessages = MutableStateFlow<List<ScheduledMessage>>(emptyList())

    /** The scheduled messages by ID, including the deleted ones that are not removed yet. */
    val messages: StateFlow<Map<String, LocalScheduledMessage>> = _messages

    /** The scheduled messages to show: the pending ones first, then the failed ones, each by date. */
    val scheduledMessages: StateFlow<List<ScheduledMessage>> = _scheduledMessages

    /**
     * Loads the scheduled messages saved in [repository], and saves every later change there. The changes made before
     * are kept and saved too: the ones removed stay removed, and the ones changed are merged with the saved ones.
     */
    suspend fun load(repository: ScheduledMessageRepository) = mutex.withLock {
        this.repository = repository
        removedBeforeLoad.forEach { repository.deleteScheduledMessage(it) }
        val saved = repository.selectScheduledMessages().associateBy(LocalScheduledMessage::id)
        val changed = _messages.value.mapValues { (id, message) ->
            saved[id]?.let { message.mergedWith(it) } ?: message
        }
        changed.values.forEach { repository.insertScheduledMessage(it) }
        removedBeforeLoad.clear()
        isLoaded = true
        setMessages(saved + changed)
    }

    /**
     * Removes the scheduled messages from memory, and stops saving changes. The repository keeps them.
     */
    suspend fun clear() = mutex.withLock {
        repository = NoOpScheduledMessageRepository
        isLoaded = false
        removedBeforeLoad.clear()
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
            if (new == null) delete(id) else repository.insertScheduledMessage(new)
            setMessages(if (new == null) _messages.value - id else _messages.value + (id to new))
        }
        new
    }

    /**
     * Removes the scheduled message with [id], also when it is saved but not loaded yet.
     */
    suspend fun remove(id: String) = mutex.withLock {
        delete(id)
        setMessages(_messages.value - id)
    }

    /**
     * Removes the scheduled messages that match [predicate].
     */
    suspend fun removeAll(predicate: (LocalScheduledMessage) -> Boolean) = mutex.withLock {
        val (removed, kept) = _messages.value.values.partition(predicate)
        removed.forEach { delete(it.id) }
        setMessages(kept.associateBy(LocalScheduledMessage::id))
    }

    private suspend fun delete(id: String) {
        if (!isLoaded) removedBeforeLoad += id
        repository.deleteScheduledMessage(id)
    }

    /**
     * Merges this message, changed before the load, with the [saved] one. A local change made before wins. A server
     * version received before keeps the saved local changes, and the pending requests of both are kept.
     */
    private fun LocalScheduledMessage.mergedWith(saved: LocalScheduledMessage): LocalScheduledMessage {
        val merged = if (pendingChange != null || saved.pendingChange == null) {
            this
        } else {
            saved.copy(
                channel = channel ?: saved.channel,
                serverStatus = serverStatus,
                publishingFailure = publishingFailure,
                createdAt = createdAt,
            )
        }
        return merged.copy(
            isPendingSendNow = isPendingSendNow || saved.isPendingSendNow,
            isDeleted = isDeleted || saved.isDeleted,
            localFailure = localFailure ?: saved.localFailure,
        )
    }

    private fun setMessages(messages: Map<String, LocalScheduledMessage>) {
        _messages.value = messages
        _scheduledMessages.value = messages.values
            .filterNot { it.isDeleted && it.localFailure == null }
            .map(LocalScheduledMessage::toScheduledMessage)
            .sortedWith(
                compareBy<ScheduledMessage> { it.status == ScheduledMessageStatus.Failed }
                    .thenBy(ScheduledMessage::scheduledAt),
            )
    }
}
