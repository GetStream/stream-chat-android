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
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Represents the theming for the attachment picker.
 *
 * @param backgroundOverlay The overlay background color.
 * @param backgroundSecondary The secondary background color.
 * @param backgroundPrimary The primary background color.
 * @param checkIconBackgroundColor The background color of the check icon.
 * @param checkIconTintColor The tint color of the check icon.
 * @param contentColor The content color used for text and icons.
 * @param saveAttachmentsOnDismiss If the selected attachments should be saved when the picker is dismissed.
 * @param loadAttachmentsAsync If file attachments should be read from the filesystem asynchronously.
 */
@Immutable
public data class AttachmentPickerTheme(
    val backgroundOverlay: Color,
    val backgroundSecondary: Color,
    val backgroundPrimary: Color,
    val checkIconBackgroundColor: Color,
    val checkIconTintColor: Color,
    val contentColor: Color,
    val saveAttachmentsOnDismiss: Boolean,
    val loadAttachmentsAsync: Boolean = true,
) {

    public companion object {

        /**
         * Builds the default attachment picker theme.
         *
         * @return A [AttachmentPickerTheme] instance holding the default theming.
         */
        @Composable
        public fun defaultTheme(
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): AttachmentPickerTheme {
            return AttachmentPickerTheme(
                backgroundOverlay = colors.overlay,
                backgroundSecondary = colors.inputBackground,
                backgroundPrimary = colors.barsBackground,
                checkIconBackgroundColor = colors.overlayDark,
                checkIconTintColor = colors.textHighEmphasisInverse,
                contentColor = colors.overlayDark,
                saveAttachmentsOnDismiss = false,
                loadAttachmentsAsync = true,
            )
        }
    }
}
