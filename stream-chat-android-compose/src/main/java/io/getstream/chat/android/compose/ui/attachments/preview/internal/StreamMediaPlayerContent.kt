/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.attachments.content.PlayButton
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.core.internal.StreamHandsOff

/**
 * A Composable that displays a video player with support for thumbnails, play controls, and buffering indicators.
 *
 * This component creates a Media3 ExoPlayer instance that handles video playback while respecting
 * the Android lifecycle. The player is automatically created when the composable enters the composition
 * and released when it leaves or pauses.
 *
 * The component offers several features:
 * - Shows an optional thumbnail with play button before playback starts
 * - Displays media controls that can be shown initially or hidden
 * - Shows a buffering indicator during loading states
 * - Automatically handles player lifecycle and resource cleanup
 *
 * @param assetUrl The URL of the video to play.
 * @param playWhenReady Whether playback should start automatically after preparation.
 * @param onPlaybackError Callback invoked when video playback encounters an error.
 * @param modifier Modifier to be applied to the player container.
 * @param thumbnailUrl Optional URL of the thumbnail image to display before playback starts.
 */
@OptIn(UnstableApi::class)
@Composable
internal fun StreamMediaPlayerContent(
    assetUrl: String?,
    playWhenReady: Boolean,
    onPlaybackError: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnailUrl: String? = null,
) {
    val context = LocalContext.current
    var showThumbnail by remember { mutableStateOf(!playWhenReady) }
    var showBuffering by remember { mutableStateOf(true) }
    val onBuffering: (Boolean) -> Unit = { isBuffering -> showBuffering = isBuffering }
    // Create player
    var player by remember { mutableStateOf<Player?>(null) }
    LifecycleResumeEffect(Unit) {
        player = createPlayer(
            context = context,
            onBuffering = onBuffering,
            onPlaybackError = onPlaybackError,
        )
        onPauseOrDispose {
            player?.release()
            player = null
        }
    }
    // Prepare media
    LaunchedEffect(player, assetUrl) {
        if (player != null && assetUrl != null) {
            player?.setMediaItem(MediaItem.fromUri(assetUrl))
            player?.prepare()
            player?.playWhenReady = playWhenReady
        }
    }
    // Draw player
    player?.let { preparedPlayer ->
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            // Video/Audio player
            AndroidView(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black),
                factory = {
                    val playerView = createPlayerView(it, preparedPlayer)
                    if (playWhenReady) playerView.showController()
                    playerView
                },
            )
            // Thumbnail
            if (showThumbnail) {
                MediaThumbnail(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable {
                            showThumbnail = false
                            preparedPlayer.play()
                        },
                    thumbnailUrl = thumbnailUrl,
                )
            }
            // Buffering indicator
            if (showBuffering) {
                LoadingIndicator()
            }
        }
    }
}

/**
 * A composable that displays a thumbnail image for a video with an optional play button overlay.
 *
 * This component shows a video thumbnail image with a centered play button that can be clicked
 * to start video playback. The thumbnail image is only displayed if video thumbnails are
 * enabled in the ChatTheme configuration.
 *
 * @param thumbnailUrl The URL of the thumbnail image to display, or null if no thumbnail is available.
 * @param modifier Modifier to be applied to the thumbnail container.
 */
@Composable
private fun MediaThumbnail(
    thumbnailUrl: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        if (thumbnailUrl != null) {
            StreamAsyncImage(
                modifier = modifier.matchParentSize(),
                data = thumbnailUrl,
                contentDescription = null,
            )
        }
        PlayButton(
            modifier = Modifier
                .shadow(6.dp, shape = CircleShape)
                .background(color = Color.White, shape = CircleShape)
                .size(
                    width = 42.dp,
                    height = 42.dp,
                ),
            contentDescription = stringResource(R.string.stream_compose_cd_play_button),
        )
    }
}

/**
 * Creates a basic [Player] instance for playing audio/video.
 * Creates a player of type [ExoPlayer].
 *
 * @param context The context to use for creating the player.
 * @param onBuffering Callback to be invoked when the player enters or exits buffering state.
 * @param onPlaybackError Callback to be invoked when a playback error occurs.
 */
private fun createPlayer(
    context: Context,
    onBuffering: (Boolean) -> Unit,
    onPlaybackError: () -> Unit,
): Player {
    // Setup player
    val player = ExoPlayer.Builder(context).build()
    player.addListener(object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_BUFFERING) {
                onBuffering(true)
            } else {
                onBuffering(false)
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            onPlaybackError()
        }
    })
    return player
}

/**
 * Creates a [PlayerView] for displaying video content.
 *
 * @param context The context to use for creating the view.
 * @param player The [Player] instance to be associated with the view.
 * @return A configured [PlayerView] instance.
 */
@StreamHandsOff(
    "This method manually inflates the PlayerView layout, because we are overriding the default " +
        "root layout (player_layout_id) and the default controller layout (controller_layout_id). These layouts are " +
        "just copies of the 'exo_player_view.xml' and the 'exo_player_control_view.xml' from the ExoPlayer " +
        "library. They only have a different name (layout ID) to avoid being overridden by layouts with the same " +
        "name from different versions of the ExoPlayer library (included in integration projects). This ensures that " +
        "we always use the correct layout for our version of the ExoPlayer library",
)
@OptIn(UnstableApi::class)
private fun createPlayerView(context: Context, player: Player): PlayerView {
    val playerView = LayoutInflater.from(context)
        .inflate(R.layout.stream_compose_player_view, null) as PlayerView
    return playerView.apply {
        this.player = player
        controllerShowTimeoutMs = ControllerShowTimeout
        controllerAutoShow = false
        controllerHideOnTouch = true
        setShowPreviousButton(false)
        setShowNextButton(false)
        setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
    }
}

private const val ControllerShowTimeout = 2000
