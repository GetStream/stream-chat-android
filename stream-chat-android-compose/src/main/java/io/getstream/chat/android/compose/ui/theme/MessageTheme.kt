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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.messages.attachments.AudioRecordingAttachmentTheme
import io.getstream.chat.android.compose.ui.theme.messages.attachments.FileAttachmentTheme

/**
 * Represents message theming.
 *
 * @param backgroundShapes The shapes for the message background.
 * @param audioRecording The theming for the audio recording attachment.
 * @param mentionColor The color for the mentions in the messages.
 */
@Immutable
public data class MessageTheme(
    val backgroundShapes: MessageBackgroundShapes,
    val audioRecording: AudioRecordingAttachmentTheme,
    val fileAttachmentTheme: FileAttachmentTheme,
    val mentionColor: Color,
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
            return MessageTheme(
                backgroundShapes = MessageBackgroundShapes(
                    top = RoundedCornerShape(20.dp),
                    middle = RoundedCornerShape(20.dp),
                    bottom = when (own) {
                        true -> shapes.myMessageBubble
                        else -> shapes.otherMessageBubble
                    },
                    none = when (own) {
                        true -> shapes.myMessageBubble
                        else -> shapes.otherMessageBubble
                    },
                ),
                audioRecording = AudioRecordingAttachmentTheme.defaultTheme(
                    own = own,
                    isInDarkMode = isInDarkMode,
                    typography = typography,
                    colors = colors,
                ),
                fileAttachmentTheme = FileAttachmentTheme.defaultTheme(
                    typography = typography,
                    shapes = shapes,
                    colors = colors,
                ),
                mentionColor = colors.chatTextMention,
            )
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
