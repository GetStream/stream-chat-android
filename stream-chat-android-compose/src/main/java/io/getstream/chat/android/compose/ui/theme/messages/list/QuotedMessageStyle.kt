package io.getstream.chat.android.compose.ui.theme.messages.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import io.getstream.chat.android.compose.ui.theme.ComponentPadding
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamShapes
import io.getstream.chat.android.compose.ui.theme.StreamTypography

/**
 * Represents the style for quoted messages.
 *
 * @param textStyle The text style for the quoted message.
 * @param backgroundColor The background color for the quoted message.
 * @param backgroundShape The shape for the quoted message background.
 * @param backgroundBorder The border for the quoted message background.
 * @param contentPadding The padding for the quoted message content.
 */
public data class QuotedMessageStyle(
    val textStyle: TextStyle,
    val backgroundColor: Color,
    val backgroundShape: Shape,
    val backgroundBorder: BorderStroke?,
    val contentPadding: ComponentPadding,
) {

    public companion object {

        /**
         * Builds the default quoted message style for the current user's messages.
         *
         * Returns a [QuotedMessageStyle] instance holding our default theming.
         */
        @Composable
        public fun defaultOwnStyle(
            isInDarkMode: Boolean,
            typography: StreamTypography,
            colors: StreamColors,
            shapes: StreamShapes,
        ): QuotedMessageStyle {
            return defaultStyle(
                own = true,
                isInDarkMode = isInDarkMode,
                typography = typography,
                colors = colors,
                shapes = shapes,
            )
        }

        /**
         * Builds the default quoted message style for other users' messages.
         *
         * Returns a [QuotedMessageStyle] instance holding our default theming.
         */
        @Composable
        public fun defaultOtherStyle(
            isInDarkMode: Boolean,
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isInDarkMode) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
            shapes: StreamShapes = StreamShapes.defaultShapes(),
        ): QuotedMessageStyle {
            return defaultStyle(
                own = false,
                isInDarkMode = isInDarkMode,
                typography = typography,
                colors = colors,
                shapes = shapes,
            )
        }

        /**
         * Builds the default quoted message style.
         *
         * Returns a [QuotedMessageStyle] instance holding our default theming.
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
        ): QuotedMessageStyle {
            return QuotedMessageStyle(
                textStyle = typography.bodyBold.copy(
                    color = when (own) {
                        true -> colors.ownMessageQuotedText
                        else -> colors.otherMessageQuotedText
                    },
                ),
                backgroundColor = when (own) {
                    true -> colors.ownMessageQuotedBackground
                    else -> colors.otherMessageQuotedBackground
                },
                backgroundShape = when (own) {
                    true -> shapes.myMessageBubble
                    else -> shapes.otherMessageBubble
                },
                backgroundBorder = null,
                contentPadding = ComponentPadding.Zero,
            )
        }
    }

}