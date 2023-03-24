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

            mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(newSpeed)
            publishSpeed(mediaPlayer.playbackParams.speed)
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
            setOnPreparedListener { mediaPlayer ->
                playerState = PlayerState.IDLE
                mediaPlayer.start()
            }

            setOnCompletionListener {
                onComplete()
            }

            playerState = PlayerState.LOADING
            publishAudioState(AudioState.LOADING)
            setDataSource(sourceUrl)
            prepareAsync()
        }
    }

    private fun start() {
        if (playerState == PlayerState.IDLE) {
            mediaPlayer.start()
            playerState = PlayerState.PLAYING
            publishAudioState(AudioState.PLAYING)
            poolProgress()
        }
    }

    private fun pause() {
        if (playerState == PlayerState.PLAYING) {
            mediaPlayer.pause()
            currentSeek = mediaPlayer.currentPosition
            publishAudioState(AudioState.PAUSE)
            playerState = PlayerState.IDLE
            stopPooling()
        }
    }

    private fun onComplete() {
        stopPooling()
        currentSeek = 0
        playerState = PlayerState.IDLE
        publishProgress(ProgressData(0, 0.0))
        publishAudioState(AudioState.IDLE)
    }

    private fun poolProgress() {
        poolJob = userScope.launch(Dispatchers.Default) {
            while (true) {
                delay(progressUpdatePeriod)

                val progress = mediaPlayer.currentPosition.toDouble() / mediaPlayer.duration

                withContext(Dispatchers.Main) {
                    publishProgress(ProgressData(mediaPlayer.currentPosition, progress))
                }
            }
        }
    }

    private fun stopPooling() {
        poolJob?.cancel()
    }

    private fun publishAudioState(audioState: AudioState) {
        onStateListeners[currentAudioHash]?.invoke(audioState)
    }

    private fun publishProgress(progressData: ProgressData) {
        onProgressListeners[currentAudioHash]?.invoke(progressData)
    }

    private fun publishSpeed(speed: Float) {
        onSpeedListeners[currentAudioHash]?.invoke(speed)
    }
}

public enum class AudioState {
    UNSET, LOADING, IDLE, PAUSE, PLAYING;
}

public enum class PlayerState {
    UNSET, LOADING, IDLE, PLAYING;
}

