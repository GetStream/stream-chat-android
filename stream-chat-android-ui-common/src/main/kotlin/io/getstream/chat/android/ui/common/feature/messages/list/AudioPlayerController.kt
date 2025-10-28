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

import androidx.collection.IntFloatMap
import androidx.collection.MutableIntFloatMap
import io.getstream.chat.android.client.audio.AudioPlayer
import io.getstream.chat.android.client.audio.AudioState
import io.getstream.chat.android.client.audio.ProgressData
import io.getstream.chat.android.client.audio.audioHash
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
    private val getRecordingUri: (Attachment) -> String?,
) {

    private val logger by taggedLogger("Chat:PlayerController")

    public val state: MutableStateFlow<AudioPlayerState> = MutableStateFlow(
        AudioPlayerState(getRecordingUri = getRecordingUri),
    )

    public fun resetAudio(attachment: Attachment) {
        if (attachment.isAudioRecording().not()) {
            logger.v { "[resetAudio] rejected (not an audio recording): ${attachment.type}" }
            return
        }
        getRecordingUri(attachment) ?: run {
            logger.v { "[resetAudio] rejected (no recordingUri): $attachment" }
            return
        }
        val audioHash = attachment.audioHash
        val curState = state.value
        if (curState.current.playingId != audioHash) {
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
        getRecordingUri(attachment) ?: run {
            logger.v { "[togglePlayback] rejected (no recordingUri): $attachment" }
            return
        }
        val audioHash = attachment.audioHash
        val curState = state.value
        val currentPlayingId = audioPlayer.currentPlayingId
        val isCurrentTrack = curState.current.playingId == audioHash
        val isProgressRunning = curState.current.playingProgress.let { it > 0 && it < 1 }
        logger.d {
            "[togglePlayback] audioHash: $audioHash, currentPlayingId; $currentPlayingId, " +
                "isCurrentTrack: $isCurrentTrack, isProgressRunning: $isProgressRunning, state: ${curState.stringify()}"
        }
        when (isCurrentTrack && isProgressRunning) {
            true -> when (curState.current.isPlaying) {
                true -> pause()
                else -> resume()
            }
            else -> play(attachment)
        }
    }

    public fun changeSpeed(attachment: Attachment) {
        if (attachment.isAudioRecording().not()) {
            logger.v { "[changeSpeed] rejected (not an audio recording): ${attachment.type}" }
            return
        }
        getRecordingUri(attachment) ?: run {
            logger.v { "[changeSpeed] rejected (no recordingUri): $attachment" }
            return
        }
        val audioHash = attachment.audioHash
        val curState = state.value
        if (curState.current.playingId != audioHash) {
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
        getRecordingUri(attachment) ?: run {
            logger.v { "[startSeek] rejected (no recordingUri): $attachment" }
            return
        }
        val audioHash = attachment.audioHash
        val curState = state.value
        if (curState.current.playingId != audioHash) {
            logger.v { "[startSeek] rejected (not playing): $audioHash" }
            return
        }
        logger.i { "[startSeek] audioHash: ${curState.current.playingId}" }
        audioPlayer.startSeek(curState.current.playingId)

        val audioState = audioPlayer.currentState
        val newState = curState.copy(
            current = when (curState.current.playingId == audioHash) {
                true -> curState.current.copy(
                    isPlaying = audioState == AudioState.PLAYING,
                    isSeeking = true,
                )
                else -> curState.current
            },
        )
        setState(newState)
    }

    public fun seekTo(attachment: Attachment, progress: Float) {
        if (attachment.isAudioRecording().not()) {
            logger.v { "[seekTo] rejected (not an audio recording): ${attachment.type}" }
            return
        }
        getRecordingUri(attachment) ?: run {
            logger.v { "[seekTo] rejected (no recordingUri): $attachment" }
            return
        }
        val audioHash = attachment.audioHash
        val curState = state.value
        val isCurrentAudio = curState.current.playingId == audioHash
        val durationInSeconds = attachment.duration ?: NULL_DURATION
        val positionInMs = (progress * durationInSeconds * MILLIS_IN_SECOND).toInt()
        logger.i {
            "[seekTo] isCurrentAudio: $isCurrentAudio, positionInMs: $positionInMs, " +
                "audioHash: $audioHash, state: ${curState.stringify()}"
        }
        audioPlayer.seekTo(positionInMs, audioHash)

        val newState = curState.copy(
            current = when (isCurrentAudio) {
                true -> curState.current.copy(
                    isSeeking = false,
                    playingProgress = progress,
                    playbackInMs = positionInMs,
                )
                else -> curState.current
            },
            seekTo = curState.seekTo + (audioHash to progress),
        )
        setState(newState)
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
        val audioHash = attachment.audioHash
        val waveform = attachment.waveformData ?: emptyList()
        var playbackInMs = audioPlayer.getCurrentPositionInMs(audioHash)
        val durationInMs = ((attachment.duration ?: NULL_DURATION) * MILLIS_IN_SECOND).toInt()
        val seekTo = curState.seekTo.getOrDefault(audioHash, 0f)
        if (seekTo > 0) {
            playbackInMs = (seekTo * durationInMs).toInt()
            logger.v { "[play] seekTo: $playbackInMs" }
        }
        logger.d { "[play] audioHash: $audioHash, playbackInMs: $playbackInMs, state: ${curState.stringify()}" }

        // Set the initial state first cause the audio player may emit progress before the state is updated
        val initialState = curState.newCurrentState(audioHash, recordingUri, waveform, playbackInMs, durationInMs)
        setState(initialState)

        if (seekTo > 0) audioPlayer.seekTo(playbackInMs, audioHash)
        audioPlayer.registerOnAudioStateChange(audioHash, this::onAudioStateChanged)
        audioPlayer.registerOnProgressStateChange(audioHash, this::onAudioPlayingProgress)
        audioPlayer.registerOnSpeedChange(audioHash, this::onAudioPlayingSpeed)
        audioPlayer.play(recordingUri, audioHash)

        val audioState = audioPlayer.currentState
        val nowState = state.value
        val newState = nowState.copy(
            current = nowState.current.copy(
                isLoading = audioState == AudioState.LOADING,
                isPlaying = audioState == AudioState.PLAYING,
            ),
        )
        setState(newState)
    }

    /**
     * Pauses the current audio recording.
     */
    public fun pause() {
        val curState = state.value
        if (curState.current.playingId == NO_ID) {
            logger.v { "[pause] rejected (no playingId)" }
            return
        }
        if (curState.current.isPlaying.not()) {
            logger.d { "[pause] rejected (not playing)" }
            return
        }
        logger.d { "[pause] audioHash: ${curState.current.playingId}" }
        audioPlayer.pause()
    }

    /**
     * Resumes the current audio recording.
     */
    public fun resume() {
        val curState = state.value
        if (curState.current.playingId == NO_ID) {
            logger.v { "[resume] rejected (no playingId)" }
            return
        }
        if (curState.current.isPlaying) {
            logger.v { "[resume] rejected (already playing)" }
            return
        }
        val playerState = audioPlayer.currentState
        val isIdleOrPaused = playerState == AudioState.IDLE || playerState == AudioState.PAUSE
        if (isIdleOrPaused.not()) {
            logger.v { "[resume] rejected (not idle or paused): $playerState" }
            return
        }
        val audioHash = curState.current.playingId
        logger.d { "[resume] audioHash: $audioHash" }
        audioPlayer.resume(audioHash)
    }

    /**
     * Resets the current audio recording.
     */
    public fun reset() {
        val curState = state.value
        logger.d { "[reset] state.playingId: ${curState.current.playingId}" }
        audioPlayer.reset()
        setState(AudioPlayerState(getRecordingUri = getRecordingUri))
    }

    private fun onAudioStateChanged(playbackState: AudioState) {
        val curState = state.value
        if (curState.current.playingId == NO_ID) {
            logger.v { "[onAudioStateChanged] rejected (no playingId)" }
            return
        }
        logger.d { "[onAudioStateChanged] playbackState: $playbackState" }
        val newState = curState.copy(
            current = curState.current.copy(
                isLoading = playbackState == AudioState.LOADING,
                isPlaying = playbackState == AudioState.PLAYING,
            ),
        )
        setState(newState)
    }

    private fun onAudioPlayingProgress(progressState: ProgressData) {
        val curState = state.value
        if (curState.current.playingId == NO_ID) {
            logger.v { "[onAudioPlayingProgress] rejected (no playingId)" }
            return
        }
        val newState = curState.copy(
            current = curState.current.copy(
                isPlaying = true,
                playingProgress = progressState.progress,
                playbackInMs = progressState.currentPosition,
                durationInMs = progressState.duration,
            ),
            seekTo = curState.seekTo - curState.current.playingId,
        )
        setState(newState)
    }

    private fun onAudioPlayingSpeed(speed: Float) {
        val curState = state.value
        if (curState.current.playingId == NO_ID) {
            logger.v { "[onAudioPlayingSpeed] rejected (no playingId)" }
            return
        }
        logger.d { "[onAudioPlayingSpeed] speed: $speed, state: ${curState.stringify()}" }
        val newState = curState.copy(
            current = curState.current.copy(
                playingSpeed = speed,
            ),
        )
        setState(newState)
    }

    private fun setState(newState: AudioPlayerState) {
        state.value = newState
    }

    private fun AudioPlayerState.newCurrentState(
        audioHash: Int,
        recordingUri: String,
        waveform: List<Float>,
        playbackInMs: Int,
        durationInMs: Int,
    ): AudioPlayerState = copy(
        current = AudioPlayerState.CurrentAudioState(
            playingId = audioHash,
            audioUri = recordingUri,
            waveform = waveform,
            durationInMs = durationInMs,
            playbackInMs = playbackInMs,
            playingProgress = playbackInMs.toFloat() / durationInMs,
        ),
        seekTo = when (current.playingId != audioHash && current.playingId != NO_ID && current.playingProgress > 0) {
            true -> seekTo + (current.playingId to current.playingProgress)
            else -> seekTo
        },
    )

    private companion object {
        private const val NO_ID = -1
        private const val NULL_DURATION = 0f
        private const val MILLIS_IN_SECOND = 1000f
    }

    internal operator fun IntFloatMap.plus(that: Pair<Int, Float>): IntFloatMap {
        val newMap = MutableIntFloatMap(this.size + 1)
        this.forEach { key, value ->
            newMap[key] = value
        }
        val (key, value) = that
        newMap[key] = value
        return newMap
    }

    internal operator fun IntFloatMap.minus(key: Int): IntFloatMap {
        if (this.contains(key).not()) {
            return this
        }

        val newMap = MutableIntFloatMap(this.size)
        this.forEach { k, v ->
            if (k != key) {
                newMap[k] = v
            }
        }
        return newMap
    }
}
