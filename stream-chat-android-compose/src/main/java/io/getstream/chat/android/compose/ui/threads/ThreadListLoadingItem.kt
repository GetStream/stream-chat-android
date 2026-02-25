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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * A skeleton loading placeholder that mirrors the layout of a [ThreadItem].
 * Displays an animated shimmer effect while thread data is loading.
 *
 * @param modifier Modifier for styling.
 */
@Suppress("LongMethod")
@Composable
internal fun ThreadListLoadingItem(modifier: Modifier = Modifier) {
    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.View)
    val baseColor = ChatTheme.colors.backgroundCoreSurface
    val highlightColor = ChatTheme.colors.skeletonLoadingHighlight
    val borderColor = ChatTheme.colors.borderCoreSubtle

    @Composable
    fun Modifier.skeleton(): Modifier = background(baseColor)
        .shimmer(shimmer)
        .background(highlightColor)

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
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .skeleton(),
        )

        // Content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Channel title + message preview
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Channel / thread title
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .skeleton(),
                )
                // Message preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .skeleton(),
                )
            }

            // Reply footer
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Reply author avatar
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .skeleton(),
                )
                // Reply author name
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .skeleton(),
                )
                // Reply preview text
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .skeleton(),
                )
            }
        }

        // Timestamp
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(percent = 50))
                .skeleton(),
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
