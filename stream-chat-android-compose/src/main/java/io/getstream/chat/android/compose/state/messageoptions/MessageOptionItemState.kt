package io.getstream.chat.android.compose.state.messageoptions

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import io.getstream.chat.android.common.state.MessageAction

/**
 * UI representation of a Message option, when the user selects a message in the list.
 *
 * @param title The title to represent the action.
 * @param titleColor The color of the title text.
 * @param iconPainter The icon to represent the action.
 * @param iconColor The color of the icon.
 * @param action The [MessageAction] the option represents.
 */
public class MessageOptionItemState(
    @StringRes public val title: Int,
    public val titleColor: Color,
    public val iconPainter: Painter,
    public val iconColor: Color,
    public val action: MessageAction,
)
