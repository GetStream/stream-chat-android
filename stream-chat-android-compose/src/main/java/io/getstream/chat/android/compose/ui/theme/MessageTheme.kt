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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import io.getstream.chat.android.compose.ui.theme.messages.attachments.AudioRecordingAttachmentTheme

/**
 * Represents message theming.
 *
 * @param audioRecording The theming for the audio recording attachment.
 */
@Immutable
public data class MessageTheme(
    val audioRecording: AudioRecordingAttachmentTheme,
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
        private fun defaultTheme(
            own: Boolean,
            isInDarkMode: Boolean,
            typography: StreamTypography,
            shapes: StreamShapes,
            colors: StreamColors,
        ): MessageTheme {
            return MessageTheme(
                audioRecording = AudioRecordingAttachmentTheme.defaultTheme(
                    own = own,
                    isInDarkMode = isInDarkMode,
                    typography = typography,
                    colors = colors,
                ),
            )
        }
    }
}
