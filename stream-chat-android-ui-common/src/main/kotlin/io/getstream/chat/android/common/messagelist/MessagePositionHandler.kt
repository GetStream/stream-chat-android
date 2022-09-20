package io.getstream.chat.android.common.messagelist

import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.common.state.messagelist.MessagePosition

/**
 * A handler to determine the position of a message inside a group.
 */
public fun interface MessagePositionHandler {
    /**
     * Determines the position of a message inside a group.
     *
     * @param prevMessage The previous [Message] in the list.
     * @param message The current [Message] in the list.
     * @param nextMessage The next [Message] in the list.
     * @param isAfterDateSeparator If a date separator was added before the current [Message].
     *
     * @return The position of the current message inside the group.
     */
    public fun handleMessagePosition(
        prevMessage: Message?,
        message: Message,
        nextMessage: Message?,
        isAfterDateSeparator: Boolean,
    ): List<MessagePosition>

    public companion object {
        /**
         * The default implementation of the [MessagePositionHandler] interface which can be taken
         * as a reference when implementing a custom one.
         *
         * @return The default implementation of [MessagePositionHandler].
         */
        internal fun defaultHandler(): MessagePositionHandler {
            return MessagePositionHandler {
                    prevMessage: Message?,
                    message: Message,
                    nextMessage: Message?,
                    isAfterDateSeparator: Boolean,
                ->
                val prevUser = prevMessage?.user
                val user = message.user
                val nextUser = nextMessage?.user

                fun Message.isServerMessage(): Boolean {
                    return type == ModelType.message_system || type == ModelType.message_error
                }

                mutableListOf<MessagePosition>().apply {
                    if (prevMessage == null || prevUser != user || prevMessage.isServerMessage() || isAfterDateSeparator) {
                        add(MessagePosition.TOP)
                    }
                    if (prevMessage != null && nextMessage != null && prevUser == user && nextUser == user) {
                        add(MessagePosition.MIDDLE)
                    }
                    if (nextMessage == null || nextUser != user || nextMessage.isServerMessage()) {
                        add(MessagePosition.BOTTOM)
                    }
                    if (isEmpty()) add(MessagePosition.NONE)
                }
            }
        }
    }
}