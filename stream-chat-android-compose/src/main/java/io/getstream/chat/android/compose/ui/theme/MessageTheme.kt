/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.messages.attachments.AudioRecordingAttachmentTheme
import io.getstream.chat.android.compose.ui.theme.messages.attachments.FileAttachmentTheme
import io.getstream.chat.android.compose.ui.theme.messages.list.PollMessageStyle
import io.getstream.chat.android.compose.ui.theme.messages.list.QuotedMessageStyle

/**
 * Represents message theming.
 *
 * @param textStyle The text style for the messages.
 * @param errorTextStyle The text style for the error messages.
 * @param contentPadding The padding for the message content.
 * @param backgroundColor The background color for the messages.
 * @param backgroundBorder The border for the message background.
 * @param backgroundShapes The shapes for the message background.
 * @param quotedTextStyle The text style for the quoted messages contained in a reply.
 * @param quotedBackgroundColor The background color for the quoted messages.
 * @param errorBackgroundColor The background color for the error messages.
 * @param deletedBackgroundColor The background color for the deleted messages.
 * @param audioRecording The theming for the audio recording attachment.
 * @param mentionColor The color for the mentions in the messages.
 * @param linkStyle The text style for the links in message components.
 * @param linkBackgroundColor The color for the message link card background.
 */
@Immutable
public data class MessageTheme(
    val textStyle: TextStyle,
    val errorTextStyle: TextStyle,
    val contentPadding: ComponentPadding,
    val backgroundColor: Color,
    val backgroundBorder: BorderStroke?,
    val backgroundShapes: MessageBackgroundShapes,
    @Deprecated(
        message = "Use quoted.textStyle instead",
        replaceWith = ReplaceWith(
            expression = "QuotedMessageStyle(textStyle = TextStyle(...))",
            imports = arrayOf("io.getstream.chat.android.compose.ui.theme.MessageTheme.quoted.textStyle"),
        ),
        level = DeprecationLevel.WARNING,
    )
    val quotedTextStyle: TextStyle,
    @Deprecated(
        message = "Use quoted.backgroundColor instead",
        replaceWith = ReplaceWith(
            expression = "QuotedMessageStyle(backgroundColor = Color(...))",
            imports = arrayOf("io.getstream.chat.android.compose.ui.theme.MessageTheme.quoted.backgroundColor"),
        ),
        level = DeprecationLevel.WARNING,
    )
    val quotedBackgroundColor: Color,
    val errorBackgroundColor: Color,
    val deletedBackgroundColor: Color,
    val audioRecording: AudioRecordingAttachmentTheme,
    val fileAttachmentTheme: FileAttachmentTheme,
    val quoted: QuotedMessageStyle,
    val poll: PollMessageStyle,
    val mentionColor: Color,
    val linkStyle: TextStyle,
    val linkBackgroundColor: Color,
) {
    public companion object {

        /**
         * Builds the default message theme for the current user's messages.
         *
         * @return A [MessageTheme] instance holding our default theming.
         */
        @Composable
        public fun defaultOwnTheme(
            isInDarkMode: Boolean = isSystemInDarkTheme(),
            typography: StreamTypography = StreamTypography.defaultTypography(),
            shapes: StreamShapes = StreamShapes.defaultShapes(),
            colors: StreamColors = when (isInDarkMode) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): MessageTheme = defaultTheme(
            own = true,
            isInDarkMode = isInDarkMode,
            typography = typography,
            shapes = shapes,
            colors = colors,
        )

        /**
         * Builds the default message theme for other users' messages.
         *
         * @return A [MessageTheme] instance holding our default theming.
         */
        @Composable
        public fun defaultOtherTheme(
            isInDarkMode: Boolean = isSystemInDarkTheme(),
            typography: StreamTypography = StreamTypography.defaultTypography(),
            shapes: StreamShapes = StreamShapes.defaultShapes(),
            colors: StreamColors = when (isInDarkMode) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): MessageTheme = defaultTheme(
            own = false,
            isInDarkMode = isInDarkMode,
            typography = typography,
            shapes = shapes,
            colors = colors,
        )

        @Composable
        @Suppress("DEPRECATION_ERROR")
        private fun defaultTheme(
            own: Boolean,
            isInDarkMode: Boolean,
            typography: StreamTypography,
            shapes: StreamShapes,
            colors: StreamColors,
        ): MessageTheme {
            val textStyle = typography.bodyBold.copy(
                color = when (own) {
                    true -> colors.ownMessageText
                    else -> colors.otherMessageText
                },
            )
            val backgroundColor = when (own) {
                true -> colors.ownMessagesBackground
                else -> colors.otherMessagesBackground
            }
            return MessageTheme(
                textStyle = textStyle,
                errorTextStyle = textStyle,
                contentPadding = ComponentPadding.Zero,
                backgroundColor = backgroundColor,
                backgroundBorder = when (own) {
                    true -> null
                    else -> BorderStroke(1.dp, colors.borders)
                },
                backgroundShapes = MessageBackgroundShapes(
                    top = RoundedCornerShape(16.dp),
                    middle = RoundedCornerShape(16.dp),
                    bottom = when (own) {
                        true -> shapes.myMessageBubble
                        else -> shapes.otherMessageBubble
                    },
                    none = when (own) {
                        true -> shapes.myMessageBubble
                        else -> shapes.otherMessageBubble
                    },
                ),
                // Deprecated
                quotedTextStyle = typography.bodyBold.copy(
                    color = when (own) {
                        true -> colors.ownMessageQuotedText
                        else -> colors.otherMessageQuotedText
                    },
                ),
                // Deprecated
                quotedBackgroundColor = when (own) {
                    true -> colors.ownMessageQuotedBackground
                    else -> colors.otherMessageQuotedBackground
                },
                errorBackgroundColor = backgroundColor,
                deletedBackgroundColor = colors.deletedMessagesBackground,
                audioRecording = AudioRecordingAttachmentTheme.defaultTheme(
                    own = own,
                    isInDarkMode = isInDarkMode,
                    typography = typography,
                    colors = colors,
                ),
                quoted = QuotedMessageStyle.defaultStyle(
                    own = own,
                    isInDarkMode = isInDarkMode,
                    typography = typography,
                    colors = colors,
                    shapes = shapes,
                ),
                poll = PollMessageStyle.defaultStyle(
                    own = own,
                    isInDarkMode = isInDarkMode,
                    typography = typography,
                    colors = colors,
                    shapes = shapes,
                ),
                fileAttachmentTheme = FileAttachmentTheme.defaultTheme(
                    typography = typography,
                    shapes = shapes,
                    colors = colors,
                ),
                mentionColor = colors.primaryAccent,
                linkStyle = TextStyle(
                    color = colors.primaryAccent,
                    textDecoration = TextDecoration.Underline,
                ),
                linkBackgroundColor = colors.linkBackground,
            ).let { theme ->
                theme.copy(
                    quoted = theme.quoted.copy(
                        textStyle = theme.quotedTextStyle,
                        backgroundColor = theme.quotedBackgroundColor,
                    ),
                )
            }
        }
    }
}

/**
 * Represents the shapes for the message background in different positions.
 *
 * @param top The shape which is used for the top message in a group.
 * @param middle The shape which is used for the middle message in a group.
 * @param bottom The shape which is used for the bottom message in a group.
 * @param none The shape which is used for messages that are not in a group.
 */
public data class MessageBackgroundShapes(
    val top: Shape,
    val middle: Shape,
    val bottom: Shape,
    val none: Shape,
)
