package io.getstream.chat.android.compose.state.channel.list

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * UI representation of a Channel option, when the user selects a channel in the list.
 *
 * @param title - The title to represent the action.
 * @param titleColor - The color of the title text.
 * @param icon - The icon to represent the action.
 * @param iconColor - The color of the icon.
 * @param action - The [ChannelListAction] the option represents.
 * */
class ChannelOption(
    val title: String,
    val titleColor: Color,
    val icon: ImageVector,
    val iconColor: Color,
    val action: ChannelListAction,
)