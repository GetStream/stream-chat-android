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

package io.getstream.chat.android.ai.compose.ui.component.internal

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

/**
 * A highlight sweeping across the content while something is in progress. It runs on the system's
 * animation clock, so it stays still when animations are turned off.
 */
internal fun Modifier.shimmer(active: Boolean, highlight: Color): Modifier = if (!active) {
    this
} else {
    composed {
        val progress by rememberInfiniteTransition(label = "shimmer").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(ShimmerMillis, easing = LinearEasing)),
            label = "shimmer-progress",
        )
        graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                val band = size.width * BandWidth
                val start = -band + (size.width + band) * progress
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, highlight, Color.Transparent),
                        startX = start,
                        endX = start + band,
                    ),
                    blendMode = BlendMode.SrcAtop,
                )
            }
    }
}

/** A gentle pulse of the content's opacity while something is in progress. */
internal fun Modifier.pulse(active: Boolean): Modifier = if (!active) {
    this
} else {
    composed {
        val alpha by rememberInfiniteTransition(label = "pulse").animateFloat(
            initialValue = 1f,
            targetValue = PulseMinAlpha,
            animationSpec = infiniteRepeatable(tween(PulseMillis), RepeatMode.Reverse),
            label = "pulse-alpha",
        )
        graphicsLayer { this.alpha = alpha }
    }
}

private const val ShimmerMillis = 1400
private const val BandWidth = 0.6f
private const val PulseMillis = 900
private const val PulseMinAlpha = 0.35f
