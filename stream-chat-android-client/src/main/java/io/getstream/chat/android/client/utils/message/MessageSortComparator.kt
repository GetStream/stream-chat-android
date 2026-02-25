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

package io.getstream.chat.android.client.utils.message

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Message

/**
 * A [Comparator] for sorting messages in ascending chronological order.
 *
 * Sorting strategy:
 * - **Two messages from the same user, both with a [Message.createdLocallyAt]**: sorted by
 *   [Message.createdLocallyAt]. Because [Message.createdLocallyAt] is only assigned when the
 *   current user prepares a message for sending, this condition is met exclusively for the
 *   current user's own messages. Sorting by local send time preserves the exact order in which
 *   the messages were composed, regardless of network round-trip latency or the server-side
 *   timestamp assigned on confirmation. Unconfirmed messages (no [Message.createdAt] yet) are also
 *   naturally handled by this rule.
 * - **All other pairs** (different users, or messages without a local timestamp): sorted by
 *   [Message.createdAt], falling back to [Message.createdLocallyAt] when the server timestamp
 *   is not yet available. This ensures that messages from users whose device clocks are skewed
 *   are placed at the correct position in the conversation timeline.
 */
@InternalStreamChatApi
public object MessageSortComparator : Comparator<Message> {
    override fun compare(m1: Message, m2: Message): Int {
        return if (m1.user.id == m2.user.id && m1.createdLocallyAt != null && m2.createdLocallyAt != null) {
            // Same user, both locally timestamped: preserve local send order.
            m1.createdLocallyAt!!.compareTo(m2.createdLocallyAt!!)
        } else {
            // Cross-user or no local timestamp: server time is the authority.
            compareValues(m1.createdAt ?: m1.createdLocallyAt, m2.createdAt ?: m2.createdLocallyAt)
        }
    }
}
