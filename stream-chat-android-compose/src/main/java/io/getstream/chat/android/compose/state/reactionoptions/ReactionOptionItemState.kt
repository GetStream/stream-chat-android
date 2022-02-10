package io.getstream.chat.android.compose.state.reactionoptions

import androidx.compose.ui.graphics.painter.Painter

/**
 * UI representation of reactions.
 *
 * @param painter The icon of the option.
 * @param type The String representation of the reaction, for the API.
 */
public data class ReactionOptionItemState(
    public val painter: Painter,
    public val type: String,
)
