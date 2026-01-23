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

package io.getstream.chat.android.compose.ui.components.avatar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/** Selects avatar placeholder colors from the avatar color palette based on the [identifier]. */
@Suppress("MagicNumber")
@Composable
internal fun rememberAvatarPlaceholderColors(identifier: Any): Pair<Color, Color> {
    val colors = ChatTheme.colors

    return remember(identifier, colors) {
        when (identifier.hashCode() % 5 + 1) {
            1 -> colors.avatarPaletteBg1 to colors.avatarPaletteText1
            2 -> colors.avatarPaletteBg2 to colors.avatarPaletteText2
            3 -> colors.avatarPaletteBg3 to colors.avatarPaletteText3
            4 -> colors.avatarPaletteBg4 to colors.avatarPaletteText4
            else -> colors.avatarPaletteBg5 to colors.avatarPaletteText5
        }
    }
}

internal fun Dp.toPlaceholderIconSize() = when {
    this >= AvatarSize.Large -> 20.dp
    this >= AvatarSize.Medium -> 16.dp
    this >= AvatarSize.Small -> 12.dp
    else -> 10.dp
}

@Composable
internal fun Dp.toPlaceholderTextStyle(): TextStyle {
    val typography = ChatTheme.typography
    return when {
        this >= AvatarSize.Large -> typography.bodyEmphasis
        this >= AvatarSize.Medium -> typography.captionEmphasis
        this >= AvatarSize.Small -> typography.captionEmphasis
        else -> typography.metadataEmphasis
    }
}
