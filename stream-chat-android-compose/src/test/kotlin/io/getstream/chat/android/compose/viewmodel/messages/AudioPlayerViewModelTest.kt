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

package io.getstream.chat.android.compose.viewmodel.messages

import androidx.lifecycle.ViewModelStore
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomFloat
import io.getstream.chat.android.ui.common.feature.messages.list.AudioPlayerController
import io.getstream.chat.android.ui.common.state.messages.list.AudioPlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class AudioPlayerViewModelTest {

    private lateinit var controllerState: MutableStateFlow<AudioPlayerState>
    private lateinit var controller: AudioPlayerController
    private lateinit var viewModel: AudioPlayerViewModel

    @BeforeEach
    fun setUp() {
        controllerState = MutableStateFlow(AudioPlayerState(getRecordingUri = { null }))
        controller = mock {
            on { state } doReturn controllerState
        }
        viewModel = AudioPlayerViewModel(controller)
    }

    @Test
    fun `state is exposed from the controller`() {
        viewModel.state `should be equal to` controllerState
    }

    @Test
    fun `playOrPause toggles playback of the attachment`() {
        val attachment = randomAttachment()

        viewModel.playOrPause(attachment)

        verify(controller).togglePlayback(attachment)
    }

    @Test
    fun `pause pauses the running audio`() {
        viewModel.pause()

        verify(controller).pause()
    }

    @Test
    fun `changeSpeed changes the speed of the attachment`() {
        val attachment = randomAttachment()

        viewModel.changeSpeed(attachment)

        verify(controller).changeSpeed(attachment)
    }

    @Test
    fun `seekTo seeks the attachment to the given progress`() {
        val attachment = randomAttachment()
        val progress = randomFloat()

        viewModel.seekTo(attachment, progress)

        verify(controller).seekTo(attachment, progress)
    }

    @Test
    fun `startSeek starts seeking the attachment`() {
        val attachment = randomAttachment()

        viewModel.startSeek(attachment)

        verify(controller).startSeek(attachment)
    }

    @Test
    fun `reset resets the audio of the attachment`() {
        val attachment = randomAttachment()

        viewModel.reset(attachment)

        verify(controller).resetAudio(attachment)
    }

    @Test
    fun `clearing the view model resets the controller`() {
        val store = ViewModelStore()
        store.put("audioPlayer", viewModel)

        store.clear()

        verify(controller).reset()
    }
}
