package com.getstream.sdk.chat.utils.extensions

import com.getstream.sdk.chat.viewmodel.messages.getCreatedAtOrNull
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.common.state.MessageFooterVisibility
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Decides if we need to show the message footer (timestamp) below the message.
 *
 * @param message The current message for which we are checking whether we need to show the footer for.
 * @param isBottomMessageInGroup Is the message at the bottom of the group.
 * @param nextMessage The message that comes after the current message. Depending on it and [MessageFooterVisibility] we will show/hide the footer.
 */
@InternalStreamChatApi
public fun MessageFooterVisibility.shouldShowMessageFooter(
    message: Message,
    isBottomMessageInGroup: Boolean,
    nextMessage: Message?,
): Boolean {
    if (nextMessage == null && this != MessageFooterVisibility.Never) return true
    return when (this) {
        MessageFooterVisibility.Always -> true
        MessageFooterVisibility.LastInGroup -> isBottomMessageInGroup
        MessageFooterVisibility.Never -> false
        is MessageFooterVisibility.WithTimeDifference -> {
            when {
                isBottomMessageInGroup -> true
                message.isDeleted() -> false
                message.user != nextMessage?.user
                    || nextMessage.isDeleted()
                    || (nextMessage.getCreatedAtOrNull()?.time ?: 0) -
                    (message.getCreatedAtOrNull()?.time ?: 0) >
                    timeDifferenceMillis -> true
                else -> false
            }
        }
    }
}