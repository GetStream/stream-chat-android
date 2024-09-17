/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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
    private val controller: AudioPlayerController,
) : ViewModel() {

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
    private val getAudioPlayer: () -> AudioPlayer,
    private val hasRecordingUri: (Attachment) -> Boolean,
    private val getRecordingUri: (Attachment) -> String?,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AudioPlayerViewModel(AudioPlayerController(getAudioPlayer(), hasRecordingUri, getRecordingUri)) as T
    }
}
