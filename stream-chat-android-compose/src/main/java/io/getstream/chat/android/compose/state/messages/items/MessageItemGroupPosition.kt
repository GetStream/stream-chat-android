package io.getstream.chat.android.compose.state.messages.items

/**
 * Represents the group position of a message, if the message is in a group. Otherwise represented as [None].
 *
 * Used to define the shape of the message as well as other UI styling.
 */
public sealed class MessageItemGroupPosition {

    /**
     * Message that is the first message in the group at the top.
     */
    public object Top : MessageItemGroupPosition()

    /**
     * Message that has another message both at the top and bottom of it.
     */
    public object Middle : MessageItemGroupPosition()

    /**
     * Message that's the last message in the group, at the bottom.
     */
    public object Bottom : MessageItemGroupPosition()

    /**
     * Message that is not in a group.
     */
    public object None : MessageItemGroupPosition()
}
