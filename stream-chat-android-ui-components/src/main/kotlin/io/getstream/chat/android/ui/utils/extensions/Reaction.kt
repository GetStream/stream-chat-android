package io.getstream.chat.android.ui.utils.extensions

import io.getstream.chat.android.client.models.Reaction

internal fun Reaction.isMine(): Boolean {
    return user?.isCurrentUser() ?: false
}

internal fun Reaction.isMineReactionOfType(reactionType: String): Boolean {
    return isMine() && type == reactionType
}
