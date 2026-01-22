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

package io.getstream.chat.android.compose.ui.theme.messages.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamShapes
import io.getstream.chat.android.compose.ui.theme.StreamTypography

/**
 * Represents the style for poll messages.
 *
 * @param backgroundColor The background color for the poll message.
 */
public data class PollMessageStyle(
    val backgroundColor: Color,
) {

    public companion object {

        /**
         * Builds the default poll message style for the current user's messages.
         *
         * Returns a [PollMessageStyle] instance holding our default theming.
         */
        @Suppress("DEPRECATION_ERROR")
        @Composable
        public fun defaultStyle(
            own: Boolean,
            isInDarkMode: Boolean,
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isInDarkMode) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
            shapes: StreamShapes = StreamShapes.defaultShapes(),
        ): PollMessageStyle {
            return PollMessageStyle(
                backgroundColor = when (own) {
                    true -> colors.chatBgOutgoing
                    else -> colors.chatBgIncoming
                },
            )
        }
    }
}
