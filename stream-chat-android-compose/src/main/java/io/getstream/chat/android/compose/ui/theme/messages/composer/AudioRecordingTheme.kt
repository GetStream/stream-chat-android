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
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
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
 */
public data class AudioRecordingTheme(
    val enabled: Boolean,
    val sendOnComplete: Boolean,
    val showRecordButtonOverSend: Boolean,
    val recordButton: IconContainerStyle,
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
            )
        }
    }
}
