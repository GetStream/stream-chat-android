package io.getstream.chat.android.compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/**
 * Represents message date separator theming.
 *
 * @param textStyle The text style for the date separator.
 * @param backgroundColor The background color for the date separator.
 */
@Immutable
public class MessageDateSeparatorTheme(
    public val textStyle: TextStyle,
    public val backgroundColor: Color,
) {

    public companion object {

        /**
         * Builds the default message date separator theme.
         *
         * @return A [MessageDateSeparatorTheme] instance holding the default theming.
         */
        @Composable
        public fun defaultTheme(
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): MessageDateSeparatorTheme {
            return MessageDateSeparatorTheme(
                textStyle = typography.body.copy(
                    color = colors.textHighEmphasisInverse,
                ),
                backgroundColor = colors.overlayDark,
            )
        }
    }
}