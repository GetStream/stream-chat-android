package io.getstream.chat.android.compose.ui.theme.messages.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamShapes
import io.getstream.chat.android.compose.ui.theme.StreamTypography

/**
 * Represents the style for poll messages.
 *
 * @param backgroundColor The background color for the poll message.
 */
public data class PollMessageStyle(
    val backgroundColor: Color,
) {

    public companion object {

        /**
         * Builds the default poll message style for the current user's messages.
         *
         * Returns a [PollMessageStyle] instance holding our default theming.
         */
        @Suppress("DEPRECATION_ERROR")
        @Composable
        public fun defaultStyle(
            own: Boolean,
            isInDarkMode: Boolean,
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isInDarkMode) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
            shapes: StreamShapes = StreamShapes.defaultShapes(),
        ): PollMessageStyle {
            return PollMessageStyle(
                backgroundColor = when (own) {
                    true -> colors.linkBackground
                    else -> colors.otherMessagesBackground
                },
            )
        }
    }
}