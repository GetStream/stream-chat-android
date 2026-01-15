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

package io.getstream.chat.android.compose.ui.theme.messages.attachments

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ComponentPadding
import io.getstream.chat.android.compose.ui.theme.ComponentSize
import io.getstream.chat.android.compose.ui.theme.IconContainerStyle
import io.getstream.chat.android.compose.ui.theme.IconStyle
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamTypography
import io.getstream.chat.android.compose.ui.theme.TextContainerStyle
import io.getstream.chat.android.compose.ui.theme.WaveformSliderLayoutStyle
import io.getstream.chat.android.compose.ui.theme.WaveformSliderStyle

/**
 * Represents the theming for the audio recording attachment.
 *
 * @param size The size of the audio recording attachment.
 * @param padding The padding for the audio recording attachment.
 * @param playButton The style for the play button.
 * @param pauseButton The style for the pause button.
 * @param timerStyle The style for the timer component.
 * @param waveformSliderStyle The style for the waveform slider.
 * @param tailWidth The width of the tail container which holds the speed button and the content type icon.
 * @param speedButton The style for the speed button.
 * @param contentTypeIcon The style for the content type icon.
 */
public data class AudioRecordingAttachmentTheme(
    public val size: ComponentSize,
    public val padding: ComponentPadding,
    public val playButton: IconContainerStyle,
    public val pauseButton: IconContainerStyle,
    public val timerStyle: TextContainerStyle,
    public val waveformSliderStyle: WaveformSliderLayoutStyle,
    public val tailWidth: Dp,
    public val speedButton: TextContainerStyle,
    public val contentTypeIcon: IconStyle,
) {

    public companion object {

        /**
         * Builds the default theming for the audio recording attachment for the current user.
         *
         * @param isInDarkMode If the app is in dark mode.
         * @param typography The typography to use for the audio recording attachment.
         * @param colors The colors to use for the audio recording attachment.
         *
         * @return The [AudioRecordingAttachmentTheme] instance with the default theming.
         */
        @Composable
        public fun defaultOwnTheme(
            isInDarkMode: Boolean = isSystemInDarkTheme(),
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isInDarkMode) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): AudioRecordingAttachmentTheme {
            return defaultTheme(own = true, isInDarkMode = isInDarkMode, typography = typography, colors = colors)
        }

        /**
         * Builds the default theming for the audio recording attachment for other users.
         *
         * @param isInDarkMode If the app is in dark mode.
         * @param typography The typography to use for the audio recording attachment.
         * @param colors The colors to use for the audio recording attachment.
         *
         * @return The [AudioRecordingAttachmentTheme] instance with the default theming.
         */
        @Composable
        public fun defaultOtherTheme(
            isInDarkMode: Boolean = isSystemInDarkTheme(),
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isInDarkMode) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): AudioRecordingAttachmentTheme {
            return defaultTheme(own = false, isInDarkMode = isInDarkMode, typography = typography, colors = colors)
        }

        /**
         * Builds the default theming for the audio recording attachment.
         *
         * @param own If the audio recording attachment is for the current user.
         * @param isInDarkMode If the app is in dark mode.
         * @param typography The typography to use for the audio recording attachment.
         * @param colors The colors to use for the audio recording attachment.
         *
         * @return The [AudioRecordingAttachmentTheme] instance with the default theming.
         */
        @Composable
        public fun defaultTheme(
            own: Boolean,
            isInDarkMode: Boolean = isSystemInDarkTheme(),
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isInDarkMode) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): AudioRecordingAttachmentTheme {
            return AudioRecordingAttachmentTheme(
                size = ComponentSize.fillMaxWidth(height = 60.dp),
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
                    textStyle = typography.body.copy(
                        color = colors.textLowEmphasis,
                    ),
                ),
                waveformSliderStyle = WaveformSliderLayoutStyle(
                    height = 36.dp,
                    style = WaveformSliderStyle.defaultStyle(colors = colors),
                ),
                tailWidth = 48.dp,
                speedButton = TextContainerStyle(
                    size = ComponentSize.square(36.dp),
                    padding = ComponentPadding.Zero,
                    backgroundColor = Color.White,
                    textStyle = typography.body,
                ),
                contentTypeIcon = IconStyle(
                    size = ComponentSize(height = 40.dp, width = 34.dp),
                    painter = painterResource(id = R.drawable.stream_compose_ic_file_audio),
                    tint = Color.Unspecified,
                ),
            )
        }
    }
}
