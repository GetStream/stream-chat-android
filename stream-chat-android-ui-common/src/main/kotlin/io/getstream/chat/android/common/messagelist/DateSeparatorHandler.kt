package io.getstream.chat.android.common.messagelist

import io.getstream.chat.android.client.models.Message

/**
 * A SAM designed to evaluate if a date separator should be added between messages.
 */
public fun interface DateSeparatorHandler {
    public fun shouldAddDateSeparator(previousMessage: Message?, message: Message): Boolean
}