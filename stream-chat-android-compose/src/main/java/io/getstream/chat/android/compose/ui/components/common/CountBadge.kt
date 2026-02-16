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

package io.getstream.chat.android.compose.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.theme.StreamTypography

@Composable
internal fun CountBadge(
    text: String,
    size: CountBadgeSize,
    modifier: Modifier = Modifier,
    fixedFontSize: Boolean = false,
) {
    val typography = ChatTheme.typography
    val style = size.textStyle(typography)
    val fontSize = if (fixedFontSize) style.fontSize / LocalDensity.current.fontScale else style.fontSize

    Text(
        text = text,
        modifier = modifier
            .shadow(2.dp, CircleShape)
            .defaultMinSize(minWidth = size.minSize, minHeight = size.minSize)
            .background(ChatTheme.colors.badgeBgDefault, CircleShape)
            .padding(horizontal = size.spacing)
            .wrapContentSize(),
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = ChatTheme.typography.numericLarge,
        color = ChatTheme.colors.badgeText,
        fontSize = fontSize,
    )
}

internal enum class CountBadgeSize(val minSize: Dp, val spacing: Dp, val textStyle: (StreamTypography) -> TextStyle) {
    Large(minSize = 32.dp, spacing = StreamTokens.spacingXs, textStyle = StreamTypography::numericLarge),
    Medium(minSize = 24.dp, spacing = StreamTokens.spacingXs, textStyle = StreamTypography::numericLarge),
    Small(minSize = 20.dp, spacing = StreamTokens.spacing2xs, textStyle = StreamTypography::numericMedium),
}

@Preview
@Composable
private fun CountBadgePreview() {
    ChatTheme {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CountBadgeSize.entries.forEach { size ->
                CountBadge(text = "+99", size = size)
            }
        }
    }
}
