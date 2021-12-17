package io.getstream.chat.android.compose.ui.util

import io.getstream.chat.android.compose.state.messages.MessageAlignment
import io.getstream.chat.android.compose.state.messages.list.MessageItemState

/**
 *  An interface that allows to return the desired horizontal alignment for a particular [MessageItemState].
 */
public fun interface MessageAlignmentProvider {

    /**
     * Returns [MessageAlignment] for a particular [MessageItemState].
     *
     * @param messageItem The message whose data is used to decide which alignment to use.
     * @return The [MessageAlignment] for the provided message.
     */
    public fun provideMessageAlignment(messageItem: MessageItemState): MessageAlignment

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
     * Returns [MessageAlignment] for a particular [MessageItemState].
     *
     * @param messageItem The message whose data is used to decide which alignment to use.
     * @return The [MessageAlignment] for the provided message.
     */
    override fun provideMessageAlignment(messageItem: MessageItemState): MessageAlignment {
        return if (messageItem.isMine) MessageAlignment.End else MessageAlignment.Start
    }
}
