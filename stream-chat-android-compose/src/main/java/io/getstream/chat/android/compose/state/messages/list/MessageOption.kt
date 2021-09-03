package io.getstream.chat.android.compose.state.messages.list

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * UI representation of a Message option, when the user selects a message in the list.
 *
 * @param title The title to represent the action.
 * @param titleColor The color of the title text.
 * @param icon The icon to represent the action.
 * @param iconColor The color of the icon.
 * @param action The [MessageAction] the option represents.
 */

public class MessageOption(
    @StringRes public val title: Int,
    public val titleColor: Color,
    public val icon: ImageVector,
    public val iconColor: Color,
    public val action: MessageAction,
)

@Composable
public fun buildMessageOption(
    @StringRes title: Int,
    icon: ImageVector,
    action: MessageAction,
): MessageOption {
    return MessageOption(
        title = title,
        icon = icon,
        action = action,
        titleColor = ChatTheme.colors.textHighEmphasis,
        iconColor = ChatTheme.colors.textLowEmphasis,
    )
}
