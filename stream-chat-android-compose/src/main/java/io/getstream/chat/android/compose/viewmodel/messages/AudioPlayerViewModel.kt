package io.getstream.chat.android.compose.viewmodel.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.client.audio.AudioPlayer
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.feature.messages.list.AudioPlayerController
import io.getstream.chat.android.ui.common.state.messages.list.AudioPlayerState
import io.getstream.log.StreamLog
import kotlinx.coroutines.flow.StateFlow

internal class AudioPlayerViewModel(
    private val controller: AudioPlayerController
): ViewModel() {

    val state: StateFlow<AudioPlayerState?> get() = controller.state

    init {
        StreamLog.i("AudioPlayerViewModel") {
            "<init> no args"
        }
    }

    override fun onCleared() {
        StreamLog.i("AudioPlayerViewModel") {
            "[onCleared] no args"
        }
        super.onCleared()
        controller.reset()
    }

    protected fun finalize() {
        StreamLog.i("AudioPlayerViewModel") {
            "<destroy> no args"
        }
    }

    fun playOrPause(attachment: Attachment) {
        controller.togglePlayback(attachment)
    }

    fun changeSpeed(attachment: Attachment) {
        controller.changeSpeed(attachment)
    }

    fun seekTo(attachment: Attachment, progress: Float) {
        controller.seekTo(attachment, progress)
    }

    fun startSeek(attachment: Attachment) {
        controller.startSeek(attachment)
    }

    fun reset(attachment: Attachment) {
        controller.resetAudio(attachment)
    }
}

@InternalStreamChatApi
public class AudioPlayerViewModelFactory(
    private val audioPlayer: AudioPlayer,
    private val hasRecordingUri: (Attachment) -> Boolean,
    private val getRecordingUri: (Attachment) -> String?,
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AudioPlayerViewModel(AudioPlayerController(audioPlayer, hasRecordingUri, getRecordingUri)) as T
    }
}