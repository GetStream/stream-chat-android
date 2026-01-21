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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

@Composable
internal fun OnlineIndicator(
    isOnline: Boolean,
    size: OnlineIndicatorSize,
    modifier: Modifier = Modifier,
) {
    val colors = ChatTheme.colors

    val color = when (isOnline) {
        true -> colors.presenceBgOnline
        false -> colors.presenceBgOffline
    }

    Box(
        modifier
            .size(size.indicatorSize)
            .border(size.borderWidth, colors.presenceBorder, CircleShape)
            .padding(size.borderWidth)
            .background(color, CircleShape),
    )
}

internal enum class OnlineIndicatorSize(val indicatorSize: Dp, val borderWidth: Dp) {
    Small(8.dp, 1.dp),
    Medium(12.dp, 2.dp),
    Large(14.dp, 2.dp),
}
