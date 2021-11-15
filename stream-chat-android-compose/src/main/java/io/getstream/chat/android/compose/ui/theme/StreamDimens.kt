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
 * @param attachmentsContentImageWidth The width of
 * @param attachmentsContentImageHeight The height of
 * @param attachmentsContentGiphyWidth The with of
 * @param attachmentsContentGiphyHeight The height of
 * @param attachmentsContentLinkWidth The with of link
 * @param attachmentsContentFileWidth The width of file
 * @param attachmentsContentFileUploadWidth The Width of
 */
@Immutable
public data class StreamDimens(
    public val channelItemVerticalPadding: Dp,
    public val channelItemHorizontalPadding: Dp,
    public val channelAvatarSize: Dp,
    public val channelInfoUserItemWidth: Dp,
    public val channelInfoUserItemHorizontalPadding: Dp,
    public val channelInfoUserItemAvatarSize: Dp,
    public val attachmentsContentImageWidth: Dp,
    public val attachmentsContentImageHeight: Dp,
    public val attachmentsContentGiphyWidth: Dp,
    public val attachmentsContentGiphyHeight: Dp,
    public val attachmentsContentLinkWidth: Dp,
    public val attachmentsContentFileWidth: Dp,
    public val attachmentsContentFileUploadWidth: Dp,
) {
    public companion object {
        public fun defaultDimens(): StreamDimens = StreamDimens(
            channelItemVerticalPadding = 12.dp,
            channelItemHorizontalPadding = 8.dp,
            channelAvatarSize = 40.dp,
            channelInfoUserItemWidth = 80.dp,
            channelInfoUserItemHorizontalPadding = 8.dp,
            channelInfoUserItemAvatarSize = 64.dp,
            attachmentsContentImageWidth = 250.dp,
            attachmentsContentImageHeight = 200.dp,
            attachmentsContentGiphyWidth = 250.dp,
            attachmentsContentGiphyHeight = 200.dp,
            attachmentsContentLinkWidth = 250.dp,
            attachmentsContentFileWidth = 250.dp,
            attachmentsContentFileUploadWidth = 250.dp,
        )
    }
}
