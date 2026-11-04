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

package io.getstream.chat.android.ai.a2ui.compose

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.getstream.chat.android.ai.a2ui.R
import io.getstream.chat.android.ai.a2ui.compose.icons.A2UIIcons
import io.getstream.chat.android.ai.compose.util.internal.AIImageLoader
import io.getstream.chat.android.ai.compose.util.internal.rememberAIImage

/**
 * `Video`: the poster, or a placeholder, with a play icon; a tap plays the video in place, with
 * the platform's controls. Only web videos play.
 */
@Composable
internal fun A2UIVideo(scope: A2UIComponentScope) {
    val url = scope.string("url")?.takeIf(AIImageLoader::isLoadable)
    val posterUrl = scope.string("posterUrl")
    var playing by rememberSaveable(url) { mutableStateOf(false) }
    val playLabel = stringResource(R.string.stream_ai_a2ui_play_video)
    val modifier = scope.modifier
        .padding(LeafMargin)
        .fillMaxWidth()
        .aspectRatio(VideoAspectRatio)
        .clip(RoundedCornerShape(MediaCorner))
    if (playing && url != null) {
        AndroidView(
            factory = { context ->
                VideoView(context).apply {
                    setMediaController(MediaController(context).also { it.setAnchorView(this) })
                    setVideoURI(Uri.parse(url))
                    start()
                }
            },
            onRelease = { it.stopPlayback() },
            modifier = modifier,
        )
        return
    }
    MediaPlaceholder(
        modifier = modifier
            .clickable(enabled = url != null, role = Role.Button) { playing = true }
            .semantics { contentDescription = playLabel },
    ) {
        posterUrl?.let { Poster(it) }
        Icon(
            imageVector = A2UIIcons.vector("play"),
            contentDescription = null,
            modifier = Modifier.size(PlayIconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Poster(url: String) {
    rememberAIImage(url, PosterSizePx)?.let {
        Image(
            bitmap = it,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/** `AudioPlayer`: a play and pause button with the audio's description. Only web audio plays. */
@Composable
internal fun A2UIAudioPlayer(scope: A2UIComponentScope) {
    val url = scope.string("url")?.takeIf(AIImageLoader::isLoadable)
    val description = scope.string("description")
    var playing by remember(url) { mutableStateOf(false) }
    val player = remember(url) { AudioPlayback(url) { playing = false } }
    DisposableEffect(player) { onDispose { player.release() } }
    Row(
        modifier = scope.modifier.padding(LeafMargin).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledTonalIconButton(
            onClick = {
                playing = if (playing) {
                    player.pause()
                    false
                } else {
                    player.play()
                }
            },
            enabled = url != null,
        ) {
            Icon(
                imageVector = A2UIIcons.vector(if (playing) "pause" else "play"),
                contentDescription = stringResource(
                    if (playing) R.string.stream_ai_a2ui_pause else R.string.stream_ai_a2ui_play,
                ),
            )
        }
        description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = LeafMargin * 2),
            )
        }
    }
}

/** One audio's playback, prepared the first time it plays. */
private class AudioPlayback(private val url: String?, private val onFinished: () -> Unit) {
    private var player: MediaPlayer? = null
    private var prepared = false
    private var playWhenPrepared = false

    /** Starts or resumes playback, returning whether it is playing or about to. */
    fun play(): Boolean {
        val source = url ?: return false
        val existing = player
        if (existing != null) {
            if (prepared) existing.start() else playWhenPrepared = true
            return true
        }
        return try {
            player = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                setDataSource(source)
                setOnPreparedListener {
                    prepared = true
                    if (playWhenPrepared) it.start()
                }
                setOnCompletionListener { onFinished() }
                setOnErrorListener { _, _, _ ->
                    onFinished()
                    true
                }
                playWhenPrepared = true
                prepareAsync()
            }
            true
        } catch (_: java.io.IOException) {
            false
        } catch (_: IllegalStateException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    fun pause() {
        playWhenPrepared = false
        if (prepared) player?.pause()
    }

    fun release() {
        player?.release()
        player = null
        prepared = false
    }
}

private const val VideoAspectRatio = 16f / 9f
private const val PosterSizePx = 720
private val MediaCorner = 8.dp
private val PlayIconSize = 48.dp
