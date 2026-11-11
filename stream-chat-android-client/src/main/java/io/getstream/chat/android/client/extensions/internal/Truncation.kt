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

package io.getstream.chat.android.client.extensions.internal

import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.NotificationChannelTruncatedEvent
import io.getstream.chat.android.client.utils.message.isLocalOnly
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Message
import java.util.Date

/**
 * The date before which the truncation removed messages. The truncation's system message is created just after
 * it, while the event itself is created later.
 */
internal val ChannelTruncatedEvent.truncatedBefore: Date
    get() = channel.truncatedAt ?: createdAt

/**
 * The date before which the truncation removed messages.
 */
internal val NotificationChannelTruncatedEvent.truncatedBefore: Date
    get() = channel.truncatedAt ?: createdAt

/**
 * Whether the message outlives a truncation at [truncatedAt]. Compares the server's creation date, as the backend
 * does, because the local one follows the device clock; a message the server has not created yet is kept.
 */
internal fun Message.survivesTruncationAt(truncatedAt: Date): Boolean = createdAt?.before(truncatedAt) != true

/**
 * The read after a truncation at [truncatedAt] left the messages with [remainingMessageIds], recalculated as the
 * backend does: the unread count is capped at the remaining messages, and the last read message is cleared unless it
 * was read after the truncation and still exists.
 */
internal fun ChannelUserRead.afterTruncationAt(truncatedAt: Date, remainingMessageIds: Set<String>): ChannelUserRead {
    val keepsLastReadMessage = lastRead.after(truncatedAt) && lastReadMessageId in remainingMessageIds
    return copy(
        unreadMessages = minOf(unreadMessages, remainingMessageIds.size),
        lastReadMessageId = lastReadMessageId.takeIf { keepsLastReadMessage },
    )
}

/**
 * The ids of the messages in [pages] the server counts, leaving out local-only ones, as the remaining messages a
 * truncation's recalculated reads are capped at.
 */
internal fun serverMessageIds(vararg pages: Collection<Message>): Set<String> =
    pages.flatMapTo(HashSet()) { page -> page.filterNot(Message::isLocalOnly).map(Message::id) }

/** The channel with its reads recalculated for a truncation at [truncatedAt], from the messages it holds. */
internal fun Channel.withReadsAfterTruncationAt(truncatedAt: Date): Channel {
    val remainingMessageIds = serverMessageIds(messages.filter { it.survivesTruncationAt(truncatedAt) })
    return copy(read = read.map { it.afterTruncationAt(truncatedAt, remainingMessageIds) })
}

/** The channel without the messages, pinned ones included, its truncation removed. */
internal fun Channel.withoutTruncatedMessages(): Channel = truncatedAt?.let { cutoff ->
    copy(
        messages = messages.filter { it.survivesTruncationAt(cutoff) },
        pinnedMessages = pinnedMessages.filter { it.survivesTruncationAt(cutoff) },
    )
} ?: this
