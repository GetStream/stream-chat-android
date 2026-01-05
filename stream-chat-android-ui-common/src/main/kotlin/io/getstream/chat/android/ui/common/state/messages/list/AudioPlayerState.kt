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

package io.getstream.chat.android.ui.common.state.messages.list

import androidx.collection.IntFloatMap
import androidx.collection.intFloatMapOf
import androidx.compose.runtime.Immutable
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Attachment

/**
 * Represents the state of the audio player.
 *
 * @property current The ongoing state of the audio player.
 * @property seekTo The seekTo state of the audio player.
 * @property getRecordingUri A function that returns the URI of the recording.
 */
@Immutable
@InternalStreamChatApi
public data class AudioPlayerState(
    val current: CurrentAudioState = CurrentAudioState(),
    val seekTo: IntFloatMap = intFloatMapOf(),
    val getRecordingUri: (Attachment) -> String?,
) {

    /**
     * Represents the ongoing state of the audio player.
     *
     * @property playingId The ID of the audio that is currently playing.
     * @property audioUri The URI of the audio that is currently playing.
     * @property waveform The waveform of the audio that is currently playing.
     * @property playbackInMs The current playback position in milliseconds.
     * @property durationInMs The duration of the audio that is currently playing.
     * @property isLoading If the audio is currently loading.
     * @property isPlaying If the audio is currently playing.
     * @property playingSpeed The speed of the audio playback.
     * @property playingProgress The progress of the audio playback.
     */
    public data class CurrentAudioState(
        val playingId: Int = -1,
        val playingSpeed: Float = 1.0f,
        val playingProgress: Float = 0f,
        val audioUri: String = "",
        val waveform: List<Float> = emptyList(),
        val playbackInMs: Int = 0,
        val durationInMs: Int = 0,
        val isLoading: Boolean = false,
        val isPlaying: Boolean = false,
        val isSeeking: Boolean = false,
    )

    public fun stringify(): String = with(current) {
        return "AudioPlayerState(" +
            "playingId=$playingId, " +
            "playingProgress=$playingProgress, " +
            "durationInMs=$durationInMs, " +
            "isPlaying=$isPlaying, " +
            "isSeeking=$isSeeking, " +
            "isLoading=$isLoading, " +
            "playingSpeed=$playingSpeed, " +
            "waveform.size=${waveform.size}, " +
            "playingUriHash=${audioUri.hashCode()}, " +
            "seekTo.size=${seekTo.size}" +
            ")"
    }
}
