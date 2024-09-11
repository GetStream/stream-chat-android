package io.getstream.chat.android.compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Represents the style for the waveform slider.
 */
public data class WaveformSliderStyle(
    val thumbStyle: WaveformThumbStyle,
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
            thumbStyle = WaveformThumbStyle.defaultStyle(colors = colors),
            trackerStyle = WaveformTrackStyle.defaultStyle(colors = colors),
        )
    }
}

/**
 * Represents the style for the waveform thumb.
 */
public data class WaveformThumbStyle(
    val widthDefault: Dp,
    val widthPressed: Dp,
    val backgroundColor: Color,
    val backgroundShape: Shape,
    val borderColor: Color,
    val borderShape: Shape,
    val borderWidth: Dp,
) {

    public companion object {

        @Composable
        public fun defaultStyle(
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): WaveformThumbStyle = WaveformThumbStyle(
            widthDefault = 7.dp,
            widthPressed = 10.dp,
            backgroundColor = Color.White,
            backgroundShape = RoundedCornerShape(5.dp),
            borderColor = Color.LightGray,
            borderShape = RoundedCornerShape(5.dp),
            borderWidth = 1.dp,
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