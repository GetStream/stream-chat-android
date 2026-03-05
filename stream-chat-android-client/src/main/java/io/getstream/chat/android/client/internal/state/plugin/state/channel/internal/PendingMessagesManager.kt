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

package io.getstream.chat.android.client.internal.state.plugin.state.channel.internal

import io.getstream.chat.android.client.extensions.getCreatedAtOrNull
import io.getstream.chat.android.client.internal.state.utils.internal.combineStates
import io.getstream.chat.android.models.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.Date

/**
 * Encapsulates all pending-message state and logic for a channel.
 *
 * Pending messages are messages awaiting moderation approval, kept separate from regular messages
 * and merged into the public message list at their natural position in the timeline.
 *
 * The feature is gated by [setEnabled]: when disabled, [pendingMessagesInRange] always emits an
 * empty list and all buffered state is cleared.
 */
internal class PendingMessagesManager {

    private val _enabled = MutableStateFlow(false)
    private val _pendingMessages = MutableStateFlow<List<Message>>(emptyList())
    private val _dateRange = MutableStateFlow(DateRange(oldest = null, newest = null))

    /**
     * Filtered pending messages ready for merging into the regular message list.
     * Returns an empty list when disabled or no pending messages fall within the loaded window.
     */
    val pendingMessagesInRange: StateFlow<List<Message>> = combineStates(
        _enabled,
        _pendingMessages,
        _dateRange,
    ) { enabled, pending, range ->
        if (!enabled || pending.isEmpty()) return@combineStates emptyList()
        pending.filter { msg ->
            val date = msg.getCreatedAtOrNull() ?: Date(0)
            (range.oldest == null || date >= range.oldest) &&
                (range.newest == null || date <= range.newest)
        }
    }

    /**
     * Enables or disables the pending-messages feature. Clears all buffered state before
     * disabling so no stale data leaks through [pendingMessagesInRange].
     */
    fun setEnabled(enabled: Boolean) {
        if (!enabled) clear()
        _enabled.value = enabled
    }

    /**
     * Replaces the pending messages list. The server is authoritative — every channel response
     * returns the latest 100 pending messages sorted by createdAt ASC, so we always replace.
     */
    fun setPendingMessages(messages: List<Message>) {
        _pendingMessages.value = messages.sortedWith(MESSAGE_COMPARATOR)
    }

    /**
     * Removes a single pending message by ID. Called when a pending message is promoted to a
     * regular message (message.new event) or deleted (message.deleted event).
     */
    fun removePendingMessage(id: String) {
        _pendingMessages.update { current ->
            if (current.none { it.id == id }) {
                current
            } else {
                current.filterNot { it.id == id }
            }
        }
    }

    /**
     * Advances the floor of the date range to the oldest message date if it is older than the
     * current floor. The floor only ever moves backward.
     */
    fun advanceOldestLoadedDate(messages: List<Message>) {
        val newOldest = messages.firstOrNull()?.getCreatedAtOrNull() ?: return
        _dateRange.update { current ->
            if (current.oldest == null || newOldest < current.oldest) {
                current.copy(oldest = newOldest)
            } else {
                current
            }
        }
    }

    /**
     * Sets the ceiling of the date range to the given date. Pass null to remove the ceiling
     * (i.e. when viewing the latest messages).
     */
    fun setNewestLoadedDate(date: Date?) {
        _dateRange.update { it.copy(newest = date) }
    }

    /**
     * Advances the ceiling of the date range forward if [date] is newer than the current ceiling.
     * Used when loading newer pages while still not at the latest messages.
     */
    fun advanceNewestLoadedDate(date: Date?) {
        if (date == null) return
        _dateRange.update { current ->
            if (current.newest == null || date > current.newest) {
                current.copy(newest = date)
            } else {
                current
            }
        }
    }

    /** Clears all buffered state. Called by [setEnabled] when disabling and by [ChannelStateImpl.destroy]. */
    fun reset() = clear()

    private fun clear() {
        _pendingMessages.value = emptyList()
        _dateRange.value = DateRange(oldest = null, newest = null)
    }

    private data class DateRange(val oldest: Date?, val newest: Date?)

    private companion object {
        private val MESSAGE_COMPARATOR: Comparator<Message> = compareBy { it.getCreatedAtOrNull() }
    }
}
