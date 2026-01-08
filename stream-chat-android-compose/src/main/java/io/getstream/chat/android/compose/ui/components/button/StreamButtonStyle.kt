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

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

internal data class StreamButtonStyle(
    val containerColor: Color,
    val contentColor: Color,
    val border: BorderStroke?,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val disabledBorder: BorderStroke?,
)

@Stable
internal fun StreamButtonStyle.contentColor(enabled: Boolean) =
    if (enabled) contentColor else disabledContentColor

@Stable
internal fun StreamButtonStyle.containerColor(enabled: Boolean) =
    if (enabled) containerColor else disabledContainerColor

@Stable
internal fun StreamButtonStyle.border(enabled: Boolean) =
    if (enabled) border else disabledBorder

internal object StreamButtonStyleDefaults {
    val primarySolid: StreamButtonStyle
        @Composable
        get() {
            val colors = ChatTheme.colors
            return StreamButtonStyle(
                containerColor = colors.buttonTypePrimaryBg,
                contentColor = colors.buttonTypePrimaryText,
                border = BorderStroke(1.dp, colors.buttonTypePrimaryBorder),
                disabledContainerColor = colors.buttonTypePrimaryBgDisabled,
                disabledContentColor = colors.buttonTypePrimaryTextDisabled,
                disabledBorder = null,
            )
        }
    val primaryGhost: StreamButtonStyle
        @Composable
        get() {
            val colors = ChatTheme.colors
            return StreamButtonStyle(
                containerColor = colors.buttonStyleGhostBg,
                contentColor = colors.buttonStyleGhostTextPrimary,
                border = BorderStroke(1.dp, colors.buttonStyleGhostBorder),
                disabledContainerColor = colors.buttonStyleGhostBg,
                disabledContentColor = colors.stateTextDisabled,
                disabledBorder = BorderStroke(1.dp, colors.buttonStyleGhostBorder),
            )
        }
    val secondaryOutline: StreamButtonStyle
        @Composable
        get() {
            val colors = ChatTheme.colors
            return StreamButtonStyle(
                containerColor = colors.buttonStyleOutlineBg,
                contentColor = colors.buttonStyleOutlineText,
                border = BorderStroke(1.dp, colors.buttonStyleOutlineBorder),
                disabledContainerColor = colors.buttonStyleOutlineBg,
                disabledContentColor = colors.stateTextDisabled,
                disabledBorder = BorderStroke(1.dp, colors.buttonStyleOutlineBorder),
            )
        }
    val secondaryGhost: StreamButtonStyle
        @Composable
        get() {
            val colors = ChatTheme.colors
            return StreamButtonStyle(
                containerColor = colors.buttonStyleGhostBg,
                contentColor = colors.buttonStyleGhostTextSecondary,
                border = BorderStroke(1.dp, colors.buttonStyleGhostBorder),
                disabledContainerColor = colors.buttonStyleGhostBg,
                disabledContentColor = colors.buttonTypeSecondaryTextDisabled,
                disabledBorder = BorderStroke(1.dp, colors.buttonStyleGhostBorder),
            )
        }

    val destructiveSolid: StreamButtonStyle
        @Composable
        get() {
            val colors = ChatTheme.colors
            return StreamButtonStyle(
                containerColor = colors.buttonTypeDestructiveBg,
                contentColor = colors.buttonTypeDestructiveText,
                border = BorderStroke(1.dp, colors.buttonTypeDestructiveBorder),
                disabledContainerColor = colors.stateBgDisabled,
                disabledContentColor = colors.stateTextDisabled,
                disabledBorder = null,
            )
        }

    val destructiveGhost: StreamButtonStyle
        @Composable
        get() {
            val colors = ChatTheme.colors
            return StreamButtonStyle(
                containerColor = colors.buttonStyleGhostBg,
                contentColor = colors.buttonTypeDestructiveTextInverse,
                border = BorderStroke(1.dp, colors.buttonStyleGhostBorder),
                disabledContainerColor = colors.buttonStyleGhostBg,
                disabledContentColor = colors.stateTextDisabled,
                disabledBorder = BorderStroke(1.dp, colors.buttonStyleGhostBorder),
            )
        }
}
