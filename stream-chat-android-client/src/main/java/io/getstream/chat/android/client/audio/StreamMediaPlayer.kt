package io.getstream.chat.android.client.audio

import android.media.MediaPlayer
import io.getstream.chat.android.client.scope.UserScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Todo: This class can be internal an implement a interface
internal class StreamMediaPlayer(
    private val mediaPlayer: MediaPlayer,
    private val userScope: UserScope,
    private val progressUpdatePeriod: Long = 50
) : RecordsPlayer{

    private val onStateListeners: MutableMap<String, (AudioState) -> Unit> = mutableMapOf()
    private val onProgressListeners: MutableMap<String, (ProgressData) -> Unit> = mutableMapOf()
    private var currentSeek = 0
    private var playerState = PlayerState.UNSET
    private var poolJob: Job? = null

    override fun onAudioStateChange(hash: String, func: (AudioState) -> Unit) {
        onStateListeners[hash] = func
    }

    override fun onProgressStateChange(hash: String, func: (ProgressData) -> Unit) {
        onProgressListeners[hash] = func
    }

    override fun play(sourceUrl: String) {
        when (playerState) {
            PlayerState.UNSET -> init(sourceUrl)
            PlayerState.LOADING -> {}
            PlayerState.IDLE -> start()
            PlayerState.PLAYING -> pause()
        }
    }

    override fun dispose() {
        stopPooling()
        onStateListeners.clear()
        onProgressListeners.clear()
        mediaPlayer.release()
    }

    private fun init(sourceUrl: String) {
        mediaPlayer.run {
            setOnPreparedListener {
                playerState = PlayerState.IDLE
                this@StreamMediaPlayer.start()
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
        // Todo: Publish only for the correct ID
        onStateListeners.values.forEach { listener -> listener(audioState) }
    }

    private fun publishProgress(progressData: ProgressData) {
        // Todo: Publish only for the correct ID
        onProgressListeners.values.forEach { listener -> listener(progressData) }
    }
}

public enum class AudioState {
    UNSET, LOADING, IDLE, PAUSE, PLAYING;
}

public enum class PlayerState {
    UNSET, LOADING, IDLE, PLAYING;
}

