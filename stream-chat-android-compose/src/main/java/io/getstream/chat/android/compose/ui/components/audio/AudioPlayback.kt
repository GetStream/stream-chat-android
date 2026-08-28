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

import io.getstream.chat.android.client.audio.audioHash
import io.getstream.chat.android.client.extensions.durationInMs
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.state.messages.list.AudioPlayerState

/**
 * The state of one audio attachment within the shared player. A single player serves every attachment on screen, so
 * each one has to work out from [AudioPlayerState] whether it is the loaded track.
 *
 * @property hasSource Whether the player has a URI to play this attachment from.
 * @property playing Whether this attachment is the one currently being played.
 * @property isSeeking Whether the user is dragging the thumb of this attachment.
 * @property progress The playback progress to render, in the 0f..1f range.
 * @property durationInMs The track duration, or `null` while it is still unknown.
 */
internal data class AudioPlayback(
    val hasSource: Boolean,
    val playing: Boolean,
    val isSeeking: Boolean,
    val progress: Float,
    val durationInMs: Int?,
)

/**
 * Resolves the [AudioPlayback] of [attachment] against the shared player state.
 */
internal fun AudioPlayerState.playbackOf(attachment: Attachment): AudioPlayback {
    val attachmentUri = getRecordingUri(attachment)?.takeIf { it.isNotBlank() }
    val isCurrent = attachmentUri != null && attachmentUri == current.audioUri
    // Voice recordings carry their duration as an extra, while regular audio files only expose it once the player
    // has loaded the track.
    val durationInMs = attachment.durationInMs?.takeIf { it > 0 }
        ?: current.durationInMs.takeIf { isCurrent && current.durationInMs > 0 }
    return AudioPlayback(
        hasSource = attachmentUri != null,
        playing = isCurrent && current.isPlaying,
        isSeeking = isCurrent && current.isSeeking,
        // A stored seek is honoured on the next play, so showing it keeps the thumb where playback will resume.
        // The player divides by the duration it reports, which is zero for a track it cannot measure.
        progress = (
            current.playingProgress.takeIf { isCurrent }
                ?: attachmentUri?.let { seekTo.getOrDefault(attachment.audioHash, 0f) }
                ?: 0f
            ).takeIf { !it.isNaN() }?.coerceIn(0f, 1f) ?: 0f,
        durationInMs = durationInMs,
    )
}
