package io.getstream.chat.android.compose.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Contains all the dimens we provide for our components.
 *
 * @param channelItemVerticalPadding The vertical content padding inside channel list item.
 * @param channelItemHorizontalPadding The horizontal content padding inside channel list item.
 * @param channelAvatarSize The size of channel avatar.
 * @param channelInfoUserItemWidth The width of a member tile in the channel info dialog.
 * @param channelInfoUserItemHorizontalPadding The padding inside a member tile in the channel info dialog.
 * @param channelInfoUserItemAvatarSize The size of a member avatar in the channel info dialog.
 */
@Immutable
public data class StreamDimens(
    public val channelItemVerticalPadding: Dp,
    public val channelItemHorizontalPadding: Dp,
    public val channelAvatarSize: Dp,
    public val channelInfoUserItemWidth: Dp,
    public val channelInfoUserItemHorizontalPadding: Dp,
    public val channelInfoUserItemAvatarSize: Dp,
) {
    public companion object {
        public fun defaultDimens(): StreamDimens = StreamDimens(
            channelItemVerticalPadding = 12.dp,
            channelItemHorizontalPadding = 8.dp,
            channelAvatarSize = 40.dp,
            channelInfoUserItemWidth = 80.dp,
            channelInfoUserItemHorizontalPadding = 8.dp,
            channelInfoUserItemAvatarSize = 64.dp,
        )
    }
}
