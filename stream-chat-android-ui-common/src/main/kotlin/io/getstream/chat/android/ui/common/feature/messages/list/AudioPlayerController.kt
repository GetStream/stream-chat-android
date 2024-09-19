/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.common.feature.messages.list

import io.getstream.chat.android.client.audio.AudioPlayer
import io.getstream.chat.android.client.audio.AudioState
import io.getstream.chat.android.client.audio.ProgressData
import io.getstream.chat.android.client.extensions.duration
import io.getstream.chat.android.client.extensions.waveformData
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.state.messages.list.AudioPlayerState
import io.getstream.log.taggedLogger
import kotlinx.coroutines.flow.MutableStateFlow

@InternalStreamChatApi
public class AudioPlayerController(
    private val audioPlayer: AudioPlayer,
    private val hasRecordingUri: (Attachment) -> Boolean,
    private val getRecordingUri: (Attachment) -> String?,
) {

    private val logger by taggedLogger("Chat:PlayerController")

    public val state: MutableStateFlow<AudioPlayerState?> = MutableStateFlow(null)

    public fun resetAudio(attachment: Attachment) {
        if (attachment.isAudioRecording().not()) {
            logger.v { "[resetAudio] rejected (not an audio recording): ${attachment.type}" }
            return
        }
        val audioHash = getRecordingUri(attachment)?.hashCode() ?: run {
            logger.v { "[resetAudio] rejected (no recordingUri): $attachment" }
            return
        }
        val curState = state.value
        if (curState?.playingId != audioHash) {
            logger.v { "[resetAudio] rejected (not playing): $audioHash" }
            return
        }
        audioPlayer.resetAudio(audioHash)
    }

    /**
     * Plays or pauses the audio recording attachment.
     */
    public fun togglePlayback(attachment: Attachment) {
        if (attachment.isAudioRecording().not()) {
            logger.v { "[togglePlayback] rejected (not an audio recording): ${attachment.type}" }
            return
        }
        if (!hasRecordingUri(attachment)) {
            logger.v { "[togglePlayback] rejected (no recordingUri): $attachment" }
            return
        }
        val curState = state.value
        logger.d { "[togglePlayback] state: ${curState?.stringify()}" }
        if (curState?.attachment == attachment) {
            if (curState.isPlaying) {
                logger.v { "[togglePlayback] pause" }
                pause()
            } else {
                logger.v { "[togglePlayback] resume" }
                resume()
            }
        } else {
            logger.v { "[togglePlayback] play" }
            play(attachment)
        }
    }

    public fun changeSpeed(attachment: Attachment) {
        if (attachment.isAudioRecording().not()) {
            logger.v { "[changeSpeed] rejected (not an audio recording): ${attachment.type}" }
            return
        }
        val audioHash = getRecordingUri(attachment)?.hashCode() ?: run {
            logger.v { "[changeSpeed] rejected (no recordingUri): $attachment" }
            return
        }
        val curState = state.value
        if (curState?.playingId != audioHash) {
            logger.v { "[startSeek] rejected (not playing): $audioHash" }
            return
        }
        audioPlayer.changeSpeed()
    }

    public fun startSeek(attachment: Attachment) {
        if (attachment.isAudioRecording().not()) {
            logger.v { "[startSeek] rejected (not an audio recording): ${attachment.type}" }
            return
        }
        val audioHash = getRecordingUri(attachment)?.hashCode() ?: run {
            logger.v { "[startSeek] rejected (no recordingUri): $attachment" }
            return
        }
        val curState = state.value
        if (curState?.playingId != audioHash) {
            logger.v { "[startSeek] rejected (not playing): $audioHash" }
            return
        }
        logger.i { "[startSeek] audioHash: ${curState.playingId}" }
        audioPlayer.startSeek(curState.playingId)
    }

    public fun seekTo(attachment: Attachment, progress: Float) {
        if (attachment.isAudioRecording().not()) {
            logger.v { "[seekTo] rejected (not an audio recording): ${attachment.type}" }
            return
        }
        val recordingUri = getRecordingUri(attachment) ?: run {
            logger.v { "[seekTo] rejected (no recordingUri): $attachment" }
            return
        }
        val audioHash = recordingUri.hashCode()
        val durationInSeconds = attachment.duration ?: NULL_DURATION
        val positionInMs = (progress * durationInSeconds * MILLIS_IN_SECOND).toInt()
        logger.i { "[seekTo] positionInMs: $positionInMs, audioHash: $audioHash, uri: $recordingUri" }
        audioPlayer.seekTo(positionInMs, audioHash)
    }

    /**
     * Plays the audio recording attachment.
     *
     * @param attachment The attachment to play.
     */
    public fun play(attachment: Attachment) {
        if (attachment.isAudioRecording().not()) {
            logger.v { "[play] rejected (not an audio recording): ${attachment.type}" }
            return
        }
        val recordingUri = getRecordingUri(attachment) ?: run {
            logger.v { "[play] rejected (no recordingUri): $attachment" }
            return
        }
        val curState = state.value
        if (curState != null) {
            audioPlayer.resetAudio(curState.playingId)
        }

        val audioHash = recordingUri.hashCode()
        audioPlayer.registerOnAudioStateChange(audioHash, this::onAudioStateChanged)
        audioPlayer.registerOnProgressStateChange(audioHash, this::onAudioPlayingProgress)
        audioPlayer.registerOnSpeedChange(audioHash, this::onAudioPlayingSpeed)
        audioPlayer.play(recordingUri, audioHash)

        val audioState = audioPlayer.currentState
        val durationInMs = ((attachment.duration ?: NULL_DURATION) * MILLIS_IN_SECOND).toInt()
        logger.d { "[play] audioHash: $audioHash, uri: $recordingUri" }
        setState(
            AudioPlayerState(
                attachment = attachment,
                waveform = attachment.waveformData ?: emptyList(),
                durationInMs = durationInMs,
                isLoading = audioState == AudioState.LOADING,
                isPlaying = audioState == AudioState.PLAYING,
                playingId = audioHash,
            ),
        )
    }

    /**
     * Pauses the current audio recording.
     */
    public fun pause() {
        val curState = state.value ?: run {
            logger.d { "[pause] rejected (no state)" }
            return
        }
        if (curState.isPlaying.not()) {
            logger.d { "[pause] rejected (not playing)" }
            return
        }
        logger.d { "[pause] audioHash: ${curState.playingId}" }
        audioPlayer.pause()
    }

    /**
     * Resumes the current audio recording.
     */
    public fun resume() {
        val curState = state.value ?: run {
            logger.v { "[resume] rejected (no state)" }
            return
        }
        if (curState.isPlaying) {
            logger.v { "[resume] rejected (already playing)" }
            return
        }
        val playerState = audioPlayer.currentState
        val isIdleOrPaused = playerState == AudioState.IDLE || playerState == AudioState.PAUSE
        if (isIdleOrPaused.not()) {
            logger.v { "[resume] rejected (not idle or paused): $playerState" }
            return
        }
        val audioHash = curState.playingId
        logger.d { "[resume] audioHash: $audioHash" }
        audioPlayer.resume(audioHash)
    }

    /**
     * Resets the current audio recording.
     */
    public fun reset() {
        audioPlayer.clearTracks()
        state.value?.playingId?.also { audioPlayer.resetAudio(it) }
        setState(null)
    }

    private fun onAudioStateChanged(playbackState: AudioState) {
        val curState = state.value ?: return
        setState(
            curState.copy(
                isLoading = playbackState == AudioState.LOADING,
                isPlaying = playbackState == AudioState.PLAYING,
            ),
        )
    }

    private fun onAudioPlayingProgress(progressState: ProgressData) {
        val curState = state.value ?: return
        setState(
            curState.copy(
                isPlaying = progressState.currentPosition > 0,
                playingProgress = progressState.progress,
                playbackInMs = progressState.currentPosition,
                durationInMs = progressState.duration,
            ),
        )
    }

    private fun onAudioPlayingSpeed(speed: Float) {
        val curState = state.value ?: return
        setState(
            curState.copy(
                playingSpeed = speed,
            ),
        )
    }

    private fun setState(newState: AudioPlayerState?) {
        // logger.v { "[setState] ${state.value?.stringify()} => ${newState?.stringify()}" }
        state.value = newState
    }

    private companion object {
        private const val NULL_DURATION = 0f
        private const val MILLIS_IN_SECOND = 1000f
    }
}
