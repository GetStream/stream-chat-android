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
import androidx.annotation.OptIn
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/**
 * Creates a basic [Player] instance for playing audio/video.
 * Creates a player of type [ExoPlayer].
 *
 * @param context The context to use for creating the player.
 * @param onBuffering Callback to be invoked when the player enters or exits buffering state.
 * @param onPlaybackError Callback to be invoked when a playback error occurs.
 */
internal fun createPlayer(
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
@OptIn(UnstableApi::class)
internal fun createPlayerView(context: Context, player: Player): PlayerView {
    return PlayerView(context).apply {
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
