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

package io.getstream.chat.android.compose.ui.components.audio

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.dragPointerInput

/**
 * A progress bar matching the Figma "Mobile / Playback Progress Bar" component.
 *
 * @param progress The current progress (0f..1f).
 * @param isPlaying Whether playback is active (changes thumb and track colors).
 * @param modifier The [Modifier] to be applied.
 * @param enabled Whether the bar accepts drags. Disable it when there is nothing to seek.
 * @param animationDurationMs How long the progress takes to animate to a new value while playing. Match this to the
 * cadence of the progress updates so the thumb travels smoothly instead of stepping.
 * @param onDragStart Callback when the user starts dragging.
 * @param onDrag Callback during drag with the current progress.
 * @param onDragStop Callback when the user stops dragging with the final progress.
 */
@Composable
internal fun PlaybackSlider(
    progress: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    animationDurationMs: Int = DefaultAnimationDurationMs,
    onDragStart: (Float) -> Unit = {},
    onDrag: (Float) -> Unit = {},
    onDragStop: (Float) -> Unit = {},
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val currentProgress by rememberUpdatedState(progress)
    var widthPx by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = if (isPlaying) {
            tween(durationMillis = animationDurationMs, easing = LinearEasing)
        } else {
            snap()
        },
        label = "playback-progress",
    )
    Box(
        modifier = modifier
            .progressSemantics(value = progress)
            .onSizeChanged { size -> widthPx = size.width.toFloat() }
            .dragPointerInput(
                enabled = enabled,
                onDragStart = { onDragStart(it.toHorizontalProgress(widthPx, isRtl)) },
                onDrag = { onDrag(it.toHorizontalProgress(widthPx, isRtl)) },
                onDragStop = { onDragStop(it?.toHorizontalProgress(widthPx, isRtl) ?: currentProgress) },
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TrackHeight)
                .clip(CircleShape)
                .background(ChatTheme.colors.chatWaveformBar),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = animatedProgress)
                .height(TrackHeight)
                .clip(CircleShape)
                .background(ChatTheme.colors.chatWaveformBarPlaying),
        )
        PlaybackThumb(progress = animatedProgress, isPlaying = isPlaying, parentWidthPx = widthPx)
    }
}

@Composable
private fun BoxScope.PlaybackThumb(
    progress: Float,
    isPlaying: Boolean,
    parentWidthPx: Float,
) {
    val thumbOffset = if (parentWidthPx > 0) {
        with(LocalDensity.current) {
            val parentWidth = parentWidthPx.toDp()
            val center = parentWidth * progress
            val left = center - (ThumbSize / 2)
            left.coerceIn(0.dp, parentWidth - ThumbSize)
        }
    } else {
        0.dp
    }
    val colors = ChatTheme.colors
    val bgColor = if (isPlaying) {
        colors.controlPlaybackThumbBgActive
    } else {
        colors.controlPlaybackThumbBgDefault
    }
    val borderColor = if (isPlaying) {
        colors.controlPlaybackThumbBorderActive
    } else {
        colors.controlPlaybackThumbBorderDefault
    }
    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .offset(x = thumbOffset)
            .size(ThumbSize)
            .shadow(2.dp, CircleShape)
            .background(bgColor, CircleShape)
            .border(1.dp, borderColor, CircleShape),
    )
}

private fun Offset.toHorizontalProgress(widthPx: Float, isRtl: Boolean): Float {
    val raw = (x / widthPx).coerceIn(0f, 1f)
    return if (isRtl) 1f - raw else raw
}

private const val DefaultAnimationDurationMs = 100
private val TrackHeight = 4.dp
private val ThumbSize = 12.dp

@Preview(showBackground = true)
@Composable
private fun PlaybackSliderPreview() {
    ChatPreviewTheme {
        PlaybackSlider(
            progress = 0.4f,
            isPlaying = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp),
        )
    }
}
