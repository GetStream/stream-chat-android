package io.getstream.chat.android.ui.messages.reactions

import io.getstream.chat.android.client.models.Reaction

internal data class ReactionItem(
    val reaction: Reaction,
    val isMine: Boolean,
)
