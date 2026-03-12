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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.models.Attachment

@Stable
internal class MediaGalleryPlayerState internal constructor() {
    var player: Player? by mutableStateOf(null)
        internal set

    /** Playback position captured before the last player release. */
    var savedPosition: Long = 0L
        internal set
}

/**
 * Creates and remembers a lifecycle-managed [Player].
 *
 * @param onPlaybackError Callback invoked when a playback error occurs.
 */
@Composable
internal fun rememberMediaGalleryPlayerState(
    onPlaybackError: (Throwable) -> Unit,
): MediaGalleryPlayerState {
    val context = LocalContext.current
    val previewMode = LocalInspectionMode.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = remember(::MediaGalleryPlayerState)

    DisposableEffect(lifecycleOwner, previewMode) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    if (!previewMode && state.player == null) {
                        state.player = createPlayer(
                            context = context,
                            onPlaybackError = onPlaybackError,
                            onBuffering = {},
                        )
                    }
                }

                Lifecycle.Event.ON_PAUSE -> state.player?.pause()

                Lifecycle.Event.ON_STOP -> {
                    state.savedPosition = state.player?.currentPosition ?: 0L
                    state.player?.release()
                    state.player = null
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            state.player?.release()
            state.player = null
        }
    }

    return state
}

/**
 * A side effect that prepares the correct media item whenever the gallery page changes
 * or the player becomes available.
 *
 * When the player is recreated on the same page (e.g. after ON_STOP → ON_START),
 * playback resumes from [MediaGalleryPlayerState.savedPosition].
 *
 * @param playerState The lifecycle-managed player state.
 * @param currentPage The current pager page index.
 * @param attachments The list of attachments displayed in the pager.
 */
@Composable
internal fun GalleryMediaEffect(
    playerState: MediaGalleryPlayerState,
    currentPage: Int,
    attachments: List<Attachment>,
) {
    var lastPreparedPage by remember { mutableIntStateOf(-1) }

    LaunchedEffect(currentPage, playerState.player) {
        playerState.player?.let { player ->
            player.pause()
            val attachment = attachments.getOrNull(currentPage) ?: return@LaunchedEffect
            if (attachment.isVideo()) {
                attachment.assetUrl?.let { assetUrl ->
                    val startPosition = if (currentPage == lastPreparedPage) {
                        playerState.savedPosition
                    } else {
                        0L
                    }
                    player.setMediaItem(MediaItem.fromUri(assetUrl), startPosition)
                    player.prepare()
                }
            }
            lastPreparedPage = currentPage
        }
    }
}
