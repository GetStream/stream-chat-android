/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.audio

@Suppress("TooManyFunctions")
/**
 * Audio player used to play audio messages.
 */
public interface AudioPlayer {

    /**
     * Subscribing for audio state changes for the audio of the hash
     *
     * @param hash the identifier of the audio track
     * @param func The listener of the [AudioState] change.
     */
    public fun onAudioStateChange(hash: Int, func: (AudioState) -> Unit)

    /**
     * Subscribing for progress changes for the audio of the hash. The progress is updated every 50ms
     *
     * @param hash the identifier of the audio track
     * @param func The listener of the [ProgressData] change.
     */
    public fun onProgressStateChange(hash: Int, func: (ProgressData) -> Unit)

    /**
     * Subscribing for speed changes for the audio of the hash.
     *
     * @param hash the identifier of the audio track
     * @param func The listener of the speed change.
     */
    public fun onSpeedChange(hash: Int, func: (Float) -> Unit)

    public fun registerTrack(url: String, hash: Int, position: Int)

    public fun clearTracks()

    public fun prepare(sourceUrl: String, audioHash: Int)

    /**
     * Plays an audio track with sourceUrl.
     *
     * @param sourceUrl the URL of the audio track
     * @param hash the identifier of the audio track
     */
    public fun play(sourceUrl: String, audioHash: Int)

    /**
     * Pauses the current song.
     */
    public fun pause()

    public fun resume(audioHash: Int)

    public fun resetAudio(audioHash: Int)

    /**
     * Seeks the audio track of the audio hash to the milli second position. If the hash is the same of the current
     * playing audio track, the current audio track pauses.
     *
     * @param msec the position in milli seconds.
     * @param hash the identifier of the audio track
     */
    public fun seekTo(msec: Int, hash: Int)

    /**
     * Informs the player that seek has started. This can be used to pause the current audio track when seek starts.
     */
    public fun startSeek(audioHash: Int)

    /**
     * Changes the speed of reproduction. Options are 1x, 1.5x and 2x
     */
    public fun changeSpeed()

    /**
     * Current speed of reproduction. Options are 1x, 1.5x and 2x
     */
    public fun currentSpeed(): Float

    /**
     * Removes the current audio form the reproduction queue and removes the listeners
     */
    public fun removeAudio(audioHash: Int)

    /**
     * Removes the current audios form the reproduction queue and removes the listeners
     */
    public fun removeAudios(audioHashList: List<Int>)

    /**
     * Disposes the MediaPlayer and remove all audios.
     */
    public fun dispose()
}

/**
 * Progress data of the audio track.
 */
public data class ProgressData(
    public val currentPosition: Int,
    public val progress: Float,
    public val duration: Int,
)

/**
 * State of the an audio track. When a song complete and another starts the current song goes to UNSET and the next one
 * go to LOADING and PLAYING.
 */
public enum class AudioState {
    UNSET, LOADING, IDLE, PAUSE, PLAYING;
}

/**
 * State of the AudioPlayer. This is the state of the whole player, not individual songs.
 */
public enum class PlayerState {
    UNSET, LOADING, IDLE, PAUSE, PLAYING;
}
