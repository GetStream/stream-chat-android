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

package io.getstream.chat.android.compose.ui.theme.messages.composer.attachments

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamTypography

/**
 * Represents the theming for the attachments preview.
 *
 * @param audioRecording The theming for the audio recording attachment preview.
 */
public data class AttachmentsPreviewTheme(
    val audioRecording: AudioRecordingAttachmentPreviewTheme,
) {

    public companion object {

        /**
         * Builds the default theming for the attachments preview.
         */
        @Composable
        public fun defaultTheme(
            isInDarkMode: Boolean = isSystemInDarkTheme(),
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isInDarkMode) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): AttachmentsPreviewTheme {
            return AttachmentsPreviewTheme(
                audioRecording = AudioRecordingAttachmentPreviewTheme.defaultTheme(isInDarkMode, typography, colors),
            )
        }
    }
}
