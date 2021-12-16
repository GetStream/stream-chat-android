package io.getstream.chat.android.compose.state

import androidx.compose.ui.Alignment

/**
 * Represents the position of the online indicator in user avatars.
 *
 * @param alignment The standard Compose [Alignment] that corresponds to the indicator alignment.
 */
public enum class OnlineIndicatorAlignment(public val alignment: Alignment) {
    /**
     * The top end position within the avatar.
     */
    TopEnd(Alignment.TopEnd),

    /**
     * The bottom end position within the avatar.
     */
    BottomEnd(Alignment.BottomEnd),

    /**
     * The top start position within the avatar.
     */
    TopStart(Alignment.TopStart),

    /**
     * The bottom start position within the avatar.
     */
    BottomStart(Alignment.BottomStart)
}
