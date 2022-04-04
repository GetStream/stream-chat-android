package io.getstream.chat.android.compose.state.messages

/**
 * Represents the behavior of deleted messages in the list, if they should show for some users or no one.
 */
public enum class DeletedMessagesVisibility {
    /**
     * No deleted messages are visible.
     */
    NONE,

    /**
     * Only the deleted messages that the current user owns are visible.
     */
    OWN,

    /**
     * All deleted messages are visible, regardless of the owner.
     */
    ALL
}
