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
import androidx.compose.ui.text.TextStyle

/**
 * Represents message date separator theming.
 *
 * @param textStyle The text style for the date separator.
 * @param backgroundColor The background color for the date separator.
 */
@Immutable
public data class MessageDateSeparatorTheme(
    public val textStyle: TextStyle,
    public val backgroundColor: Color,
) {

    public companion object {

        /**
         * Builds the default message date separator theme.
         *
         * @return A [MessageDateSeparatorTheme] instance holding the default theming.
         */
        @Composable
        public fun defaultTheme(
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): MessageDateSeparatorTheme {
            return MessageDateSeparatorTheme(
                textStyle = typography.body.copy(
                    color = colors.textHighEmphasisInverse,
                ),
                backgroundColor = colors.overlayDark,
            )
        }
    }
}
