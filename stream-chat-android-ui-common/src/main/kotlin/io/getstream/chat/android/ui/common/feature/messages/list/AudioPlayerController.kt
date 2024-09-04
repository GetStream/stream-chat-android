package io.getstream.chat.android.ui.common.feature.messages.list

import io.getstream.chat.android.client.audio.AudioPlayer
import io.getstream.chat.android.client.audio.AudioState
import io.getstream.chat.android.client.audio.PlayerState
import io.getstream.chat.android.client.audio.ProgressData
import io.getstream.chat.android.client.extensions.duration
import io.getstream.chat.android.client.extensions.waveformData
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.state.messages.list.AudioPlayerState
import io.getstream.log.taggedLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.log

@InternalStreamChatApi
public class AudioPlayerController(
    private val audioPlayer: AudioPlayer,
) {

    private val logger by taggedLogger("Chat:PlayerController")

    public val state: MutableStateFlow<AudioPlayerState?> = MutableStateFlow(null)

    /**
     * Plays or pauses the audio recording attachment.
     */
    public fun togglePlayback(attachment: Attachment) {
        if (attachment.isAudioRecording().not()) {
            logger.v { "[togglePlayback] rejected (not an audio recording): ${attachment.type}" }
            return
        }
        attachment.assetUrl ?: run {
            logger.v { "[togglePlayback] rejected (no assetUrl): $attachment" }
            return
        }
        val curState = state.value
        if (curState?.attachment == attachment) {
            if (curState.isPlaying) {
                logger.d { "[togglePlayback] pause" }
                pause()
            } else {
                logger.d { "[togglePlayback] resume" }
                resume()
            }
        } else {
            logger.d { "[togglePlayback] play" }
            play(attachment)
        }
    }

    public fun changeSpeed(attachment: Attachment) {
        if (attachment.isAudioRecording().not()) {
            logger.v { "[changeSpeed] rejected (not an audio recording): ${attachment.type}" }
            return
        }
        attachment.assetUrl ?: run {
            logger.v { "[changeSpeed] rejected (no assetUrl): $attachment" }
            return
        }
        val curState = state.value
        if (curState?.attachment != attachment) {
            logger.v { "[changeSpeed] rejected (not playing): $attachment" }
            return
        }
        audioPlayer.changeSpeed()
    }

    public fun startSeek(attachment: Attachment) {
        if (attachment.isAudioRecording().not()) {
            logger.v { "[startSeek] rejected (not an audio recording): ${attachment.type}" }
            return
        }
        attachment.assetUrl ?: run {
            logger.v { "[startSeek] rejected (no assetUrl): $attachment" }
            return
        }
        val curState = state.value
        if (curState?.attachment != attachment) {
            logger.v { "[startSeek] rejected (not playing): $attachment" }
            return
        }
        audioPlayer.startSeek(curState.playingId)
    }

    public fun seekTo(attachment: Attachment, progress: Float) {
        if (attachment.isAudioRecording().not()) {
            logger.v { "[seekTo] rejected (not an audio recording): ${attachment.type}" }
            return
        }
        attachment.assetUrl ?: run {
            logger.v { "[seekTo] rejected (no assetUrl): $attachment" }
            return
        }
        val curState = state.value
        if (curState?.attachment != attachment) {
            logger.v { "[seekTo] rejected (not playing): $attachment" }
            return
        }
        val durationInSeconds = attachment.duration ?: NULL_DURATION
        val positionInMs = (progress * durationInSeconds * MILLIS_IN_SECOND).toInt()
        logger.d { "[seekTo] positionInMs: $positionInMs, audioHash: ${curState.playingId}" }
        audioPlayer.seekTo(positionInMs, curState.playingId)
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
        val assetUrl = attachment.assetUrl ?: run {
            logger.v { "[play] rejected (no assetUrl): $attachment" }
            return
        }
        val curState = state.value
        if (curState != null) {
            audioPlayer.resetAudio(curState.playingId)
        }

        val audioHash = attachment.hashCode()
        audioPlayer.registerOnAudioStateChange(audioHash, this::onAudioStateChanged)
        audioPlayer.registerOnProgressStateChange(audioHash, this::onAudioPlayingProgress)
        audioPlayer.registerOnSpeedChange(audioHash, this::onAudioPlayingSpeed)
        audioPlayer.play(assetUrl, audioHash)

        val audioState = audioPlayer.currentState
        val durationInMs = ((attachment.duration ?: NULL_DURATION) * MILLIS_IN_SECOND).toInt()
        logger.d { "[play] audioHash: $audioHash" }
        setState(AudioPlayerState(
            attachment = attachment,
            waveform = attachment.waveformData ?: emptyList(),
            durationInMs = durationInMs,
            isLoading = audioState == AudioState.LOADING,
            isPlaying = audioState == AudioState.PLAYING,
            playingId = audioHash,
        ))
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
        if (!isIdleOrPaused) {
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
        setState(curState.copy(
            isLoading = playbackState == AudioState.LOADING,
            isPlaying = playbackState == AudioState.PLAYING,
        ))
    }

    private fun onAudioPlayingProgress(progressState: ProgressData) {
        logger.d { "[onAudioPlayingProgress] progressState: $progressState" }
        val curState = state.value ?: return
        setState(curState.copy(
            isPlaying = progressState.currentPosition > 0,
            playingProgress = progressState.progress,
            playbackInMs = progressState.currentPosition,
            durationInMs = progressState.duration,
        ))

    }

    private fun onAudioPlayingSpeed(speed: Float) {
        // logger.d { "[onAudioPlayingProgress] speed: $speed" }
        val curState = state.value ?: return
        setState(curState.copy(
            playingSpeed = speed,
        ))
    }

    private fun setState(newState: AudioPlayerState?) {
        logger.v { "[setState] ${state.value?.stringify()} => ${newState?.stringify()}" }
        state.value = newState
    }

    private companion object {
        private const val NULL_DURATION = 0f
        private const val MILLIS_IN_SECOND = 1000f
    }
}