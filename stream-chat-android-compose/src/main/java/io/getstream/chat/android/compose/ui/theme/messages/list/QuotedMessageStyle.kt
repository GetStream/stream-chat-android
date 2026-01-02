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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ComponentPadding
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamShapes
import io.getstream.chat.android.compose.ui.theme.StreamTypography

/**
 * Represents the style for quoted messages.
 *
 * @param textStyle The text style for the quoted message.
 * @param backgroundColor The background color for the quoted message.
 * @param backgroundShape The shape for the quoted message background.
 * @param backgroundBorder The border for the quoted message background.
 * @param contentPadding The padding for the quoted message content.
 */
// TODO [G.] do we really need this style? What do other SDKs do
//  Also, in designs this is consistent with the link preview, does it make sense to have different classes?
public data class QuotedMessageStyle(
    val textStyle: TextStyle,
    val backgroundColor: Color,
    // TODO [G.] better name, assuming we keep it
    val backgroundColorInComposer: Color,
    val backgroundShape: Shape,
    val backgroundBorder: BorderStroke?,
    val indicatorColor: Color,
    val contentPadding: ComponentPadding,
) {

    public companion object {

        /**
         * Builds the default quoted message style for the current user's messages.
         *
         * Returns a [QuotedMessageStyle] instance holding our default theming.
         */
        @Composable
        public fun defaultOwnStyle(
            isInDarkMode: Boolean,
            typography: StreamTypography,
            colors: StreamColors,
            shapes: StreamShapes,
        ): QuotedMessageStyle {
            return defaultStyle(
                own = true,
                isInDarkMode = isInDarkMode,
                typography = typography,
                colors = colors,
                shapes = shapes,
            )
        }

        /**
         * Builds the default quoted message style for other users' messages.
         *
         * Returns a [QuotedMessageStyle] instance holding our default theming.
         */
        @Composable
        public fun defaultOtherStyle(
            isInDarkMode: Boolean,
            typography: StreamTypography = StreamTypography.defaultTypography(),
            colors: StreamColors = when (isInDarkMode) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
            shapes: StreamShapes = StreamShapes.defaultShapes(),
        ): QuotedMessageStyle {
            return defaultStyle(
                own = false,
                isInDarkMode = isInDarkMode,
                typography = typography,
                colors = colors,
                shapes = shapes,
            )
        }

        /**
         * Builds the default quoted message style.
         *
         * Returns a [QuotedMessageStyle] instance holding our default theming.
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
        ): QuotedMessageStyle {
            // TODO [G.] consider lifting the if(own) check outside, to do it only once
            return QuotedMessageStyle(
                textStyle = typography.body.copy(
                    color = when (own) {
                        true -> colors.ownMessageQuotedText
                        else -> colors.otherMessageQuotedText
                    },
                ),
                backgroundColor = when (own) {
                    true -> attachmentBackgroundColorOutgoing
                    else -> attachmentBackgroundColorIncoming
                },
                backgroundColorInComposer = when (own) {
                    true -> backgroundColorOutgoing
                    false -> backgroundColorIncoming
                },
                backgroundShape = shape,
                backgroundBorder = null,
                indicatorColor = when (own) {
                    true -> indicatorColorOutgoing
                    false -> indicatorColorIncoming
                },
                contentPadding = ComponentPadding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            )
        }
    }
}

// TODO [G.]
private val indicatorColorIncoming = Color(0xFFB8BEC4)
private val indicatorColorOutgoing = Color(0xFF4E8BFF)
private val backgroundColorIncoming = Color(0xFFF2F4F6)
private val backgroundColorOutgoing = Color(0xFFD2E3FF)
public val attachmentBackgroundColorIncoming: Color = Color(0xFFE2E6EA)
public val attachmentBackgroundColorOutgoing: Color = Color(0xFFA6C4FF)
private val shape = RoundedCornerShape(12.dp)
