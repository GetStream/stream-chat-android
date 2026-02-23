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

package io.getstream.chat.android.compose.ui.theme.messages.composer.attachments

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ComponentPadding
import io.getstream.chat.android.compose.ui.theme.ComponentSize
import io.getstream.chat.android.compose.ui.theme.IconContainerStyle
import io.getstream.chat.android.compose.ui.theme.IconStyle
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamTypography
import io.getstream.chat.android.compose.ui.theme.TextContainerStyle

/**
 * Represents the theming for the audio recording attachment.
 *
 * @param size The size of the audio recording attachment preview.
 * @param padding The padding for the audio recording attachment preview.
 * @param playButton The style for the play button.
 * @param pauseButton The style for the pause button.
 * @param timerStyle The style for the timer component.
 */
public data class AudioRecordingAttachmentPreviewTheme(
    public val size: ComponentSize,
    public val padding: ComponentPadding,
    public val playButton: IconContainerStyle,
    public val pauseButton: IconContainerStyle,
    public val timerStyle: TextContainerStyle,
) {

    public companion object {

        /**
         * Builds the default theming for the audio recording attachment.
         *
         * @param isInDarkMode If the app is in dark mode.
         * @param typography The typography to use for the audio recording attachment.
         * @param colors The colors to use for the audio recording attachment.
         *
         * @return The [AudioRecordingAttachmentPreviewTheme] instance with the default theming.
         */
        @Composable
        public fun defaultTheme(
            isInDarkMode: Boolean = isSystemInDarkTheme(),
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isInDarkMode) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): AudioRecordingAttachmentPreviewTheme {
            return AudioRecordingAttachmentPreviewTheme(
                size = ComponentSize(height = 50.dp, width = 200.dp),
                padding = ComponentPadding(start = 8.dp, end = 0.dp, top = 2.dp, bottom = 2.dp),
                playButton = IconContainerStyle(
                    size = ComponentSize.square(36.dp),
                    padding = ComponentPadding.Zero,
                    icon = IconStyle(
                        painter = painterResource(id = R.drawable.stream_compose_ic_play),
                        tint = Color.Black,
                        size = ComponentSize.square(24.dp),
                    ),
                ),
                pauseButton = IconContainerStyle(
                    size = ComponentSize.square(36.dp),
                    padding = ComponentPadding.Zero,
                    icon = IconStyle(
                        painter = painterResource(id = R.drawable.stream_compose_ic_pause),
                        tint = Color.Black,
                        size = ComponentSize.square(24.dp),
                    ),
                ),
                timerStyle = TextContainerStyle(
                    size = ComponentSize.width(48.dp),
                    padding = ComponentPadding.Zero,
                    backgroundColor = Color.Unspecified,
                    textStyle = typography.bodyDefault.copy(
                        color = colors.textSecondary,
                    ),
                ),
            )
        }
    }
}
