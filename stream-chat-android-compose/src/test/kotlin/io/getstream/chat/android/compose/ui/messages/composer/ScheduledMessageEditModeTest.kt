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

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.test.MockedChatClientTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerViewEvent
import io.getstream.chat.android.ui.common.state.messages.composer.ScheduleMessageState
import kotlinx.coroutines.flow.MutableSharedFlow
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
internal class ScheduledMessageEditModeTest : MockedChatClientTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val edited = Message(id = "scheduled", text = "Heads up")
    private val events = MutableSharedFlow<MessageComposerViewEvent>(extraBufferCapacity = 1)
    private val viewModel: MessageComposerViewModel = mock {
        on { inputFocusEvents } doReturn MutableSharedFlow()
        on { events } doReturn events
        on { scheduleMessageState } doReturn MutableStateFlow(ScheduleMessageState())
        on { messageComposerState } doReturn MutableStateFlow(
            MessageComposerState(
                inputValue = "Heads up, edited",
                action = Edit(edited),
                ownCapabilities = setOf(ChannelCapabilities.SEND_MESSAGE),
            ),
        )
    }

    @Test
    fun `the edit indicator names a scheduled message`() {
        setContent(ScheduledMessageEditMode(canSave = true))

        composeTestRule.onNodeWithText("Edit scheduled message").assertExists()
        composeTestRule.onNodeWithText("Edit Message").assertDoesNotExist()
    }

    @Test
    fun `the edit indicator of any other edit names a message`() {
        setContent(editMode = null)

        composeTestRule.onNodeWithText("Edit Message").assertExists()
        composeTestRule.onNodeWithText("Edit scheduled message").assertDoesNotExist()
    }

    @Test
    fun `the save button is enabled when the scheduled message can be saved`() {
        setContent(ScheduledMessageEditMode(canSave = true))

        composeTestRule.onNodeWithTag("Stream_ComposerSaveButton").assertIsEnabled()
    }

    @Test
    fun `the save button is disabled when the scheduled message can't be saved`() {
        setContent(ScheduledMessageEditMode(canSave = false))

        composeTestRule.onNodeWithTag("Stream_ComposerSaveButton").assertIsNotEnabled()
    }

    @Test
    fun `a command typed in a scheduled message tells that commands can't be scheduled`() {
        setContent(ScheduledMessageEditMode(canSave = false))

        events.tryEmit(MessageComposerViewEvent.CommandUnavailable(Edit(edited)))
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Commands can't be scheduled. Remove the / command to continue.")
            .assertExists()
    }

    @Test
    fun `slow mode shows the save button of a scheduled message, not the cooldown`() {
        whenever(viewModel.messageComposerState) doReturn MutableStateFlow(
            MessageComposerState(
                inputValue = "Heads up, edited",
                action = Edit(edited),
                ownCapabilities = setOf(ChannelCapabilities.SEND_MESSAGE),
                coolDownTime = 30,
            ),
        )
        setContent(ScheduledMessageEditMode(canSave = true))

        composeTestRule.onNodeWithTag("Stream_ComposerSaveButton").assertIsEnabled()
    }

    private fun setContent(editMode: ScheduledMessageEditMode?) {
        composeTestRule.setContent {
            ChatTheme {
                CompositionLocalProvider(LocalScheduledMessageEditMode provides editMode) {
                    MessageComposer(viewModel = viewModel)
                }
            }
        }
        composeTestRule.waitForIdle()
    }
}
