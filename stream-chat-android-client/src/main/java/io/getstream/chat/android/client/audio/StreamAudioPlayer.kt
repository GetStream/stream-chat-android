package io.getstream.chat.android.client.audio

import android.media.MediaPlayer

// Todo: This class can be internal an implement a interface
public class StreamAudioPlayer(private val mediaPlayer: MediaPlayer) {

    private val onStateListeners: MutableMap<String, (AudioState) -> Unit> = mutableMapOf()
    private var currentSeek = 0
    private var playerState = PlayerState.UNSET

    private fun publishAudioState(audioState: AudioState) {
        // Todo: Publish only for the correct ID
        onStateListeners.values.forEach { listener -> listener(audioState) }
    }

    public fun onAudioStateChange(hash: String, func: (AudioState) -> Unit) {
        onStateListeners[hash] = func
    }

    public fun play(sourceUrl: String) {
        if (playerState == PlayerState.UNSET) {
            init(sourceUrl)
        } else {
            start()
        }
    }

    private fun init(sourceUrl: String) {
        mediaPlayer.run {
            setOnPreparedListener {
                playerState = PlayerState.IDLE
                start()
            }

            setOnCompletionListener {
                currentSeek = 0
                playerState = PlayerState.IDLE
                publishAudioState(AudioState.IDLE)
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
        }
    }

}

public enum class AudioState {
    UNSET, LOADING, IDLE, PLAYING;
}

public enum class PlayerState {
    UNSET, LOADING, IDLE, PLAYING;
}

