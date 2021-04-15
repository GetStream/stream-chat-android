package io.getstream.chat.android.ui.message.list.reactions.internal

import io.getstream.chat.android.ui.SupportedReactions

internal data class ReactionItem(
    val type: String,
    val isMine: Boolean,
    val reactionDrawable: SupportedReactions.ReactionDrawable,
)
