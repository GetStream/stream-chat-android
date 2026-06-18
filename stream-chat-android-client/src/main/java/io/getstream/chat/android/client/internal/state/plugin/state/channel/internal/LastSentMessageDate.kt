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

import io.getstream.chat.android.models.Message
import java.util.Date

/**
 * Returns the more recent of two nullable dates, or `null` when both are `null`.
 */
internal fun latestOf(first: Date?, second: Date?): Date? = when {
    first == null -> second
    second == null -> first
    else -> maxOf(first, second)
}

/**
 * Returns the creation date of this message when it is a thread-only reply sent by [currentUserId],
 * or `null` otherwise.
 *
 * The channel cooldown counts thread replies the same as channel messages, but thread-only replies
 * (`parentId != null && !showInChannel`) are excluded from the channel message list, so the cooldown
 * derivation tracks them separately. Shadowed messages are excluded to match the channel message
 * date derivation.
 */
internal fun Message.ownThreadReplyDate(currentUserId: String): Date? = when {
    user.id != currentUserId -> null
    parentId == null || showInChannel -> null
    shadowed -> null
    else -> createdLocallyAt ?: createdAt
}

/**
 * Returns the creation date of the most recent thread-only reply sent by [currentUserId] in this
 * collection, or `null` when there is none.
 */
internal fun Collection<Message>.latestOwnThreadReplyDate(currentUserId: String?): Date? {
    currentUserId ?: return null
    return asSequence()
        .mapNotNull { it.ownThreadReplyDate(currentUserId) }
        .maxOrNull()
}
