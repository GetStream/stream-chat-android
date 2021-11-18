package io.getstream.chat.android.compose.ui.util

import io.getstream.chat.android.compose.state.messages.items.MessageItem
import io.getstream.chat.android.compose.ui.messages.list.MessageAlignment

/**
 *  An interface that allows to return the desired horizontal alignment for a particular [MessageItem].
 */
public interface MessageAlignmentProvider {

    /**
     * Returns [MessageAlignment] for a particular [MessageItem].
     *
     * @param messageItem The message whose data is used to decide which alignment to use.
     * @return The [MessageAlignment] for the provided message.
     */
    public fun provideMessageAlignment(messageItem: MessageItem): MessageAlignment

    public companion object {
        /**
         * Builds the default message alignment provider.
         *
         * @see [DefaultMessageAlignmentProvider]
         */
        public fun defaultMessageAlignmentProvider(): MessageAlignmentProvider {
            return DefaultMessageAlignmentProvider()
        }
    }
}

/**
 * A simple implementation of [MessageAlignmentProvider] that returns [MessageAlignment.End]
 * for the messages of the current user and [MessageAlignment.Start] for the messages of
 * other users.
 */
private class DefaultMessageAlignmentProvider : MessageAlignmentProvider {

    /**
     * Returns [MessageAlignment] for a particular [MessageItem].
     *
     * @param messageItem The message whose data is used to decide which alignment to use.
     * @return The [MessageAlignment] for the provided message.
     */
    override fun provideMessageAlignment(messageItem: MessageItem): MessageAlignment {
        return if (messageItem.isMine) MessageAlignment.End else MessageAlignment.Start
    }
}
