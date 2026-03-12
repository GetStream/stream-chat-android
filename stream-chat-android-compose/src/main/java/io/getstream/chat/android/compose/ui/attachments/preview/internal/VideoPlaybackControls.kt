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

package io.getstream.chat.android.compose.ui.attachments.preview.internal

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.button.SpeedButton
import io.getstream.chat.android.compose.ui.components.button.StreamButton
import io.getstream.chat.android.compose.ui.components.button.StreamButtonSize
import io.getstream.chat.android.compose.ui.components.button.StreamButtonStyleDefaults
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.dragPointerInput
import kotlinx.coroutines.delay

/**
 * A composable that displays video playback controls for the media gallery.
 *
 * Contains play/pause button, current time, seek bar, and speed toggle.
 *
 * @param player The [Player] instance to control.
 * @param modifier The [Modifier] to be applied.
 */
@Composable
internal fun VideoPlaybackControls(
    player: Player,
    modifier: Modifier = Modifier,
) {
    val state = rememberVideoPlaybackControlsState(player)

    Row(
        modifier = modifier.padding(start = StreamTokens.spacingSm, end = StreamTokens.spacingMd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Play/Pause button
        StreamButton(
            onClick = state::togglePlayPause,
            style = StreamButtonStyleDefaults.secondaryGhost,
            size = StreamButtonSize.Small,
            modifier = Modifier.minimumInteractiveComponentSize(),
        ) {
            val icon = if (state.isPlaying) {
                R.drawable.stream_compose_ic_pause
            } else {
                R.drawable.stream_compose_ic_play
            }
            val contentDescription = if (state.isPlaying) {
                R.string.stream_compose_audio_playback_pause
            } else {
                R.string.stream_compose_cd_play_button
            }
            Icon(
                painter = painterResource(icon),
                contentDescription = stringResource(contentDescription),
                modifier = Modifier.size(20.dp),
            )
        }

        Text(
            text = ChatTheme.durationFormatter.format(state.currentPosition.toInt()),
            style = ChatTheme.typography.captionDefault,
            color = if (state.isPlaying) ChatTheme.colors.accentPrimary else ChatTheme.colors.textPrimary,
        )

        PlaybackSlider(
            progress = state.progress,
            isPlaying = state.isPlaying,
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
                .padding(horizontal = StreamTokens.spacingMd),
            onDragStart = { state.onDragStart() },
            onDrag = state::onDrag,
            onDragStop = state::onDragStop,
        )

        SpeedButton(
            speed = state.speed,
            onClick = state::cycleSpeed,
        )
    }
}

/**
 * A progress bar matching the Figma "Mobile / Playback Progress Bar" component.
 *
 * Displays a rounded track (4dp) with a 12dp white circular thumb with border and shadow.
 *
 * @param progress The current progress (0f..1f).
 * @param isPlaying Whether playback is active (changes thumb and track colors).
 * @param modifier The [Modifier] to be applied.
 * @param onDragStart Callback when the user starts dragging.
 * @param onDrag Callback during drag with the current progress.
 * @param onDragStop Callback when the user stops dragging with the final progress.
 */
@Composable
private fun PlaybackSlider(
    progress: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
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
            tween(durationMillis = PositionPollingIntervalMs.toInt(), easing = LinearEasing)
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
                enabled = true,
                onDragStart = { onDragStart(it.toHorizontalProgress(widthPx, isRtl)) },
                onDrag = { onDrag(it.toHorizontalProgress(widthPx, isRtl)) },
                onDragStop = { onDragStop(it?.toHorizontalProgress(widthPx, isRtl) ?: currentProgress) },
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        // Track background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TrackHeight)
                .clip(CircleShape)
                .background(ChatTheme.colors.chatWaveformBar),
        )
        // Active track
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = animatedProgress)
                .height(TrackHeight)
                .clip(CircleShape)
                .background(ChatTheme.colors.chatWaveformBarPlaying),
        )
        // Thumb
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

@Suppress("MagicNumber")
private val PlaybackSpeeds = floatArrayOf(1f, 1.5f, 2f)
private const val PositionPollingIntervalMs = 100L
private val TrackHeight = 4.dp
private val ThumbSize = 12.dp

/**
 * Observable state holder for [VideoPlaybackControls].
 *
 * Observes a [Player] via listener for discrete events (play/pause, speed, duration)
 * and polls for continuous position updates while playing.
 *
 * @param player The [Player] instance to observe and control.
 */
@Stable
internal class VideoPlaybackControlsState(private val player: Player) {
    var isPlaying: Boolean by mutableStateOf(player.isPlaying)
        private set

    var currentPosition: Long by mutableLongStateOf(player.currentPosition)
        private set

    var duration: Long by mutableLongStateOf(player.duration.coerceAtLeast(0L))
        private set

    var speed: Float by mutableFloatStateOf(player.playbackParameters.speed)
        private set

    var isSeeking: Boolean by mutableStateOf(false)
        private set

    val progress: Float
        get() = when {
            duration > 0 -> (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
            else -> 0f
        }

    val listener: Player.Listener = object : Player.Listener {
        override fun onIsPlayingChanged(playing: Boolean) {
            isPlaying = playing
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            speed = playbackParameters.speed
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            duration = player.duration.coerceAtLeast(0L)
            currentPosition = player.currentPosition.coerceAtLeast(0L)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                duration = player.duration.coerceAtLeast(0L)
            }
            if (playbackState == Player.STATE_ENDED) {
                currentPosition = duration
            }
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(0L)
            }
            player.play()
        }
    }

    fun cycleSpeed() {
        val currentIndex = PlaybackSpeeds.indexOfFirst { it == speed }
        val nextIndex = (currentIndex + 1) % PlaybackSpeeds.size
        player.playbackParameters = player.playbackParameters.withSpeed(PlaybackSpeeds[nextIndex])
    }

    fun onDragStart() {
        isSeeking = true
    }

    fun onDrag(dragProgress: Float) {
        currentPosition = (dragProgress * duration).toLong()
    }

    fun onDragStop(dragProgress: Float) {
        currentPosition = (dragProgress * duration).toLong()
        player.seekTo(currentPosition)
        isSeeking = false
    }

    suspend fun pollPosition() {
        while (isPlaying) {
            if (!isSeeking) {
                currentPosition = player.currentPosition.coerceAtLeast(0L)
            }
            delay(PositionPollingIntervalMs)
        }
    }
}

@Composable
private fun rememberVideoPlaybackControlsState(player: Player): VideoPlaybackControlsState {
    val state = remember(player) { VideoPlaybackControlsState(player) }

    DisposableEffect(player) {
        player.addListener(state.listener)
        onDispose { player.removeListener(state.listener) }
    }

    LaunchedEffect(player, state.isPlaying) {
        state.pollPosition()
    }

    return state
}
