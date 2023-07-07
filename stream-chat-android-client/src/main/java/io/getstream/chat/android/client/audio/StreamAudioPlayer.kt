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
import androidx.core.net.toUri
import io.getstream.chat.android.client.scope.UserScope
import io.getstream.log.taggedLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

private const val INITIAL_SPEED = 1F
private const val SPEED_INCREMENT = 0.5F

@Suppress("TooManyFunctions")
internal class StreamMediaPlayer(
    private val mediaPlayer: MediaPlayer,
    private val userScope: UserScope,
    private val progressUpdatePeriod: Long = 50,
) : AudioPlayer {

    private val logger by taggedLogger("StreamMediaPlayer")

    private val onStateListeners: MutableMap<Int, MutableList<(AudioState) -> Unit>> = mutableMapOf()
    private val onProgressListeners: MutableMap<Int, MutableList<(ProgressData) -> Unit>> = mutableMapOf()
    private val onSpeedListeners: MutableMap<Int, MutableList<(Float) -> Unit>> = mutableMapOf()
    private val audioTracks: MutableList<TrackInfo> = mutableListOf()
    private val registeredTrackHashSet: MutableSet<Int> = mutableSetOf()
    private val seekMap: MutableMap<Int, Int> = mutableMapOf()
    private var playerState = PlayerState.UNSET
    private var pollJob: Job? = null
    private var currentAudioHash: Int = -1
    private var playingSpeed = 1F
    private var currentIndex = 0

    override fun onAudioStateChange(hash: Int, func: (AudioState) -> Unit) {
        onStateListeners[hash]?.add(func) ?: run {
            onStateListeners[hash] = mutableListOf(func)
        }
    }

    override fun onProgressStateChange(hash: Int, func: (ProgressData) -> Unit) {
        onProgressListeners[hash]?.add(func) ?: run {
            onProgressListeners[hash] = mutableListOf(func)
        }
    }

    override fun onSpeedChange(hash: Int, func: (Float) -> Unit) {
        onSpeedListeners[hash]?.add(func) ?: run {
            onSpeedListeners[hash] = mutableListOf(func)
        }
    }

    override fun registerTrack(url: String, hash: Int, position: Int) {
        if (!registeredTrackHashSet.contains(hash)) {
            registeredTrackHashSet.add(hash)
            audioTracks.add(TrackInfo(url, hash, position))
            audioTracks.sort()
        }
    }

    override fun clearTracks() {
        registeredTrackHashSet.clear()
        audioTracks.clear()
    }

    override fun prepare(sourceUrl: String, audioHash: Int) {
        logger.d { "[prepare] audioHash: $audioHash, sourceUrl: $sourceUrl" }
        if (audioHash != currentAudioHash) {
            publishAudioState(currentAudioHash, AudioState.UNSET)
            mediaPlayer.reset()
            setAudio(sourceUrl, audioHash, autoPlay = false)
            return
        }
    }

    override fun play(sourceUrl: String, audioHash: Int) {
        logger.d { "[play] audioHash: $audioHash, sourceUrl: $sourceUrl" }
        if (audioHash != currentAudioHash) {
            publishAudioState(currentAudioHash, AudioState.UNSET)
            mediaPlayer.reset()
            setAudio(sourceUrl, audioHash, autoPlay = true)
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
        stopPolling()
        onStateListeners.clear()
        onProgressListeners.clear()
        onSpeedListeners.clear()
        seekMap.clear()
        audioTracks.clear()
        mediaPlayer.release()
    }

    override fun removeAudio(audioHash: Int) {
        onStateListeners.remove(audioHash)
        onProgressListeners.remove(audioHash)
        onSpeedListeners.remove(audioHash)
        audioTracks.removeAll { trackInto -> trackInto.hash == audioHash }
        seekMap.remove(audioHash)
    }

    override fun removeAudios(audioHashList: List<Int>) {
        audioHashList.forEach { hash ->
            removeAudio(hash)
        }
    }

    override fun resetAudio(audioHash: Int) {
        if (audioHash == currentAudioHash) {
            mediaPlayer.reset()
            playerState = PlayerState.UNSET
            publishAudioState(audioHash, AudioState.UNSET)
        }
        removeAudio(audioHash)
    }

    private fun setAudio(sourceUrl: String, audioHash: Int, autoPlay: Boolean = true) {
        logger.i { "[setAudio] audioHash: $audioHash" }
        currentIndex = audioTracks.indexOfFirst { trackInfo -> trackInfo.hash == audioHash }
            .takeUnless { index -> index == -1 }
            ?: 0

        currentAudioHash = audioHash

        mediaPlayer.run {
            setOnPreparedListener {
                logger.w { "[setAudio] prepared; autoPlay: $autoPlay, audioHash: $audioHash" }
                playerState = PlayerState.IDLE
                if (autoPlay) this@StreamMediaPlayer.start()
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
        val currentPosition = mediaPlayer.currentPosition
        logger.d { "[start] currentPosition: $currentPosition, playerState: $playerState" }
        if (playerState == PlayerState.IDLE || playerState == PlayerState.PAUSE) {
            val seekTo = seekMap[currentAudioHash] ?: 0
            logger.v { "[start] seekTo: $seekTo" }
            mediaPlayer.seekTo(seekTo)
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
            mediaPlayer.seekTo(seekTo)
            pollProgress()
        }
    }

    override fun pause() {
        if (playerState == PlayerState.PLAYING) {
            mediaPlayer.pause()
            seekMap[currentAudioHash] = mediaPlayer.currentPosition
            playerState = PlayerState.PAUSE
            publishAudioState(currentAudioHash, AudioState.PAUSE)
            stopPolling()
        }
    }

    override fun resume(audioHash: Int) {
        val isIdleOrPaused = playerState == PlayerState.IDLE || playerState == PlayerState.PAUSE
        if (isIdleOrPaused && currentAudioHash == audioHash) {
            start()
        }
    }

    override fun seekTo(msec: Int, hash: Int) {
        seekMap[hash] = msec

        if (currentAudioHash == hash) {
            mediaPlayer.seekTo(msec)
            val currentPosition = mediaPlayer.currentPosition
            val duration = mediaPlayer.duration
            logger.w { "[seekTo] msec: $msec, currentPosition: $currentPosition, duration: $duration" }
        }
    }

    fun getCurrentProgress(audioHash: Int): Int {
        if (currentIndex == audioHash) {
            return mediaPlayer.currentPosition
        }
        return seekMap[audioHash] ?: 0
    }

    override fun startSeek(audioHash: Int) {
        if (playerState == PlayerState.PLAYING && currentAudioHash == audioHash) {
            pause()
        }
    }

    private fun onComplete() {
        stopPolling()
        publishProgress(currentAudioHash, ProgressData(0, 0f, mediaPlayer.duration))
        playerState = PlayerState.IDLE
        publishAudioState(currentAudioHash, AudioState.IDLE)
        seekMap[currentAudioHash] = 0

        if (currentIndex >= audioTracks.lastIndex) {
            playerState = PlayerState.IDLE
        } else {
            val trackInfo = audioTracks[currentIndex + 1]
            mediaPlayer.reset()
            setAudio(trackInfo.url, trackInfo.hash)
        }
    }

    private fun pollProgress() {
        val prevCurrentPosition = AtomicInteger(-1)
        val prevPosition = AtomicInteger(-1)
        logger.d { "[pollProgress] #1; currentPosition: ${mediaPlayer.currentPosition}" }
        pollJob = userScope.launch(Dispatchers.Default) {
            logger.d { "[pollProgress] #2; currentPosition: ${mediaPlayer.currentPosition}" }
            while (true) {
                val currentPosition = mediaPlayer.currentPosition
                val finalPosition = when (currentPosition > 0 && currentPosition == prevCurrentPosition.get()) {
                    true -> prevPosition.addAndGet(progressUpdatePeriod.toInt())
                    else -> prevPosition.set(currentPosition).let {
                        currentPosition
                    }
                }
                val durationMs = mediaPlayer.duration
                val progress = finalPosition.toFloat() / durationMs
                logger.v { "[pollProgress] #3; finalPosition: $finalPosition($currentPosition), prevPosition: $prevPosition" }
                withContext(Dispatchers.Main) {
                    publishProgress(
                        currentAudioHash,
                        ProgressData(
                            currentPosition = finalPosition,
                            progress = progress,
                            duration = durationMs,
                        )
                    )
                }
                prevCurrentPosition.set(currentPosition)
                delay(progressUpdatePeriod)
            }
        }
    }

    private fun stopPolling() {
        pollJob?.cancel()
    }

    private fun publishAudioState(audioHash: Int, audioState: AudioState) {
        logger.v { "[publishAudioState] audioHash: $audioHash, audioState: $audioState" }
        onStateListeners[audioHash]?.forEach { listener -> listener.invoke(audioState) }
    }

    private fun publishProgress(audioHash: Int, progressData: ProgressData) {
        onProgressListeners[audioHash]?.forEach { listener -> listener.invoke(progressData) }
    }

    private fun publishSpeed(audioHash: Int, speed: Float) {
        onSpeedListeners[audioHash]?.forEach { listener -> listener.invoke(speed) }
    }

    private fun normalize(uri: String): String {
        try {
            return uri.toUri().toString()
        } catch (_: Throwable) {
            return uri
        }
    }
}

internal class TrackInfo(val url: String, val hash: Int, private val positionInt: Int) : Comparable<TrackInfo> {

    override fun compareTo(other: TrackInfo): Int = this.positionInt.compareTo(other.positionInt)
}
