package io.getstream.chat.android.compose.state.messages.reaction

import androidx.compose.ui.graphics.painter.Painter

/**
 * UI representation of reactions.
 *
 * @param drawable The ImageVector that's shown in the icon.
 * @param isSelected If the option is selected or not (already reacted with it).
 * @param type The String representation of the reaction, for the API. Can be any of: ["like", "love", "haha", "wow", "sad"].
 *              TODO TODO TODO: is this still valid?
 */
public class ReactionOption(
    public val drawable: Painter,
    public val isSelected: Boolean,
    public val type: String,
)
