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

package io.getstream.chat.android.compose.ui.messages.composer

import androidx.annotation.UiThread
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.test.MockedChatClientTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageComposerTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.randomCommand
import io.getstream.chat.android.randomFloat
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.ui.common.state.messages.MessageMode.MessageThread
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class MessageComposerScreenTest : MockedChatClientTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    val mockViewModel: MessageComposerViewModel = mock()

    @Test
    @UiThread
    fun `instant commands`() {
        val command = randomCommand()
        whenever(mockViewModel.messageComposerState) doReturn
            MutableStateFlow(MessageComposerState(hasCommands = true, commandSuggestions = listOf(command)))

        composeTestRule.setContent {
            ChatTheme {
                MessageComposer(viewModel = mockViewModel)
            }
        }

        composeTestRule.onNodeWithText("Instant Commands").assertExists()
        composeTestRule.onNodeWithText("/${command.name} ${command.args}").assertExists()
    }

    @Test
    @UiThread
    fun `mention suggestions`() {
        val user = PreviewUserData.user7
        whenever(mockViewModel.messageComposerState) doReturn
            MutableStateFlow(MessageComposerState(mentionSuggestions = listOf(user)))

        composeTestRule.setContent {
            ChatTheme {
                MessageComposer(viewModel = mockViewModel)
            }
        }

        composeTestRule.onNode(hasText(user.name)).assertExists()
    }

    @Test
    @UiThread
    fun `thread mode`() {
        whenever(mockViewModel.messageComposerState) doReturn
            MutableStateFlow(MessageComposerState(messageMode = MessageThread(parentMessage = randomMessage())))

        composeTestRule.setContent {
            ChatTheme {
                MessageComposer(viewModel = mockViewModel)
            }
        }

        composeTestRule.onNodeWithText("Also send as direct message").assertExists()
    }

    @Test
    @UiThread
    fun `audio recording`() {
        val recording = RecordingState.Hold(
            durationInMs = 120_000,
            waveform = listOf(randomFloat(), randomFloat(), randomFloat(), randomFloat()),
        )
        whenever(mockViewModel.messageComposerState) doReturn
            MutableStateFlow(MessageComposerState(recording = recording))

        composeTestRule.setContent {
            val messageComposerTheme = MessageComposerTheme.defaultTheme().let {
                it.copy(audioRecording = it.audioRecording.copy(enabled = true))
            }
            ChatTheme(messageComposerTheme = messageComposerTheme) {
                MessageComposer(viewModel = mockViewModel)
            }
        }

        composeTestRule.onNodeWithText("02:00").assertExists()
        composeTestRule.onNodeWithText("Slide to cancel").assertExists()
    }
}
