package io.getstream.chat.android.ui.message.list.reactions.view

/**
 * Determines the orientation of the reaction bubble inside message options user reactions.
 *
 * @param value The int value of the enum used for xml attributes.
 */
public enum class ReactionBubbleOrientation(public val value: Int) {
    /**
     * Aligns the reaction to the start of the user avatar.
     */
    START(0),

    /**
     * Aligns the reaction to the end of user avatar.
     */
    END(1),

    /**
     * Aligns the reaction to the end of user avatar if the reaction is from the current user otherwise aligns it
     * to the start of the avatar.
     */
    BY_USER(2),

    /**
     *
     */
    BY_USER_INVERTED(3)
}

public fun Int.getUserReactionOrientation(): ReactionBubbleOrientation {
    return ReactionBubbleOrientation.values().firstOrNull { it.value == this } ?: error("No such alignment")
}

/**
 * Determines if the user reaction should be aligned to start or end.
 *
 * @param isMine Is the reaction the current users reaction.
 *
 * @return If the reaction is aligned to the start or not.
 */
public fun ReactionBubbleOrientation.isOrientedTowardsStart(isMine: Boolean): Boolean {
    return this == ReactionBubbleOrientation.START
        || (isMine && this == ReactionBubbleOrientation.BY_USER)
        || (!isMine && this == ReactionBubbleOrientation.BY_USER_INVERTED)
}