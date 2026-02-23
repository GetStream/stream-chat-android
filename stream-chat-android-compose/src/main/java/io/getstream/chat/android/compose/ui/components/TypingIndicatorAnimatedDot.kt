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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import kotlinx.coroutines.delay

/**
 * Show a dot with infinite alpha animation.
 *
 * @param initialDelayMillis The delay before the animation is started in millis.
 */
@Composable
public fun TypingIndicatorAnimatedDot(
    initialDelayMillis: Int,
) {
    val alpha = remember { Animatable(0.5f) }

    LaunchedEffect(initialDelayMillis) {
        delay(initialDelayMillis.toLong())
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = DotAnimationDurationMillis,
                    delayMillis = DotAnimationDurationMillis,
                ),
                repeatMode = RepeatMode.Reverse,
            ),
        )
    }

    val color: Color = ChatTheme.colors.textSecondary.copy(alpha = alpha.value)

    Box(
        Modifier
            .background(color, CircleShape)
            .size(5.dp),
    )
}

/**
 * The animation duration of each dot.
 */
public const val DotAnimationDurationMillis: Int = 200
