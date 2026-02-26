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

package io.getstream.chat.android.compose.ui.channels.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * A single swipe action button displayed behind a channel list item.
 *
 * @param icon The icon to display.
 * @param label Accessibility label used as the icon's content description.
 * @param onClick Called when this action is tapped.
 * @param backgroundColor The background color of this action.
 * @param contentColor The color for the icon and label.
 * @param modifier Modifier for styling.
 */
@Composable
public fun SwipeActionItem(
    icon: Painter,
    label: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(80.dp)
            .fillMaxHeight()
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
    }
}

/**
 * A single swipe action button using [SwipeActionStyle] for slot-based coloring.
 *
 * @param icon The icon to display.
 * @param label Accessibility label used as the icon's content description.
 * @param onClick Called when this action is tapped.
 * @param style The visual style determining background and content colors.
 * @param modifier Modifier for styling.
 */
@Composable
public fun SwipeActionItem(
    icon: Painter,
    label: String,
    onClick: () -> Unit,
    style: SwipeActionStyle,
    modifier: Modifier = Modifier,
) {
    val colors = ChatTheme.colors
    val (bg, content) = when (style) {
        SwipeActionStyle.Primary -> colors.accentPrimary to colors.buttonPrimaryTextOnAccent
        SwipeActionStyle.Secondary -> colors.backgroundCoreSurface to colors.textPrimary
        SwipeActionStyle.Destructive -> colors.accentError to colors.buttonDestructiveTextOnAccent
    }
    SwipeActionItem(
        icon = icon,
        label = label,
        onClick = onClick,
        backgroundColor = bg,
        contentColor = content,
        modifier = modifier,
    )
}
