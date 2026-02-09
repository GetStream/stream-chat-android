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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Represents the style for the waveform slider layout.
 *
 * @param height The height of the waveform slider.
 * @param style The style for the waveform slider.
 */
public data class WaveformSliderLayoutStyle(
    val height: Dp,
    val style: WaveformSliderStyle,
)

/**
 * Represents the style for the waveform slider.
 */
public data class WaveformSliderStyle(
    val trackerStyle: WaveformTrackStyle,
) {

    public companion object {

        @Composable
        public fun defaultStyle(
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): WaveformSliderStyle = WaveformSliderStyle(
            trackerStyle = WaveformTrackStyle.defaultStyle(colors = colors),
        )
    }
}

/**
 * Represents the style for the waveform track.
 *
 * @param passedColor The color for the passed part of the waveform.
 * @param futureColor The color for the future part of the waveform.
 * @param minBarHeight The minimum height for the waveform bars.
 * @param barSpacingRatio The ratio for the spacing between the waveform bars.
 */
public data class WaveformTrackStyle(
    val passedColor: Color,
    val futureColor: Color,
    val minBarHeight: Dp,
    val barSpacingRatio: Float,
) {

    public companion object {

        @Composable
        public fun defaultStyle(
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): WaveformTrackStyle = WaveformTrackStyle(
            passedColor = colors.primaryAccent,
            futureColor = Color.LightGray,
            minBarHeight = 4.dp,
            barSpacingRatio = 0.2f,
        )
    }
}
