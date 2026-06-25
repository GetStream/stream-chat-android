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

package io.getstream.chat.android.compose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens

/**
 * A pill-shaped badge used to display a short label, such as a member role.
 *
 * Provide the colors for the desired style, for example a primary (brand) color pair for an owner
 * badge and a neutral color pair for other roles.
 *
 * @param text The label to display inside the badge.
 * @param backgroundColor The badge background color.
 * @param textColor The color of the [text].
 * @param modifier The [Modifier] to be applied to this badge.
 */
@Composable
public fun RoleBadge(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(StreamTokens.radiusFull),
            )
            .padding(
                horizontal = StreamTokens.spacingXs,
                vertical = StreamTokens.spacing3xs,
            ),
        text = text,
        style = ChatTheme.typography.metadataEmphasis,
        color = textColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
