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

package io.getstream.chat.android.compose.ui.threads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.components.ShimmerProgressIndicator
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens

/**
 * A skeleton loading placeholder that mirrors the layout of a [ThreadItem].
 * Displays an animated shimmer effect while thread data is loading.
 *
 * @param modifier Modifier for styling.
 */
@Composable
internal fun ThreadListLoadingItem(modifier: Modifier = Modifier) {
    val borderColor = ChatTheme.colors.borderCoreSubtle
    val highlightColor = ChatTheme.colors.skeletonLoadingHighlight

    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                val y = size.height - strokeWidth / 2
                drawLine(
                    color = borderColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth,
                )
            }
            .padding(StreamTokens.spacingMd),
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingSm),
        verticalAlignment = Alignment.Top,
    ) {
        // Avatar
        ShimmerProgressIndicator(
            modifier = Modifier
                .size(AvatarSize.ExtraLarge)
                .clip(CircleShape),
            highlightColor = highlightColor,
        )

        // Content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
        ) {
            // Channel title + message preview
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = StreamTokens.spacing2xs),
                verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
            ) {
                ShimmerProgressIndicator(
                    modifier = Modifier
                        .width(120.dp)
                        .height(StreamTokens.size12)
                        .clip(RoundedCornerShape(StreamTokens.radiusFull)),
                    highlightColor = highlightColor,
                )
                ShimmerProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(RoundedCornerShape(StreamTokens.radiusFull)),
                    highlightColor = highlightColor,
                )
            }

            // Reply footer
            Row(
                horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ShimmerProgressIndicator(
                    modifier = Modifier
                        .size(AvatarSize.Small)
                        .clip(CircleShape),
                    highlightColor = highlightColor,
                )
                ShimmerProgressIndicator(
                    modifier = Modifier
                        .width(64.dp)
                        .height(StreamTokens.size12)
                        .clip(RoundedCornerShape(StreamTokens.radiusFull)),
                    highlightColor = highlightColor,
                )
                ShimmerProgressIndicator(
                    modifier = Modifier
                        .width(64.dp)
                        .height(StreamTokens.size12)
                        .clip(RoundedCornerShape(StreamTokens.radiusFull)),
                    highlightColor = highlightColor,
                )
            }
        }

        ShimmerProgressIndicator(
            modifier = Modifier
                .width(48.dp)
                .height(StreamTokens.size16)
                .clip(RoundedCornerShape(StreamTokens.radiusFull)),
            highlightColor = highlightColor,
        )
    }
}

@Preview
@Composable
private fun ThreadListLoadingItemPreview() {
    ChatTheme {
        Surface {
            ThreadListLoadingItem()
        }
    }
}
