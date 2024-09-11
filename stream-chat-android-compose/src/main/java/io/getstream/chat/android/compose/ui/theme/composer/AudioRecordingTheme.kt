package io.getstream.chat.android.compose.ui.theme.composer

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ComposerCancelIconStyle
import io.getstream.chat.android.compose.ui.theme.IconComponentStyle
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamTypography
import io.getstream.chat.android.compose.ui.theme.WaveformSliderStyle

public data class AudioRecordingTheme(
    val enabled: Boolean = false,
    val showRecordButtonOverSend: Boolean = false,
    val recordButtonIconStyle: IconComponentStyle,
    val waveformSliderStyle: WaveformSliderStyle,
    val slideToCancelTextStyle: TextStyle,
    val slideToCancelIconStyle: IconComponentStyle,
) {

    public companion object {
        @Composable
        public fun defaultTheme(
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): AudioRecordingTheme {
            return AudioRecordingTheme(
                enabled = false,
                showRecordButtonOverSend = false,
                recordButtonIconStyle = IconComponentStyle(
                    painter = painterResource(id = R.drawable.stream_compose_ic_mic_active),
                    tint = colors.textLowEmphasis,
                    width = 24.dp,
                    height = 24.dp,
                ),
                waveformSliderStyle = WaveformSliderStyle.defaultStyle(colors = colors),
                slideToCancelTextStyle = typography.body.copy(
                    color = colors.textLowEmphasis,
                ),
                slideToCancelIconStyle = IconComponentStyle(
                    painter = painterResource(id = R.drawable.stream_compose_ic_arrow_left_black),
                    tint = colors.textLowEmphasis,
                    width = 24.dp,
                    height = 24.dp,
                ),
            )
        }
    }

}
