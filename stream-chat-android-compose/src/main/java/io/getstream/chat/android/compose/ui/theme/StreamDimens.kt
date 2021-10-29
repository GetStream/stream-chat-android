package io.getstream.chat.android.compose.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Contains all the dimens we provide for our components.
 *
 * @param channelItemVerticalPadding The vertical content padding inside channel list item.
 * @param channelItemHorizontalPadding The horizontal content padding inside channel list item.
 */
@Immutable
public data class StreamDimens(
    public val channelItemVerticalPadding: Dp,
    public val channelItemHorizontalPadding: Dp,
) {
    public companion object {
        public fun defaultDimens(): StreamDimens = StreamDimens(
            channelItemVerticalPadding = 12.dp,
            channelItemHorizontalPadding = 8.dp
        )
    }
}
