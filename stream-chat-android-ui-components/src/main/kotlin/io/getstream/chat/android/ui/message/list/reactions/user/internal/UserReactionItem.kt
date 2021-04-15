package io.getstream.chat.android.ui.message.list.reactions.user.internal

import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.SupportedReactions

internal data class UserReactionItem(
    val user: User,
    val reaction: Reaction,
    val isMine: Boolean,
    private val reactionDrawable: SupportedReactions.ReactionDrawable,
) {
    val drawable = if (isMine) {
        reactionDrawable.activeDrawable
    } else {
        reactionDrawable.inactiveDrawable
    }
}
