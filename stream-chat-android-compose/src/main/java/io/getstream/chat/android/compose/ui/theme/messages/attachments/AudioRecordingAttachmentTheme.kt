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

package io.getstream.chat.android.compose.ui.theme.messages.attachments

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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
import io.getstream.chat.android.compose.ui.theme.WaveformSliderStyle

/**
 * Represents the theming for the audio recording attachment.
 *
 * @param height The height of the audio recording attachment.
 * @param padding The padding for the audio recording attachment.
 * @param playButton The theming for the play button.
 * @param pauseButton The theming for the pause button.
 * @param timerTextWidth The width of the timer text.
 * @param timerTextStyle The text style for the timer text.
 * @param waveformSliderStyle The theming for the waveform slider.
 * @param waveformSliderHeight The height of the waveform slider.
 * @param waveformSliderPadding The padding for the waveform slider.
 * @param tailWidth The width of the tail container which holds the speed button and the icon.
 * @param speedButton The theming for the speed button.
 * @param iconStyle The theming for the icon.
 */
public class AudioRecordingAttachmentTheme(
    public val height: Dp,
    public val padding: ComponentPadding,
    public val playButton: IconContainerStyle,
    public val pauseButton: IconContainerStyle,
    public val timerTextWidth: Dp,
    public val timerTextStyle: TextStyle,
    public val waveformSliderStyle: WaveformSliderStyle,
    public val waveformSliderHeight: Dp,
    public val waveformSliderPadding: ComponentPadding,
    public val tailWidth: Dp,
    public val speedButton: TextContainerStyle,
    public val iconStyle: IconStyle,
) {

    public companion object {
        @Composable
        public fun defaultTheme(
            own: Boolean,
            isInDarkMode: Boolean = isSystemInDarkTheme(),
            typography: StreamTypography,
            colors: StreamColors,
        ): AudioRecordingAttachmentTheme {
            return AudioRecordingAttachmentTheme(
                height = 60.dp,
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
                timerTextWidth = 48.dp,
                timerTextStyle = typography.body.copy(
                    color = colors.textLowEmphasis,
                ),
                waveformSliderStyle = WaveformSliderStyle.defaultStyle(colors = colors),
                waveformSliderHeight = 36.dp,
                waveformSliderPadding = ComponentPadding.Zero,
                tailWidth = 48.dp,
                speedButton = TextContainerStyle(
                    size = ComponentSize.square(36.dp),
                    padding = ComponentPadding.Zero,
                    backgroundColor = Color.White,
                    textStyle = typography.body,
                ),
                iconStyle = IconStyle(
                    size = ComponentSize(height = 40.dp, width = 34.dp),
                    painter = painterResource(id = R.drawable.stream_compose_ic_file_aac),
                    tint = Color.Unspecified,
                ),
            )
        }
    }
}
