package io.getstream.chat.android.common.messagelist

import com.getstream.sdk.chat.utils.extensions.getCreatedAtOrThrow
import io.getstream.chat.android.client.models.Message

/**
 * A SAM designed to evaluate if a date separator should be added between messages.
 */
public fun interface DateSeparatorHandler {
    public fun shouldAddDateSeparator(previousMessage: Message?, message: Message): Boolean

    public companion object {
        public fun getDefaultDateSeparator(separatorTimeMillis: Long = DateSeparatorDefaultHourThreshold):
            DateSeparatorHandler = DateSeparatorHandler { previousMessage, message ->
            if (previousMessage == null) {
                true
            } else {
                val timeDifference = message.getCreatedAtOrThrow().time - previousMessage.getCreatedAtOrThrow().time
                timeDifference > separatorTimeMillis
            }
        }

        public fun getDefaultThreadDateSeparator(separatorTimeMillis: Long = DateSeparatorDefaultHourThreshold):
            DateSeparatorHandler = DateSeparatorHandler { previousMessage, message ->
            if (previousMessage == null) {
                false
            } else {
                (message.getCreatedAtOrThrow().time - previousMessage.getCreatedAtOrThrow().time) >
                    separatorTimeMillis
            }
        }

        /**
         * The default threshold for showing date separators. If the message difference in millis is equal to this
         * number, then we show a separator, if it's enabled in the list.
         */
        private const val DateSeparatorDefaultHourThreshold: Long = 4 * 60 * 60 * 1000
    }
}