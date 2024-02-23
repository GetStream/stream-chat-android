package io.getstream.chat.android.compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import io.getstream.chat.android.compose.R

/**
 * Represents the theming for the message composer.
 * @param cancelIcon The theming for the cancel icon used in the message composer.
 */
public data class MessageComposerTheme(
    val cancelIcon: MessageComposerCancelIconStyle,
) {

    public companion object {

        /**
         * Builds the default message composer theme.
         *
         * @return A [MessageComposerTheme] instance holding the default theming.
         */
        @Composable
        public fun defaultTheme(
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): MessageComposerTheme {
            return MessageComposerTheme(
                cancelIcon = MessageComposerCancelIconStyle(
                    backgroundShape = CircleShape,
                    backgroundColor = colors.overlayDark,
                    painter = painterResource(id = R.drawable.stream_compose_ic_close),
                    tint = colors.appBackground,
                ),
            )
        }
    }

}

/**
 * Represents the theming for the cancel icon used in the message composer.
 *
 * @param backgroundShape The shape of the background for the cancel icon.
 * @param backgroundColor The background color for the cancel icon.
 * @param tint The tint color for the cancel icon.
 */
public data class MessageComposerCancelIconStyle(
    val backgroundShape: Shape,
    val backgroundColor: Color,
    val painter: Painter,
    val tint: Color,
)