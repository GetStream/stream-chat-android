package io.getstream.chat.android.compose.state.userreactions

import androidx.compose.ui.graphics.painter.Painter
import io.getstream.chat.android.client.models.User

/**
 * UI representation of user reaction.
 *
 * @param user The user who left the reaction.
 * @param painter The icon of the reaction.
 * @param type The string representation of the reaction.
 */
public data class UserReactionItemState(
    public val user: User,
    public val painter: Painter,
    public val type: String,
)
