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

package io.getstream.chat.android.compose.sample.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

@Composable
internal fun PaneTitle(
    text: String,
    padding: PaddingValues = PaddingValues(
        top = 24.dp,
        start = 16.dp,
        bottom = 8.dp,
        end = 16.dp,
    ),
) {
    Text(
        modifier = Modifier.padding(padding),
        text = text,
        style = ChatTheme.typography.footnote,
        color = ChatTheme.colors.textSecondary,
    )
}

@Composable
internal fun PaneRow(
    index: Int,
    lastIndex: Int,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .run {
                val shape = when (index) {
                    0 -> if (lastIndex == 0) {
                        // Single item in the list
                        RoundedCornerShape(12.dp)
                    } else {
                        // Top item in the list
                        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    }
                    // Bottom item in the list
                    lastIndex -> RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                    // Middle item in the list
                    else -> RectangleShape
                }
                background(
                    color = ChatTheme.colors.backgroundElevationElevation1,
                    shape = shape,
                )
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        content = content,
    )
}
