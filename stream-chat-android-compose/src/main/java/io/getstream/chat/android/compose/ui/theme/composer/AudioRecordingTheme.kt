package io.getstream.chat.android.compose.ui.theme.composer

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.IconButtonStyle
import io.getstream.chat.android.compose.ui.theme.IconStyle
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamTypography
import io.getstream.chat.android.compose.ui.theme.WaveformSliderStyle

public data class AudioRecordingTheme(
    val enabled: Boolean = false,
    val showRecordButtonOverSend: Boolean = false,
    val recordButtonIconStyle: IconStyle,
    val waveformSliderStyle: WaveformSliderStyle,
    val slideToCancelTextStyle: TextStyle,
    val slideToCancelIconStyle: IconStyle,
    val slideToCancelMarginEnd: Dp,
    val deleteButton: IconButtonStyle,
    val stopButton: IconButtonStyle,
    val completeButtonStyle: IconButtonStyle,
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
                recordButtonIconStyle = IconStyle(
                    painter = painterResource(id = R.drawable.stream_compose_ic_mic_active),
                    tint = colors.textLowEmphasis,
                    width = 24.dp,
                    height = 24.dp,
                ),
                waveformSliderStyle = WaveformSliderStyle.defaultStyle(colors = colors),
                slideToCancelTextStyle = typography.body.copy(
                    color = colors.textLowEmphasis,
                ),
                slideToCancelIconStyle = IconStyle(
                    painter = painterResource(id = R.drawable.stream_compose_ic_arrow_left_black),
                    tint = colors.textLowEmphasis,
                    width = 24.dp,
                    height = 24.dp,
                ),
                slideToCancelMarginEnd = 96.dp,
                deleteButton = IconButtonStyle(
                    width = 32.dp,
                    height = 32.dp,
                    padding = 4.dp,
                    icon = IconStyle(
                        painter = painterResource(id = R.drawable.stream_compose_ic_delete),
                        tint = colors.primaryAccent,
                        width = 24.dp,
                        height = 24.dp,
                    ),
                ),
                stopButton = IconButtonStyle(
                    width = 32.dp,
                    height = 32.dp,
                    padding = 4.dp,
                    icon = IconStyle(
                        painter = painterResource(id = R.drawable.stream_compose_ic_stop_circle),
                        tint = colors.errorAccent,
                        width = 24.dp,
                        height = 24.dp,
                    ),
                ),
                completeButtonStyle = IconButtonStyle(
                    width = 32.dp,
                    height = 32.dp,
                    padding = 4.dp,
                    icon = IconStyle(
                        painter = painterResource(id = R.drawable.stream_compose_ic_check_circle),
                        tint = colors.primaryAccent,
                        width = 24.dp,
                        height = 24.dp,
                    ),
                ),
            )
        }
    }

}
