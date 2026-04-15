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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.components.ShimmerProgressIndicator

/**
 * A skeleton shimmer placeholder item that mirrors the channel item layout.
 * Used as loading content while channels are being fetched.
 */
@Composable
internal fun ChannelListLoadingItem(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar placeholder
        ShimmerProgressIndicator(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
        ) {
            // Title + timestamp row
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ShimmerProgressIndicator(
                    modifier = Modifier
                        .weight(1f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp)),
                )
                Spacer(modifier = Modifier.width(16.dp))
                ShimmerProgressIndicator(
                    modifier = Modifier
                        .width(48.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp)),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Message preview placeholder
            ShimmerProgressIndicator(
                modifier = Modifier
                    .width(200.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp)),
            )
        }
    }
}
