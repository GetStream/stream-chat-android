package io.getstream.chat.android.ui.messages.reactions

import io.getstream.chat.android.client.models.Reaction

public fun interface ReactionClickListener {
    public fun onReactionClick(reaction: Reaction)
}
