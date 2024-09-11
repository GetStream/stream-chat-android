package io.getstream.chat.android.compose.ui.theme.composer

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
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
import io.getstream.chat.android.compose.ui.theme.WaveformSliderStyle

public data class AudioRecordingTheme(
    val enabled: Boolean = false,
    val showRecordButtonOverSend: Boolean = false,
    val recordButtonIconStyle: IconStyle,
    val waveformSliderStyle: WaveformSliderStyle,
    val waveformSliderPadding: ComponentPadding,
    val slideToCancelTextStyle: TextStyle,
    val slideToCancelIconStyle: IconStyle,
    val slideToCancelMarginEnd: Dp,

    val micIndicator: IconContainerStyle,

    val timerTextStyle: TextStyle,

    val playbackHeight: Dp,
    val playButton: IconContainerStyle,
    val pauseButton: IconContainerStyle,

    val controlsHeight: Dp,
    val deleteButton: IconContainerStyle,
    val stopButton: IconContainerStyle,
    val completeButtonStyle: IconContainerStyle,
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
                    size = ComponentSize(width = 24.dp, height = 24.dp),
                ),
                waveformSliderStyle = WaveformSliderStyle.defaultStyle(colors = colors),
                waveformSliderPadding = ComponentPadding(start = 16.dp, top = 8.dp, end = 0.dp, bottom = 8.dp),
                slideToCancelTextStyle = typography.body.copy(
                    color = colors.textLowEmphasis,
                ),
                slideToCancelIconStyle = IconStyle(
                    painter = painterResource(id = R.drawable.stream_compose_ic_arrow_left_black),
                    tint = colors.textLowEmphasis,
                    size = ComponentSize(width = 24.dp, height = 24.dp),
                ),
                slideToCancelMarginEnd = 96.dp,

                playbackHeight = 48.dp,
                micIndicator = IconContainerStyle(
                    size = ComponentSize(width = 32.dp, height = 32.dp),
                    padding = ComponentPadding.all(4.dp),
                    icon = IconStyle(
                        painter = painterResource(id = R.drawable.stream_compose_ic_mic),
                        tint = colors.errorAccent,
                        size = ComponentSize(width = 24.dp, height = 24.dp),
                    ),
                ),

                timerTextStyle = typography.body.copy(
                    color = colors.textLowEmphasis,
                ),

                playButton = IconContainerStyle(
                    size = ComponentSize(width = 32.dp, height = 32.dp),
                    padding = ComponentPadding.all(4.dp),
                    icon = IconStyle(
                        painter = painterResource(id = R.drawable.stream_compose_ic_play),
                        tint = colors.primaryAccent,
                        size = ComponentSize(width = 24.dp, height = 24.dp),
                    ),
                ),
                pauseButton = IconContainerStyle(
                    size = ComponentSize(width = 32.dp, height = 32.dp),
                    padding = ComponentPadding.all(4.dp),
                    icon = IconStyle(
                        painter = painterResource(id = R.drawable.stream_compose_ic_pause),
                        tint = colors.primaryAccent,
                        size = ComponentSize(width = 24.dp, height = 24.dp),
                    ),
                ),

                controlsHeight = 48.dp,
                deleteButton = IconContainerStyle(
                    size = ComponentSize(width = 32.dp, height = 32.dp),
                    padding = ComponentPadding.all(4.dp),
                    icon = IconStyle(
                        painter = painterResource(id = R.drawable.stream_compose_ic_delete),
                        tint = colors.primaryAccent,
                        size = ComponentSize(width = 24.dp, height = 24.dp),
                    ),
                ),
                stopButton = IconContainerStyle(
                    size = ComponentSize(width = 32.dp, height = 32.dp),
                    padding = ComponentPadding.all(4.dp),
                    icon = IconStyle(
                        painter = painterResource(id = R.drawable.stream_compose_ic_stop_circle),
                        tint = colors.errorAccent,
                        size = ComponentSize(width = 24.dp, height = 24.dp),
                    ),
                ),
                completeButtonStyle = IconContainerStyle(
                    size = ComponentSize(width = 32.dp, height = 32.dp),
                    padding = ComponentPadding.all(4.dp),
                    icon = IconStyle(
                        painter = painterResource(id = R.drawable.stream_compose_ic_check_circle),
                        tint = colors.primaryAccent,
                        size = ComponentSize(width = 24.dp, height = 24.dp),
                    ),
                ),
            )
        }
    }

}
