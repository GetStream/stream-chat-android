package io.getstream.chat.android.ui.common.state.messages.list

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Attachment

/**
 * Represents the state of the audio player.
 *
 * @property attachment The attachment that is being played.
 * @property waveform The waveform of the audio.
 * @property playbackInMs The current playback position in milliseconds.
 * @property durationInMs The duration of the audio in milliseconds.
 * @property isLoading If the audio is currently loading.
 * @property isPlaying If the audio is currently playing.
 * @property playingSpeed The speed of the audio playback.
 * @property playingProgress The progress of the audio playback.
 * @property playingId The ID of the audio that is currently playing.
 */
@InternalStreamChatApi
public data class AudioPlayerState(
    val attachment: Attachment = Attachment(),
    val waveform: List<Float> = emptyList(),
    val playbackInMs: Int = 0,
    val durationInMs: Int = 0,
    val isLoading: Boolean = false,
    val isPlaying: Boolean = false,
    val playingSpeed: Float = 0.0f,
    val playingProgress: Float = 0f,
    val playingId: Int = -1,
) {

    public fun stringify(): String {
        return "AudioPlayerState(" +
            "playingId=$playingId, " +
            "playingProgress=$playingProgress, " +
            "durationInMs=$durationInMs, " +
            "isPlaying=$isPlaying, " +
            "isLoading=$isLoading, " +
            "playingSpeed=$playingSpeed, " +
            "waveform.size=${waveform.size}, " +
            "assertUrlHash=${attachment.assetUrl.hashCode()}" +
            ")"
    }

}
