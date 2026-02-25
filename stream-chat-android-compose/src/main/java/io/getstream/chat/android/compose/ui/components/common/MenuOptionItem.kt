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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.clickable

/**
 * Represents a generic menu option item that can be used in a list of options.
 *
 * @param modifier Modifier for styling.
 * @param onClick Handler called when the item is clicked.
 * @param leadingIcon The icon to show on the left side of the item.
 * @param title The title of the item.
 * @param titleColor The color of the title.
 * @param style The style of the title.
 * @param itemHeight The height of the item.
 * @param verticalAlignment Used to apply vertical alignment.
 * @param horizontalArrangement Used to apply horizontal arrangement.
 */
@Composable
public fun MenuOptionItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    leadingIcon: @Composable RowScope.() -> Unit,
    title: String,
    titleColor: Color,
    style: TextStyle = ChatTheme.typography.bodyEmphasis,
    itemHeight: Dp = 56.dp,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(itemHeight)
            .clickable(onClick = onClick),
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement,
    ) {
        leadingIcon()
        Text(
            modifier = Modifier.testTag("Stream_ContextMenu_$title"),
            text = title,
            style = style,
            color = titleColor,
        )
    }
}
