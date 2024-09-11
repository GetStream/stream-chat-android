package io.getstream.chat.android.compose.ui.theme.composer

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ComponentOffset
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
    val recordButton: IconContainerStyle,
    val waveformSliderStyle: WaveformSliderStyle,
    val waveformSliderPadding: ComponentPadding,
    val slideToCancelTextStyle: TextStyle,
    val slideToCancelIconStyle: IconStyle,
    val slideToCancelMarginEnd: Dp,
    val slideToCancelThreshold: Dp,

    val micIndicator: IconContainerStyle,
    val timerTextStyle: TextStyle,

    val micFloatingButton: AudioRecordingFloatingIconStyle,
    val lockFloatingIcon: AudioRecordingFloatingIconStyle,
    val lockedFloatingIcon: AudioRecordingFloatingIconStyle,

    val lockThreshold: Dp,
    val lockEdgeOffset: ComponentOffset,

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
                recordButton = IconContainerStyle(
                    size = ComponentSize.square(48.dp),
                    padding = ComponentPadding.Zero,
                    icon = IconStyle(
                        painter = painterResource(id = R.drawable.stream_compose_ic_mic_active),
                        tint = colors.textLowEmphasis,
                        size = ComponentSize.square(24.dp),
                    ),
                ),
                waveformSliderStyle = WaveformSliderStyle.defaultStyle(colors = colors),
                waveformSliderPadding = ComponentPadding(start = 16.dp, top = 8.dp, end = 0.dp, bottom = 8.dp),
                slideToCancelTextStyle = typography.body.copy(
                    color = colors.textLowEmphasis,
                ),
                slideToCancelIconStyle = IconStyle(
                    painter = painterResource(id = R.drawable.stream_compose_ic_arrow_left_black),
                    tint = colors.textLowEmphasis,
                    size = ComponentSize.square(24.dp),
                ),
                slideToCancelMarginEnd = 96.dp,
                slideToCancelThreshold = 96.dp,

                playbackHeight = 48.dp,
                micIndicator = IconContainerStyle(
                    size = ComponentSize.square(32.dp),
                    padding = ComponentPadding.all(4.dp),
                    icon = IconStyle(
                        painter = painterResource(id = R.drawable.stream_compose_ic_mic),
                        tint = colors.errorAccent,
                        size = ComponentSize.square(24.dp),
                    ),
                ),

                timerTextStyle = typography.body.copy(
                    color = colors.textLowEmphasis,
                ),

                micFloatingButton = AudioRecordingFloatingIconStyle(
                    delegate = IconContainerStyle(
                        size = ComponentSize.square(64.dp),
                        padding = ComponentPadding.Zero,
                        icon = IconStyle(
                            painter = painterResource(id = R.drawable.stream_compose_ic_mic),
                            tint = colors.primaryAccent,
                            size = ComponentSize.square(24.dp),
                        ),
                    ),
                    backgroundColor = colorResource(id = R.color.stream_compose_grey_gainsboro),
                    backgroundShape = CircleShape,
                ),

                lockFloatingIcon = AudioRecordingFloatingIconStyle(
                    delegate = IconContainerStyle(
                        size = ComponentSize(width = 48.dp, height = 88.dp),
                        padding = ComponentPadding.Zero,
                        icon = IconStyle(
                            painter = painterResource(id = R.drawable.stream_compose_ic_mic_lock),
                            tint = Color.Unspecified,
                            size = ComponentSize(width = 48.dp, height = 88.dp),
                        ),
                    ),
                    backgroundColor = Color.Unspecified,
                    backgroundShape = RoundedCornerShape(24.dp),
                ),

                lockedFloatingIcon = AudioRecordingFloatingIconStyle(
                    delegate = IconContainerStyle(
                        size = ComponentSize.square(48.dp),
                        padding = ComponentPadding.Zero,
                        icon = IconStyle(
                            painter = painterResource(id = R.drawable.stream_compose_ic_mic_locked),
                            tint = Color.Unspecified,
                            size = ComponentSize.square(48.dp),
                        ),
                    ),
                    backgroundColor = Color.Unspecified,
                    backgroundShape = CircleShape,
                ),

                lockThreshold = 96.dp,
                lockEdgeOffset = ComponentOffset(x = 4.dp, y = 16.dp),

                playButton = IconContainerStyle(
                    size = ComponentSize.square(32.dp),
                    padding = ComponentPadding.all(4.dp),
                    icon = IconStyle(
                        painter = painterResource(id = R.drawable.stream_compose_ic_play),
                        tint = colors.primaryAccent,
                        size = ComponentSize.square(24.dp),
                    ),
                ),
                pauseButton = IconContainerStyle(
                    size = ComponentSize.square(32.dp),
                    padding = ComponentPadding.all(4.dp),
                    icon = IconStyle(
                        painter = painterResource(id = R.drawable.stream_compose_ic_pause),
                        tint = colors.primaryAccent,
                        size = ComponentSize.square(24.dp),
                    ),
                ),

                controlsHeight = 48.dp,
                deleteButton = IconContainerStyle(
                    size = ComponentSize.square(32.dp),
                    padding = ComponentPadding.all(4.dp),
                    icon = IconStyle(
                        painter = painterResource(id = R.drawable.stream_compose_ic_delete),
                        tint = colors.primaryAccent,
                        size = ComponentSize.square(24.dp),
                    ),
                ),
                stopButton = IconContainerStyle(
                    size = ComponentSize.square(32.dp),
                    padding = ComponentPadding.all(4.dp),
                    icon = IconStyle(
                        painter = painterResource(id = R.drawable.stream_compose_ic_stop_circle),
                        tint = colors.errorAccent,
                        size = ComponentSize.square(24.dp),
                    ),
                ),
                completeButtonStyle = IconContainerStyle(
                    size = ComponentSize.square(32.dp),
                    padding = ComponentPadding.all(4.dp),
                    icon = IconStyle(
                        painter = painterResource(id = R.drawable.stream_compose_ic_check_circle),
                        tint = colors.primaryAccent,
                        size = ComponentSize.square(24.dp),
                    ),
                ),
            )
        }
    }

}

/**
 * Represents the style for the floating icon in the audio recording theme.
 *
 * @param delegate The icon container style.
 * @param backgroundColor The background color of the floating icon.
 * @param backgroundShape The shape of the background.
 */
public data class AudioRecordingFloatingIconStyle(
    private val delegate: IconContainerStyle,
    val backgroundColor: Color,
    val backgroundShape: Shape,
) {

    val size: ComponentSize get() = delegate.size
    val padding: ComponentPadding get() = delegate.padding
    val icon: IconStyle get() = delegate.icon

}