package io.getstream.chat.android.common.messagelist

import com.getstream.sdk.chat.utils.extensions.getCreatedAtOrThrow
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * A SAM designed to evaluate if a date separator should be added between messages.
 */
public fun interface DateSeparatorHandler {
    public fun shouldAddDateSeparator(previousMessage: Message?, message: Message): Boolean

    @InternalStreamChatApi
    public companion object {
        @InternalStreamChatApi
        public fun getDefaultDateSeparator(separatorTime: Long, showDateSeparators: Boolean = true):
            DateSeparatorHandler = DateSeparatorHandler { previousMessage, message ->
            if (!showDateSeparators) {
                false
            } else if (previousMessage == null) {
                true
            } else {
                val timeDifference = message.getCreatedAtOrThrow().time - previousMessage.getCreatedAtOrThrow().time
                timeDifference > separatorTime
            }
        }

        @InternalStreamChatApi
        public fun getDefaultThreadDateSeparator(separatorTime: Long, showDateSeparators: Boolean = true):
            DateSeparatorHandler = DateSeparatorHandler { previousMessage, message ->
            if (!showDateSeparators) {
                false
            } else if (previousMessage == null) {
                false
            } else {
                (message.getCreatedAtOrThrow().time - previousMessage.getCreatedAtOrThrow().time) >
                    separatorTime
            }
        }
    }
}