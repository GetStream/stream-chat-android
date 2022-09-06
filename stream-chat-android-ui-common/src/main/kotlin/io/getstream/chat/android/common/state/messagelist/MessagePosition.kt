package io.getstream.chat.android.common.state.messagelist

import io.getstream.chat.android.client.models.Message

/**
 * Represents the group position of a message, if the message is in a group. Otherwise represented as [NONE].
 *
 * Used to define the shape of the message as well as other UI styling.
 */
public enum class MessagePosition {
    /**
     * Message that is the first message in the group at the top.
     */
    TOP,
    /**
     * Message that has another message both at the top and bottom of it.
     */
    MIDDLE,
    /**
     * Message that's the last message in the group, at the bottom.
     */
    BOTTOM,
    /**
     * Message that is not in a group.
     */
    NONE
}
