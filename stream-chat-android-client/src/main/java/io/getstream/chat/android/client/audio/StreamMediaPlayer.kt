package io.getstream.chat.android.client.audio

import android.media.MediaPlayer
import android.os.Build
import io.getstream.chat.android.client.scope.UserScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class StreamMediaPlayer(
    private val mediaPlayer: MediaPlayer,
    private val userScope: UserScope,
    private val progressUpdatePeriod: Long = 50,
) : RecordsPlayer {

    private val onStateListeners: MutableMap<Int, (AudioState) -> Unit> = mutableMapOf()
    private val onProgressListeners: MutableMap<Int, (ProgressData) -> Unit> = mutableMapOf()
    private val onSpeedListeners: MutableMap<Int, (Float) -> Unit> = mutableMapOf()
    private var currentSeek = 0
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
            PlayerState.IDLE -> start()
            PlayerState.PLAYING -> pause()
        }
    }

    override fun changeSpeed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val currentSpeed = mediaPlayer.playbackParams.speed
            val newSpeed = if (currentSpeed >= 2) {
                1.0F
            } else {
                currentSpeed + 0.5F
            }

            playingSpeed = newSpeed

            mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(newSpeed)
            publishSpeed(currentAudioHash, mediaPlayer.playbackParams.speed)
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
        if (playerState == PlayerState.IDLE) {
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

    private fun pause() {
        if (playerState == PlayerState.PLAYING) {
            mediaPlayer.pause()
            currentSeek = mediaPlayer.currentPosition
            publishAudioState(currentAudioHash, AudioState.PAUSE)
            playerState = PlayerState.IDLE
            stopPooling()
        }
    }

    private fun onComplete() {
        stopPooling()
        currentSeek = 0
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

public enum class AudioState {
    UNSET, LOADING, IDLE, PAUSE, PLAYING;
}

public enum class PlayerState {
    UNSET, LOADING, IDLE, PLAYING;
}

