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

package io.getstream.chat.android.client.utils.channel

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageType
import java.util.Date

/**
 * Generates the channel id based on the member ids if provided [channelId] is empty.
 * Member-based id should only be used for creating distinct channels.
 * Created member-based id might differ from the one created by the server if the channel already exists
 * and was created with different members order.
 *
 * @param channelId The channel id. ie 123.
 * @param memberIds The list of members' ids.
 *
 * @return [channelId] if not blank or new, member-based id.
 */
@InternalStreamChatApi
public fun generateChannelIdIfNeeded(channelId: String, memberIds: List<String>): String {
    return channelId.ifBlank {
        memberIds.joinToString(prefix = "!members-")
    }
}

/**
 * Calculates the new lastMessageAt date based on the message.
 * Returns the current lastMessageAt if the message should not update it
 * (e.g., shadowed, system message when skipped, or thread reply not shown in channel),
 * or the max of current and message date if it should be updated.
 *
 * @param message The message to check.
 * @param currentLastMessageAt The current lastMessageAt value.
 * @param skipLastMsgUpdateForSystemMsgs Whether to skip system messages when updating lastMessageAt.
 * @return The new lastMessageAt date.
 */
internal fun calculateNewLastMessageAt(
    message: Message,
    currentLastMessageAt: Date?,
    skipLastMsgUpdateForSystemMsgs: Boolean,
): Date? {
    // Skip shadowed messages
    if (message.shadowed) return currentLastMessageAt
    // Skip system messages if config says so
    if (message.type == MessageType.SYSTEM && skipLastMsgUpdateForSystemMsgs) return currentLastMessageAt
    // Skip thread replies not shown in channel
    if (message.parentId != null && !message.showInChannel) return currentLastMessageAt

    val messageDate = message.createdLocallyAt ?: message.createdAt ?: return currentLastMessageAt

    return if (currentLastMessageAt == null || messageDate.after(currentLastMessageAt)) {
        messageDate
    } else {
        currentLastMessageAt
    }
}
