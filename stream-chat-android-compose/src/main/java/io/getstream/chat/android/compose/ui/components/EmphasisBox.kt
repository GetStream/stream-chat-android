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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * A box that can be used to emphasize its content by adding a transparent overlay on top of it.
 */
@Composable
internal fun EmphasisBox(
    modifier: Modifier = Modifier,
    isEmphasized: Boolean = true,
    content: @Composable () -> Unit,
) {
    val color = ChatTheme.colors.textSecondary
    val alpha by animateFloatAsState(targetValue = if (isEmphasized) EmphasisTransparency else 0f)
    Box(
        modifier = modifier.drawWithContent {
            drawContent()
            drawRect(color = color, alpha = alpha, size = size)
        },
    ) {
        content()
    }
}

private const val EmphasisTransparency = .3f
