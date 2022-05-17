package io.getstream.chat.android.compose.ui.util.extensions.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.state.userreactions.UserReactionItemState

/**
 * @return if the current reaction is owned by current user or not.
 */
internal fun UserReactionItemState.isMine(): Boolean {
    return user.id == ChatClient.instance().getCurrentUser()?.id
}