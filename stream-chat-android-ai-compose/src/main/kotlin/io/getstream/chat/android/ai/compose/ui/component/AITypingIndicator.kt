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

package io.getstream.chat.android.ai.compose.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * A typing indicator composable that displays an optional label and an animated indicator.
 *
 * By default, displays three animated dots that sequentially highlight in a smooth, overlapping
 * animation. The indicator uses [LocalContentColor] to match the current theme's content color.
 *
 * @param modifier Modifier to be applied to the root Row container
 * @param label Optional composable label to display before the indicator. Defaults to empty content.
 * @param indicator Composable indicator to display. Defaults to [AnimatedDots] which shows three
 *   animated dots with sequential highlighting.
 */
@Composable
public fun AITypingIndicator(
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit = {
        with(LocalChatAiComponentFactory.current) {
            AITypingIndicatorLabel(AITypingIndicatorLabelParams())
        }
    },
    indicator: @Composable () -> Unit = {
        with(LocalChatAiComponentFactory.current) {
            AITypingIndicatorIndicator(AITypingIndicatorIndicatorParams())
        }
    },
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        label()
        indicator()
    }
}

/**
 * Displays three animated dots with sequential highlighting animation.
 * Uses [LocalContentColor] for the dot color.
 */
@Composable
internal fun AnimatedDots(modifier: Modifier = Modifier) {
    val contentColor = LocalContentColor.current
    val infiniteTransition = rememberInfiniteTransition(label = "dots_transition")
    val progress by infiniteTransition.animateFloat(
        initialValue = ProgressStart,
        targetValue = ProgressFull,
        animationSpec = infiniteRepeatable(
            animation = tween(DotCycleDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(DotCount) { index ->
            AnimatedDot(index, progress, contentColor)
        }
    }
}

/**
 * A single animated dot that fades in and out based on its position in the sequence.
 * Each dot has a staggered start time to create a sequential highlighting effect.
 */
@Composable
private fun AnimatedDot(
    index: Int,
    progress: Float,
    contentColor: Color,
) {
    val startOffset = (index * DotStaggerDelay).toFloat() / DotCycleDuration
    val t = ((progress - startOffset + ProgressFull) % ProgressFull) * DotCycleDuration / DotAnimationDuration

    val alpha = when {
        t <= ProgressStart || t >= ProgressFull -> DotMinAlpha
        else -> {
            val eased = smoothstep(
                if (t <= AnimationMidpoint) {
                    t * AnimationDouble
                } else {
                    (ProgressFull - t) * AnimationDouble
                },
            )
            DotMinAlpha + (DotMaxAlpha - DotMinAlpha) * eased
        }
    }

    Dot(alpha = alpha, contentColor = contentColor)
}

/**
 * Smoothstep interpolation function for easing animation transitions.
 * Provides a smooth S-curve transition between 0 and 1.
 */
private fun smoothstep(t: Float) = t * t * (SmoothstepFactor1 - SmoothstepFactor2 * t)

/**
 * Renders a single circular dot with the specified alpha and color.
 */
@Composable
private fun Dot(
    alpha: Float,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(6.dp)
            .graphicsLayer {
                this.alpha = alpha
            }
            .background(
                color = contentColor,
                shape = CircleShape,
            ),
    )
}

private const val DotCount = 3
private const val DotMinAlpha = 0.3f
private const val DotMaxAlpha = 1f
private const val DotAnimationDuration = 600
private const val DotStaggerDelay = 200
private const val DotCycleDuration = DotAnimationDuration + (DotCount - 1) * DotStaggerDelay

// Animation calculation constants
private const val ProgressStart = 0f
private const val ProgressFull = 1f
private const val AnimationMidpoint = 0.5f
private const val AnimationDouble = 2f
private const val SmoothstepFactor1 = 3f
private const val SmoothstepFactor2 = 2f

@Composable
internal fun AITypingIndicatorWithLabel() {
    AITypingIndicator(label = { Text(text = "Thinking") })
}

@Preview(showBackground = true)
@Composable
private fun AITypingIndicatorWithLabelPreview() {
    AITypingIndicatorWithLabel()
}
