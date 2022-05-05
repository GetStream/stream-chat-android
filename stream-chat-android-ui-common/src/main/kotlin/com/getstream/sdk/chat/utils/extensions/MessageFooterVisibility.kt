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

package com.getstream.sdk.chat.utils.extensions

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.common.state.MessageFooterVisibility
import io.getstream.chat.android.core.internal.InternalStreamChatApi

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
public fun MessageFooterVisibility.shouldShowMessageFooter(
    message: Message,
    isLastMessageInGroup: Boolean,
    nextMessage: Message?,
): Boolean {
    if (nextMessage == null && this != MessageFooterVisibility.Never) return true
    return when (this) {
        MessageFooterVisibility.Always -> true
        MessageFooterVisibility.LastInGroup -> isLastMessageInGroup
        MessageFooterVisibility.Never -> false
        is MessageFooterVisibility.WithTimeDifference -> {
            when {
                isLastMessageInGroup -> true
                message.isDeleted() -> false
                message.user != nextMessage?.user ||
                    nextMessage.isDeleted() ||
                    (nextMessage.getCreatedAtOrNull()?.time ?: 0) -
                    (message.getCreatedAtOrNull()?.time ?: 0) >
                    timeDifferenceMillis -> true
                else -> false
            }
        }
    }
}
