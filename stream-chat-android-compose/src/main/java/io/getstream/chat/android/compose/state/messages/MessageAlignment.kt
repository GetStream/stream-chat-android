package io.getstream.chat.android.compose.state.messages

import androidx.compose.ui.Alignment

/**
 * Represents the horizontal alignment of messages in the message list.
 *
 * @param itemAlignment The alignment of the message item.
 * @param contentAlignment The alignment of the inner content.
 */
public enum class MessageAlignment(
    public val itemAlignment: Alignment,
    public val contentAlignment: Alignment.Horizontal,
) {
    /**
     * Represents the alignment at the start of the screen, by default for other people's messages.
     */
    Start(Alignment.CenterStart, Alignment.Start),

    /**
     * Represents the alignment at the end of the screen, by default for owned messages.
     */
    End(Alignment.CenterEnd, Alignment.End),
}
