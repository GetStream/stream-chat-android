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

import android.media.MediaPlayer
import android.os.Build
import io.getstream.chat.android.client.scope.UserScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val INITIAL_SPEED = 1F
private const val SPEED_INCREMENT = 0.5F

internal class StreamMediaPlayer(
    private val mediaPlayer: MediaPlayer,
    private val userScope: UserScope,
    private val progressUpdatePeriod: Long = 50,
) : AudioPlayer {

    private val onStateListeners: MutableMap<Int, (AudioState) -> Unit> = mutableMapOf()
    private val onProgressListeners: MutableMap<Int, (ProgressData) -> Unit> = mutableMapOf()
    private val onSpeedListeners: MutableMap<Int, (Float) -> Unit> = mutableMapOf()
    private val seekMap: MutableMap<Int, Int> = mutableMapOf()
    private var playerState = PlayerState.UNSET
    private var poolJob: Job? = null
    private var currentAudioHash: Int = -1
    private var playingSpeed = 1F

    override fun onAudioStateChange(hash: Int, func: (AudioState) -> Unit) {
        onStateListeners[hash] = func
    }

    override fun onProgressStateChange(hash: Int, func: (ProgressData) -> Unit) {
        onProgressListeners[hash] = func
    }

    override fun onSpeedChange(hash: Int, func: (Float) -> Unit) {
        onSpeedListeners[hash] = func
    }

    override fun play(sourceUrl: String, audioHash: Int) {
        if (audioHash != currentAudioHash) {
            publishAudioState(currentAudioHash, AudioState.UNSET)
            mediaPlayer.reset()
            setAudio(sourceUrl, audioHash)
            return
        }

        when (playerState) {
            PlayerState.UNSET -> setAudio(sourceUrl, audioHash)
            PlayerState.LOADING -> {}
            PlayerState.IDLE, PlayerState.PAUSE -> start()
            PlayerState.PLAYING -> pause()
        }
    }

    override fun changeSpeed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val currentSpeed = playingSpeed
            val newSpeed = if (currentSpeed >= 2 || currentSpeed < 1) {
                INITIAL_SPEED
            } else {
                currentSpeed + SPEED_INCREMENT
            }

            playingSpeed = newSpeed

            if (playerState == PlayerState.PLAYING) {
                mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(newSpeed)
            }

            publishSpeed(currentAudioHash, newSpeed)
        }
    }

    override fun currentSpeed(): Float =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) mediaPlayer.playbackParams.speed else 1F

    override fun dispose() {
        stopPooling()
        onStateListeners.clear()
        onProgressListeners.clear()
        mediaPlayer.release()
    }

    override fun removeAudios(audioHashList: List<Int>) {
        audioHashList.forEach { hash ->
            onStateListeners.remove(hash)
            onProgressListeners.remove(hash)
            onSpeedListeners.remove(hash)
            seekMap.remove(hash)
        }
    }

    private fun setAudio(sourceUrl: String, audioHash: Int) {
        currentAudioHash = audioHash

        mediaPlayer.run {
            setOnPreparedListener {
                playerState = PlayerState.IDLE
                this@StreamMediaPlayer.start()
            }

            setOnCompletionListener {
                onComplete()
            }

            playerState = PlayerState.LOADING
            publishAudioState(currentAudioHash, AudioState.LOADING)
            setDataSource(sourceUrl)
            prepareAsync()
        }
    }

    private fun start() {
        if (playerState == PlayerState.IDLE || playerState == PlayerState.PAUSE) {
            mediaPlayer.seekTo(seekMap[currentAudioHash] ?: 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(playingSpeed)
            }
            mediaPlayer.start()
            playerState = PlayerState.PLAYING
            publishAudioState(currentAudioHash, AudioState.PLAYING)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(playingSpeed)
                publishSpeed(currentAudioHash, playingSpeed)
            }
            poolProgress()
        }
    }

    override fun pause() {
        if (playerState == PlayerState.PLAYING) {
            mediaPlayer.pause()
            seekMap[currentAudioHash] = mediaPlayer.currentPosition
            publishAudioState(currentAudioHash, AudioState.PAUSE)
            playerState = PlayerState.PAUSE
            stopPooling()
        }
    }

    override fun seekTo(msec: Int, audioHash: Int) {
        seekMap[audioHash] = msec

        if (currentAudioHash == audioHash) {
            mediaPlayer.seekTo(msec)
        }
    }

    override fun startSeek(audioHash: Int) {
        if (playerState == PlayerState.PLAYING && currentAudioHash == audioHash) {
            pause()
        }
    }

    private fun onComplete() {
        stopPooling()
        seekMap[currentAudioHash] = 0
        playerState = PlayerState.IDLE
        publishProgress(currentAudioHash, ProgressData(0, 0.0))
        publishAudioState(currentAudioHash, AudioState.IDLE)
    }

    private fun poolProgress() {
        poolJob = userScope.launch(Dispatchers.Default) {
            while (true) {
                delay(progressUpdatePeriod)

                val progress = mediaPlayer.currentPosition.toDouble() / mediaPlayer.duration

                withContext(Dispatchers.Main) {
                    publishProgress(currentAudioHash, ProgressData(mediaPlayer.currentPosition, progress))
                }
            }
        }
    }

    private fun stopPooling() {
        poolJob?.cancel()
    }

    private fun publishAudioState(audioHash: Int, audioState: AudioState) {
        onStateListeners[audioHash]?.invoke(audioState)
    }

    private fun publishProgress(audioHash: Int, progressData: ProgressData) {
        onProgressListeners[audioHash]?.invoke(progressData)
    }

    private fun publishSpeed(audioHash: Int, speed: Float) {
        onSpeedListeners[audioHash]?.invoke(speed)
    }
}
