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

package io.getstream.chat.android.client.audio

import io.getstream.chat.android.client.scope.UserScope
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.log.taggedLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

@Suppress("TooManyFunctions")
internal class StreamAudioPlayer(
    private val mediaPlayer: NativeMediaPlayer,
    private val userScope: UserScope,
    private val progressUpdatePeriod: Long = 50,
) : AudioPlayer {

    companion object {
        private const val DEBUG_POLLING = false
        private const val INITIAL_SPEED = 1F
        private const val SPEED_INCREMENT = 0.5F
    }

    private val logger by taggedLogger("Chat:StreamAudioPlayer")

    private val onStateListeners: MutableMap<Int, MutableList<(AudioState) -> Unit>> = mutableMapOf()
    private val onProgressListeners: MutableMap<Int, MutableList<(ProgressData) -> Unit>> = mutableMapOf()
    private val onSpeedListeners: MutableMap<Int, MutableList<(Float) -> Unit>> = mutableMapOf()
    private val audioTracks: MutableList<TrackInfo> = mutableListOf()
    private val registeredTrackHashSet: MutableSet<Int> = mutableSetOf()
    private val seekMap: MutableMap<Int, Int> = mutableMapOf()
    private val speedMap: MutableMap<Int, Float> = mutableMapOf()
    private var playerState = PlayerState.UNSET
        set(value) {
            logger.v { "[setPlayerState] value: $value" }
            field = value
        }
    private var pollJob: Job? = null
    private var currentAudioHash: Int = -1
    private var playingSpeed = 1F
    private var currentIndex = 0

    override val currentPlayingId: Int get() = currentAudioHash

    override val currentState: AudioState
        get() = when (playerState) {
            PlayerState.UNSET -> AudioState.UNSET
            PlayerState.LOADING -> AudioState.LOADING
            PlayerState.IDLE -> AudioState.IDLE
            PlayerState.PAUSE -> AudioState.PAUSE
            PlayerState.PLAYING -> AudioState.PLAYING
        }

    override fun registerOnAudioStateChange(audioHash: Int, onAudioStateChange: (AudioState) -> Unit) {
        logger.v { "[registerOnAudioStateChange] audioHash: $audioHash, size: ${onStateListeners.size}" }
        onStateListeners[audioHash]?.add(onAudioStateChange) ?: run {
            onStateListeners[audioHash] = mutableListOf(onAudioStateChange)
        }
    }

    override fun registerOnProgressStateChange(audioHash: Int, onProgressDataChange: (ProgressData) -> Unit) {
        logger.v { "[registerOnProgressStateChange] audioHash: $audioHash, size: ${onProgressListeners.size}" }
        onProgressListeners[audioHash]?.add(onProgressDataChange) ?: run {
            onProgressListeners[audioHash] = mutableListOf(onProgressDataChange)
        }
    }

    override fun registerOnSpeedChange(audioHash: Int, onSpeedChange: (Float) -> Unit) {
        logger.v { "[registerOnSpeedChange] audioHash: $audioHash, size: ${onSpeedListeners.size}" }
        onSpeedListeners[audioHash]?.add(onSpeedChange) ?: run {
            onSpeedListeners[audioHash] = mutableListOf(onSpeedChange)
        }
    }

    override fun registerTrack(sourceUrl: String, audioHash: Int, position: Int) {
        logger.i {
            "[registerTrack] audioHash: $audioHash, position: $position" +
                ", sourceUrl.hash: ${sourceUrl.hashCode()}"
        }
        if (!registeredTrackHashSet.contains(audioHash)) {
            registeredTrackHashSet.add(audioHash)
            audioTracks.add(TrackInfo(sourceUrl, audioHash, position))
            audioTracks.sort()
        }
    }

    override fun clearTracks() {
        logger.i { "[clearTracks] no args" }
        registeredTrackHashSet.clear()
        audioTracks.clear()
    }

    override fun prepare(sourceUrl: String, audioHash: Int) {
        logger.d { "[prepare] audioHash: $audioHash, sourceUrl.hash: ${sourceUrl.hashCode()}" }
        if (audioHash != currentAudioHash) {
            resetPlayer()
            setAudio(sourceUrl, audioHash, autoPlay = false)
            return
        }
    }

    override fun play(sourceUrl: String, audioHash: Int) {
        logger.i { "[play] audioHash($currentAudioHash): $audioHash, playerState: $playerState" }
        if (audioHash != currentAudioHash) {
            resetPlayer()
            setAudio(sourceUrl, audioHash, autoPlay = true)
            return
        }
        logger.v { "[play] currentAudioHash: $currentAudioHash, playerState: $playerState" }
        when (playerState) {
            PlayerState.UNSET -> setAudio(sourceUrl, audioHash)
            PlayerState.LOADING -> {}
            PlayerState.IDLE, PlayerState.PAUSE -> start()
            PlayerState.PLAYING -> pause()
        }
    }

    override fun changeSpeed(audioHash: Int): Float {
        logger.i { "[changeSpeed] audioHash: $audioHash" }
        val currentSpeed = speedMap[audioHash] ?: INITIAL_SPEED
        val newSpeed = if (currentSpeed >= 2 || currentSpeed < 1) {
            INITIAL_SPEED
        } else {
            currentSpeed + SPEED_INCREMENT
        }

        speedMap[audioHash] = newSpeed

        // If this is the currently playing audio, apply the speed immediately
        if (currentAudioHash == audioHash) {
            playingSpeed = newSpeed
            if (playerState == PlayerState.PLAYING && mediaPlayer.isSpeedSettable()) {
                mediaPlayer.speed = newSpeed
            }
        }

        publishSpeed(audioHash, newSpeed)
        return newSpeed
    }

    override fun currentSpeed(): Float = mediaPlayer.speed

    override fun dispose() {
        userScope.launch(DispatcherProvider.Main) {
            logger.i { "[dispose] playerState: $playerState" }
            stopPolling()
            onStateListeners.clear()
            onProgressListeners.clear()
            onSpeedListeners.clear()
            seekMap.clear()
            speedMap.clear()
            audioTracks.clear()
            mediaPlayer.release()
        }
    }

    override fun removeAudio(audioHash: Int) {
        logger.i { "[removeAudio] audioHash: $audioHash" }
        onStateListeners.remove(audioHash)
        onProgressListeners.remove(audioHash)
        onSpeedListeners.remove(audioHash)
        audioTracks.removeAll { trackInto -> trackInto.hash == audioHash }
        seekMap.remove(audioHash)
        speedMap.remove(audioHash)
    }

    override fun removeAudios(audioHashList: List<Int>) {
        logger.i { "[removeAudios] audioHashList.size: ${audioHashList.size}" }
        audioHashList.forEach { audioHash ->
            logger.v { "[removeAudios] audioHash: $audioHash" }
            removeAudio(audioHash)
        }
    }

    override fun resetAudio(audioHash: Int) {
        logger.i { "[resetAudio] playerState: $playerState, audioHash: $audioHash" }
        if (audioHash == currentAudioHash) {
            resetPlayer()
        }
        removeAudio(audioHash)
    }

    override fun reset() {
        logger.i { "[reset] playerState: $playerState, currentAudioHash: $currentAudioHash" }
        resetPlayer()
        onStateListeners.clear()
        onProgressListeners.clear()
        onSpeedListeners.clear()
        audioTracks.clear()
        seekMap.clear()
        speedMap.clear()
    }

    private fun resetPlayer() {
        logger.v { "[resetPlayer] playerState: $playerState, audioHash: $currentAudioHash" }
        stopPolling()
        mediaPlayer.reset()
        playerState = PlayerState.UNSET
        if (currentAudioHash != -1) {
            publishAudioState(currentAudioHash, AudioState.UNSET)
            seekMap.remove(currentAudioHash)
            currentAudioHash = -1
        }
    }

    private fun setAudio(sourceUrl: String, audioHash: Int, autoPlay: Boolean = true) {
        logger.d { "[setAudio] audioHash: $audioHash, autoPlay: $autoPlay, sourceUrl.hash: ${sourceUrl.hashCode()}" }
        currentIndex = audioTracks.indexOfFirst { trackInfo -> trackInfo.hash == audioHash }
            .takeUnless { index -> index == -1 }
            ?: 0

        currentAudioHash = audioHash

        mediaPlayer.run {
            setOnPreparedListener {
                onPrepared(audioHash, autoPlay)
            }

            setOnCompletionListener {
                onComplete(audioHash)
            }

            setOnErrorListener { errorCode ->
                onError(audioHash, errorCode)
            }

            playerState = PlayerState.LOADING
            publishAudioState(currentAudioHash, AudioState.LOADING)
            setDataSource(sourceUrl)
            prepareAsync()
        }
    }

    private fun start() {
        val currentPosition = mediaPlayer.currentPosition
        val currentAudioHash = currentAudioHash
        logger.d {
            "[start] currentAudioHash: $currentAudioHash" +
                ", currentPosition: $currentPosition, playerState: $playerState"
        }
        if (playerState == PlayerState.IDLE || playerState == PlayerState.PAUSE) {
            val seekTo = seekMap[currentAudioHash] ?: 0
            val duration = mediaPlayer.duration
            logger.v { "[start] seekTo: $seekTo, duration: $duration" }
            if (seekTo >= duration) {
                publishProgress(currentAudioHash, ProgressData(duration, 1f, duration))
                postOnComplete(currentAudioHash)
                return
            }
            mediaPlayer.seekTo(seekTo)
            if (mediaPlayer.isSpeedSettable()) {
                val speed = speedMap[currentAudioHash] ?: INITIAL_SPEED
                playingSpeed = speed
                mediaPlayer.speed = speed
                publishSpeed(currentAudioHash, speed)
            }
            mediaPlayer.start()
            playerState = PlayerState.PLAYING
            publishAudioState(currentAudioHash, AudioState.PLAYING)
            pollProgress()
        }
    }

    override fun pause() {
        logger.d { "[pause] playerState: $playerState, currentAudioHash: $currentAudioHash" }
        if (playerState == PlayerState.PLAYING) {
            mediaPlayer.pause()
            seekMap[currentAudioHash] = mediaPlayer.currentPosition
            playerState = PlayerState.PAUSE
            publishAudioState(currentAudioHash, AudioState.PAUSE)
            stopPolling()
        }
    }

    override fun resume(audioHash: Int) {
        logger.d { "[resume] audioHash: $audioHash, playerState: $playerState" }
        val isIdleOrPaused = playerState == PlayerState.IDLE || playerState == PlayerState.PAUSE
        if (isIdleOrPaused && currentAudioHash == audioHash) {
            start()
        }
    }

    override fun seekTo(positionInMs: Int, audioHash: Int) {
        logger.d { "[seekTo] audioHash: $audioHash, positionInMs: $positionInMs, playerState: $playerState" }
        seekMap[audioHash] = positionInMs

        if (currentAudioHash == audioHash && mediaPlayer.isSeekable()) {
            mediaPlayer.seekTo(positionInMs)
            val currentPosition = mediaPlayer.currentPosition
            val duration = mediaPlayer.duration
            logger.v { "[seekTo] msec: $positionInMs, currentPosition: $currentPosition, duration: $duration" }
        }
    }

    override fun getCurrentPositionInMs(audioHash: Int): Int {
        if (currentIndex == audioHash) {
            return mediaPlayer.currentPosition
        }
        return seekMap[audioHash] ?: 0
    }

    override fun startSeek(audioHash: Int) {
        logger.d { "[startSeek] audioHash: $audioHash, playerState: $playerState" }
        if (playerState == PlayerState.PLAYING && currentAudioHash == audioHash) {
            pause()
        }
    }

    private fun onPrepared(audioHash: Int, autoPlay: Boolean) {
        logger.i { "[onPrepared] audioHash: $audioHash, autoPlay: $autoPlay" }
        playerState = PlayerState.IDLE
        publishAudioState(audioHash, AudioState.IDLE)
        if (autoPlay) {
            start()
        }
    }

    private fun onError(audioHash: Int, errorCode: Int): Boolean {
        logger.e { "[onError] audioHash: $audioHash, errorCode: $errorCode" }
        complete(audioHash)
        resetPlayer()
        mediaPlayer.release()
        return true
    }

    private fun postOnComplete(audioHash: Int) {
        logger.v { "[postOnComplete] audioHash: $audioHash" }
        userScope.launch(DispatcherProvider.Main) {
            complete(audioHash)
        }
    }

    private fun onComplete(audioHash: Int) {
        logger.i { "[onComplete] audioHash: $audioHash" }
        complete(audioHash)
    }

    private fun complete(audioHash: Int) {
        logger.d { "[complete] audioHash: $audioHash" }
        publishProgress(audioHash, ProgressData(0, 0f, mediaPlayer.duration))
        stopPolling()

        playerState = PlayerState.IDLE
        publishAudioState(audioHash, AudioState.IDLE)
        seekMap[audioHash] = 0

        logger.v { "[complete] currentIndex: $currentIndex, lastIndex: ${audioTracks.lastIndex}" }
        if (currentIndex < audioTracks.lastIndex) {
            val trackInfo = audioTracks[currentIndex + 1]
            resetPlayer()
            setAudio(trackInfo.url, trackInfo.hash)
        }
    }

    private fun pollProgress() {
        val audioHash: Int = currentAudioHash
        val prevCurPosition = AtomicInteger(-1)
        val prevPosition = AtomicInteger(-1)
        logger.d {
            "[pollProgress] #1; audioHash: $audioHash, currentPosition: ${mediaPlayer.currentPosition}, " +
                "duration: ${mediaPlayer.duration}"
        }
        pollJob = userScope.launch(DispatcherProvider.Main) {
            while (isActive) {
                val curPosition = mediaPlayer.currentPosition
                val finalPosition = when (curPosition > 0 && curPosition == prevCurPosition.get()) {
                    true -> prevPosition.addAndGet(progressUpdatePeriod.toInt())
                    else -> prevPosition.set(curPosition).let {
                        curPosition
                    }
                }
                val durationMs = mediaPlayer.duration
                val progress = finalPosition.toFloat() / durationMs
                if (DEBUG_POLLING) {
                    logger.v {
                        "[pollProgress] #2; finalPosition: $finalPosition($durationMs), " +
                            "curPosition: $curPosition($prevPosition)"
                    }
                }
                publishProgress(
                    audioHash,
                    ProgressData(
                        currentPosition = finalPosition,
                        progress = progress,
                        duration = durationMs,
                    ),
                )
                prevCurPosition.set(curPosition)
                if (finalPosition >= durationMs) {
                    logger.i { "[pollProgress] #3; finalPosition($finalPosition) >= durationMs($durationMs)" }
                    complete(audioHash)
                    break
                }
                delay(progressUpdatePeriod)
            }
        }
    }

    private fun stopPolling() {
        logger.v { "[stopPolling] no args" }
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

    private fun NativeMediaPlayer.isSeekable(): Boolean {
        return when (state) {
            NativeMediaPlayerState.PREPARED,
            NativeMediaPlayerState.STARTED,
            NativeMediaPlayerState.PAUSED,
            NativeMediaPlayerState.PLAYBACK_COMPLETED,
            -> true

            else -> false
        }
    }

    private fun NativeMediaPlayer.isSpeedSettable(): Boolean {
        return when (state) {
            NativeMediaPlayerState.INITIALIZED,
            NativeMediaPlayerState.PREPARED,
            NativeMediaPlayerState.STARTED,
            NativeMediaPlayerState.PAUSED,
            NativeMediaPlayerState.PLAYBACK_COMPLETED,
            NativeMediaPlayerState.ERROR,
            -> true

            else -> false
        }
    }
}

internal class TrackInfo(val url: String, val hash: Int, private val positionInt: Int) : Comparable<TrackInfo> {

    override fun compareTo(other: TrackInfo): Int = this.positionInt.compareTo(other.positionInt)
}
