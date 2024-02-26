/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R

/**
 * Represents the theming for the message composer.
 * @param attachmentCancelIcon The theming for the cancel icon used in the message composer.
 */
public data class MessageComposerTheme(
    val attachmentCancelIcon: ComposerCancelIconStyle,
    val linkPreview: ComposerLinkPreviewTheme,
    val inputField: ComposerInputFieldTheme,
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
            shapes: StreamShapes = StreamShapes.defaultShapes(),
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): MessageComposerTheme {
            return MessageComposerTheme(
                attachmentCancelIcon = ComposerCancelIconStyle.defaultStyle(colors),
                linkPreview = ComposerLinkPreviewTheme.defaultTheme(typography, colors),
                inputField = ComposerInputFieldTheme.defaultTheme(typography, shapes, colors),
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
public data class ComposerCancelIconStyle(
    val backgroundShape: Shape,
    val backgroundColor: Color,
    val painter: Painter,
    val tint: Color,
) {
    public companion object {

        /**
         * Builds the default cancel icon style.
         *
         * @return A [ComposerCancelIconStyle] instance holding the default theming.
         */
        @Composable
        public fun defaultStyle(
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): ComposerCancelIconStyle {
            return ComposerCancelIconStyle(
                backgroundShape = CircleShape,
                backgroundColor = colors.overlayDark,
                painter = painterResource(id = R.drawable.stream_compose_ic_close),
                tint = colors.appBackground,
            )
        }
    }
}

/**
 * Represents the theming for the link preview in the message composer.
 *
 * @param imageSize The size of the image in the link preview.
 * @param imagePadding The padding for the image in the link preview.
 * @param imageShape The shape of the image in the link preview.
 * @param separatorSize The size of the separator in the link preview.
 * @param separatorMarginStart The start margin for the separator in the link preview.
 * @param separatorMarginEnd The end margin for the separator in the link preview.
 * @param title The theming for the title in the link preview.
 * @param titleToSubtitle The vertical space between the title and the subtitle in the link preview.
 * @param subtitle The theming for the subtitle in the link preview.
 * @param cancelIcon The theming for the cancel icon in the link preview.
 */
public data class ComposerLinkPreviewTheme(
    val imageSize: ComponentSize,
    val imageShape: Shape,
    val imagePadding: Dp,
    val separatorSize: ComponentSize,
    val separatorMarginStart: Dp,
    val separatorMarginEnd: Dp,
    val title: TextComponentStyle,
    val titleToSubtitle: Dp,
    val subtitle: TextComponentStyle,
    val cancelIcon: ComposerCancelIconStyle,
) {
    public companion object {

        @Composable
        public fun defaultTheme(
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): ComposerLinkPreviewTheme {
            return ComposerLinkPreviewTheme(
                imageSize = ComponentSize(width = 48.dp, height = 48.dp),
                imageShape = RectangleShape,
                imagePadding = 4.dp,
                separatorSize = ComponentSize(width = 2.dp, height = 48.dp),
                separatorMarginStart = 4.dp,
                separatorMarginEnd = 8.dp,
                title = TextComponentStyle(
                    color = colors.textHighEmphasis,
                    style = typography.bodyBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                ),
                titleToSubtitle = 4.dp,
                subtitle = TextComponentStyle(
                    color = colors.textHighEmphasis,
                    style = typography.footnote,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                ),
                cancelIcon = ComposerCancelIconStyle.defaultStyle(colors),
            )
        }
    }
}

/**
 * Represents the theming for the input field in the message composer.
 *
 * @param borderShape The shape of the border for the input field.
 * @param backgroundColor The background color for the input field.
 * @param textStyle The text style for the input field.
 * @param cursorBrushColor The color for the cursor in the input field.
 */
public data class ComposerInputFieldTheme(
    val borderShape: Shape,
    val backgroundColor: Color,
    val textStyle: TextStyle,
    val cursorBrushColor: Color,
) {

    public companion object {
        @Composable
        public fun defaultTheme(
            typography: StreamTypography = StreamTypography.defaultTypography(),
            shapes: StreamShapes = StreamShapes.defaultShapes(),
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): ComposerInputFieldTheme {
            return ComposerInputFieldTheme(
                borderShape = shapes.inputField,
                backgroundColor = colors.inputBackground,
                textStyle = typography.body.copy(
                    color = colors.textHighEmphasis,
                    textDirection = TextDirection.Content,
                ),
                cursorBrushColor = colors.primaryAccent,
            )
        }
    }
}
