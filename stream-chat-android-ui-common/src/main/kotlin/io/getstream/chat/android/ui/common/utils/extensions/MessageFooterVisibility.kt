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

package io.getstream.chat.android.ui.common.utils.extensions

import io.getstream.chat.android.client.extensions.getCreatedAtOrDefault
import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.list.MessageFooterVisibility

/**
 * Decides if we need to show the message footer (timestamp) below the message.
 *
 * @param message The current message for which we are checking whether we need to show the footer for.
 * @param isLastMessageInGroup Is the message at the bottom of the group.
 * @param nextMessage The message that comes after the current message.
 * Depending on it and [MessageFooterVisibility] we will show/hide the footer.
 *
 * @return If the message footer should be visible or not.
 */
@InternalStreamChatApi
@Suppress("ReturnCount")
public fun MessageFooterVisibility.shouldShowMessageFooter(
    message: Message,
    isLastMessageInGroup: Boolean,
    nextMessage: Message?,
): Boolean {
    if (nextMessage == null && this != MessageFooterVisibility.Never) return true
    if (message.messageTextUpdatedAt != null && this != MessageFooterVisibility.Never) return true
    return when (this) {
        MessageFooterVisibility.Always -> true
        MessageFooterVisibility.LastInGroup -> isLastMessageInGroup
        MessageFooterVisibility.Never -> false
        is MessageFooterVisibility.WithTimeDifference -> isFooterVisibleWithTimeDifference(
            message = message,
            nextMessage = nextMessage,
            isLastMessageInGroup = isLastMessageInGroup,
            timeDifferenceMillis = timeDifferenceMillis,
        )
    }
}

/**
 * @param message The current [Message].
 * @param nextMessage The next [Message] in the list if there is one.
 * @param isLastMessageInGroup If the message is the last in group of messages.
 * @param timeDifferenceMillis The time difference between next and current message.
 *
 * @return Whether the footer should be visible or not.
 */
private fun isFooterVisibleWithTimeDifference(
    message: Message,
    nextMessage: Message?,
    isLastMessageInGroup: Boolean,
    timeDifferenceMillis: Long,
): Boolean {
    return when {
        isLastMessageInGroup -> true
        message.isDeleted() -> false
        message.user.id != nextMessage?.user?.id ||
            nextMessage.isDeleted() ||
            (nextMessage.getCreatedAtOrDefault(NEVER).time) -
            (message.getCreatedAtOrDefault(NEVER).time) >
            timeDifferenceMillis -> true
        else -> false
    }
}
