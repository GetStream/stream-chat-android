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

package io.getstream.chat.android.compose.ui.theme.messages.composer

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

/**
 * Represents the theme for the audio recording component.
 *
 * @property enabled If the audio recording is enabled.
 * @property sendOnComplete Sends the recording on "Complete" button click.  If false, attaches it for manual sending.
 * @property showRecordButtonOverSend Shows the record button over the send button.
 * @property recordButton The style for the record button.
 * @property floatingIcons The theme for the floating icons.
 * @property controls The theme for the audio recording controls component.
 * @property holdToRecord The theme for the hold to record component.
 * @property permissionRationale The theme for the permission rationale component.
 */
public data class AudioRecordingTheme(
    val enabled: Boolean,
    val sendOnComplete: Boolean,
    val showRecordButtonOverSend: Boolean,
    val recordButton: IconContainerStyle,
    val floatingIcons: AudioRecordingFloatingIconsTheme,
    val controls: AudioRecordingControlsTheme,
    val holdToRecord: AudioRecordingHoldToRecordTheme,
    val permissionRationale: AudioRecordingPermissionRationaleTheme,
) {

    public companion object {
        @Composable
        public fun defaultTheme(
            isInDarkMode: Boolean = isSystemInDarkTheme(),
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isInDarkMode) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): AudioRecordingTheme {
            return AudioRecordingTheme(
                enabled = false,
                sendOnComplete = true,
                showRecordButtonOverSend = false,
                recordButton = IconContainerStyle(
                    size = ComponentSize.square(48.dp),
                    padding = ComponentPadding.Zero,
                    icon = IconStyle(
                        painter = painterResource(id = R.drawable.stream_compose_ic_mic),
                        tint = colors.textLowEmphasis,
                        size = ComponentSize.square(24.dp),
                    ),
                ),
                floatingIcons = AudioRecordingFloatingIconsTheme(
                    mic = AudioRecordingFloatingIconStyle(
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

                    lock = AudioRecordingFloatingIconStyle(
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
                    locked = AudioRecordingFloatingIconStyle(
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
                ),
                controls = AudioRecordingControlsTheme(
                    height = 48.dp,
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
                    completeButton = IconContainerStyle(
                        size = ComponentSize.square(32.dp),
                        padding = ComponentPadding.all(4.dp),
                        icon = IconStyle(
                            painter = painterResource(id = R.drawable.stream_compose_ic_check_circle),
                            tint = colors.primaryAccent,
                            size = ComponentSize.square(24.dp),
                        ),
                    ),
                ),
                holdToRecord = AudioRecordingHoldToRecordTheme(
                    containerElevation = 2.dp,
                    containerPadding = ComponentPadding(horizontal = 8.dp, vertical = 16.dp),
                    containerColor = colorResource(
                        when (isInDarkMode) {
                            true -> R.color.stream_compose_white_85
                            else -> R.color.stream_compose_black_85
                        },
                    ),
                    containerShape = RoundedCornerShape(16.dp),
                    contentHeight = 48.dp,
                    contentPadding = ComponentPadding(horizontal = 16.dp),
                    textStyle = typography.bodyBold.copy(
                        color = colors.textHighEmphasisInverse,
                    ),
                ),
                permissionRationale = AudioRecordingPermissionRationaleTheme(
                    containerElevation = 2.dp,
                    containerPadding = ComponentPadding(horizontal = 8.dp),
                    containerColor = colorResource(
                        when (isInDarkMode) {
                            true -> R.color.stream_compose_white_85
                            else -> R.color.stream_compose_black_85
                        },
                    ),
                    containerShape = RoundedCornerShape(4.dp),
                    containerBottomOffset = 16.dp,
                    contentHeight = 64.dp,
                    contentSpace = 8.dp,
                    contentPadding = ComponentPadding(start = 16.dp, end = 8.dp),
                    textStyle = typography.body.copy(
                        color = colors.textHighEmphasisInverse,
                    ),
                    buttonTextStyle = typography.bodyBold.copy(
                        color = colors.primaryAccent,
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

/**
 * Represents the theme for the floating icons in the audio recording component.
 *
 * @property mic The style for the mic icon.
 * @property lock The style for the lock icon.
 * @property locked The style for the locked icon.
 * @property lockThreshold The threshold for the lock icon.
 * @property lockEdgeOffset The edge offset for the lock icon.
 */
public data class AudioRecordingFloatingIconsTheme(
    val mic: AudioRecordingFloatingIconStyle,
    val lock: AudioRecordingFloatingIconStyle,
    val locked: AudioRecordingFloatingIconStyle,
    val lockThreshold: Dp,
    val lockEdgeOffset: ComponentOffset,
)

/**
 * Represents the theme for the hold to record component.
 *
 * @property containerElevation The elevation of the container.
 * @property containerColor The color of the container.
 * @property containerShape The shape of the container.
 * @property containerPadding The padding of the container.
 * @property contentHeight The height of the content.
 * @property contentPadding The padding of the content.
 * @property textStyle The text style.
 */
public data class AudioRecordingHoldToRecordTheme(
    val containerElevation: Dp,
    val containerColor: Color,
    val containerShape: Shape,
    val containerPadding: ComponentPadding,
    val contentHeight: Dp,
    val contentPadding: ComponentPadding,
    val textStyle: TextStyle,
)

/**
 * Represents the theme for the audio recording controls component.
 *
 * @property height The height of the controls component.
 * @property deleteButton The style for the delete button.
 * @property stopButton The style for the stop button.
 * @property completeButton The style for the complete button.
 */
public data class AudioRecordingControlsTheme(
    val height: Dp,
    val deleteButton: IconContainerStyle,
    val stopButton: IconContainerStyle,
    val completeButton: IconContainerStyle,
)

/**
 * Represents the theme for the permission rationale component.
 *
 * @property containerElevation The elevation of the container.
 * @property containerColor The color of the container.
 * @property containerShape The shape of the container.
 * @property containerPadding The padding of the container.
 * @property containerBottomOffset The bottom offset of the container.
 * @property contentHeight The height of the content.
 * @property contentSpace The space between the content.
 * @property contentPadding The padding of the content.
 * @property textStyle The text style.
 * @property buttonTextStyle The text style for the button.
 */
public data class AudioRecordingPermissionRationaleTheme(
    val containerElevation: Dp,
    val containerColor: Color,
    val containerShape: Shape,
    val containerPadding: ComponentPadding,
    val containerBottomOffset: Dp,
    val contentHeight: Dp,
    val contentSpace: Dp,
    val contentPadding: ComponentPadding,
    val textStyle: TextStyle,
    val buttonTextStyle: TextStyle,
)
