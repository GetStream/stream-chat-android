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

package io.getstream.chat.android.compose.ui.components.button

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import io.getstream.chat.android.compose.ui.theme.ChatTheme

internal data class StreamButtonStyle(
    val containerColor: Color?,
    val contentColor: Color,
    val borderColor: Color?,
    val disabledContainerColor: Color?,
    val disabledContentColor: Color,
    val disabledBorderColor: Color?,
)

@Stable
internal fun StreamButtonStyle.contentColor(enabled: Boolean) =
    if (enabled) contentColor else disabledContentColor

@Stable
internal fun StreamButtonStyle.containerColor(enabled: Boolean) =
    if (enabled) containerColor else disabledContainerColor

@Stable
internal fun StreamButtonStyle.borderColor(enabled: Boolean) =
    if (enabled) borderColor else disabledBorderColor

internal object StreamButtonStyleDefaults {
    val primarySolid: StreamButtonStyle
        @Composable
        get() {
            val colors = ChatTheme.colors
            return StreamButtonStyle(
                containerColor = colors.buttonPrimaryBg,
                contentColor = colors.buttonPrimaryTextOnAccent,
                borderColor = null,
                disabledContainerColor = colors.backgroundCoreDisabled,
                disabledContentColor = colors.textDisabled,
                disabledBorderColor = null,
            )
        }
    val primaryOutline: StreamButtonStyle
        @Composable
        get() {
            val colors = ChatTheme.colors
            return StreamButtonStyle(
                containerColor = null,
                contentColor = colors.buttonPrimaryText,
                borderColor = colors.buttonPrimaryBorder,
                disabledContainerColor = null,
                disabledContentColor = colors.textDisabled,
                disabledBorderColor = colors.borderUtilityDisabled,
            )
        }
    val primaryGhost: StreamButtonStyle
        @Composable
        get() {
            val colors = ChatTheme.colors
            return StreamButtonStyle(
                containerColor = null,
                contentColor = colors.buttonPrimaryText,
                borderColor = null,
                disabledContainerColor = null,
                disabledContentColor = colors.textDisabled,
                disabledBorderColor = null,
            )
        }
    val secondarySolid: StreamButtonStyle
        @Composable
        get() {
            val colors = ChatTheme.colors
            return StreamButtonStyle(
                containerColor = colors.buttonSecondaryBg,
                contentColor = colors.buttonSecondaryTextOnAccent,
                borderColor = null,
                disabledContainerColor = colors.backgroundCoreDisabled,
                disabledContentColor = colors.textDisabled,
                disabledBorderColor = null,
            )
        }
    val secondaryOutline: StreamButtonStyle
        @Composable
        get() {
            val colors = ChatTheme.colors
            return StreamButtonStyle(
                containerColor = null,
                contentColor = colors.buttonSecondaryText,
                borderColor = colors.buttonSecondaryBorder,
                disabledContainerColor = null,
                disabledContentColor = colors.textDisabled,
                disabledBorderColor = colors.buttonSecondaryBorder,
            )
        }
    val secondaryGhost: StreamButtonStyle
        @Composable
        get() {
            val colors = ChatTheme.colors
            return StreamButtonStyle(
                containerColor = null,
                contentColor = colors.buttonSecondaryText,
                borderColor = null,
                disabledContainerColor = null,
                disabledContentColor = colors.textDisabled,
                disabledBorderColor = null,
            )
        }
    val destructiveSolid: StreamButtonStyle
        @Composable
        get() {
            val colors = ChatTheme.colors
            return StreamButtonStyle(
                containerColor = colors.buttonDestructiveBg,
                contentColor = colors.buttonDestructiveTextOnAccent,
                borderColor = null,
                disabledContainerColor = colors.backgroundCoreDisabled,
                disabledContentColor = colors.textDisabled,
                disabledBorderColor = null,
            )
        }
    val destructiveOutline: StreamButtonStyle
        @Composable
        get() {
            val colors = ChatTheme.colors
            return StreamButtonStyle(
                containerColor = null,
                contentColor = colors.buttonDestructiveText,
                borderColor = colors.buttonDestructiveBorder,
                disabledContainerColor = null,
                disabledContentColor = colors.textDisabled,
                disabledBorderColor = colors.borderUtilityDisabled,
            )
        }
    val destructiveGhost: StreamButtonStyle
        @Composable
        get() {
            val colors = ChatTheme.colors
            return StreamButtonStyle(
                containerColor = null,
                contentColor = colors.buttonDestructiveText,
                borderColor = null,
                disabledContainerColor = null,
                disabledContentColor = colors.textDisabled,
                disabledBorderColor = null,
            )
        }
}
