/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import io.getstream.chat.android.compose.R

/**
 * Represents the theming for the message composer.
 * @param attachmentCancelIcon The theming for the cancel icon used in the message composer.
 */
public data class MessageComposerTheme(
    val attachmentCancelIcon: ComposerCancelIconStyle,
) {

    public companion object {

        /**
         * Builds the default message composer theme.
         *
         * @return A [MessageComposerTheme] instance holding the default theming.
         */
        @Composable
        public fun defaultTheme(
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): MessageComposerTheme {
            return MessageComposerTheme(
                attachmentCancelIcon = ComposerCancelIconStyle(
                    backgroundShape = CircleShape,
                    backgroundColor = colors.overlayDark,
                    painter = painterResource(id = R.drawable.stream_compose_ic_close),
                    tint = colors.appBackground,
                ),
            )
        }
    }
}

/**
 * Represents the theming for the cancel icon used in the message composer.
 *
 * @param backgroundShape The shape of the background for the cancel icon.
 * @param backgroundColor The background color for the cancel icon.
 * @param tint The tint color for the cancel icon.
 */
public data class ComposerCancelIconStyle(
    val backgroundShape: Shape,
    val backgroundColor: Color,
    val painter: Painter,
    val tint: Color,
)
