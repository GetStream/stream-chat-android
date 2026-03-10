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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Shows the default indeterminate loading indicator.
 *
 * @param modifier Modifier for styling.
 */
@Composable
public fun LoadingIndicator(modifier: Modifier = Modifier) {
    val colors = ChatTheme.colors
    Box(
        modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            strokeWidth = 2.dp,
            trackColor = colors.borderCoreDefault,
            color = colors.accentPrimary,
        )
    }
}

/**
 * Shows a determinate loading indicator with progress.
 *
 * @param progress The current progress value between 0f and 1f.
 * @param modifier Modifier for styling.
 */
@Composable
public fun LoadingIndicator(progress: () -> Float, modifier: Modifier = Modifier) {
    val colors = ChatTheme.colors
    val animatedProgress = animateFloatAsState(targetValue = progress(), label = "progress")
    Box(
        modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = animatedProgress::value,
            strokeWidth = 2.dp,
            trackColor = colors.borderCoreDefault,
            color = colors.accentPrimary,
            gapSize = 0.dp,
        )
    }
}
