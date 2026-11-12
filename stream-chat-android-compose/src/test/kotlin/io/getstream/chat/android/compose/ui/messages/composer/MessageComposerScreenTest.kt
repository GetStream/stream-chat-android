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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.test.MockedChatClientTest
import io.getstream.chat.android.compose.ui.messages.composer.schedule.LocalMessageScheduleOptions
import io.getstream.chat.android.compose.ui.messages.composer.schedule.MessageScheduleOptions
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ChatUiConfig
import io.getstream.chat.android.compose.ui.theme.ComposerConfig
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.randomCommand
import io.getstream.chat.android.randomFloat
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.ui.common.feature.messages.composer.scheduled.scheduleMessageQuickDates
import io.getstream.chat.android.ui.common.state.messages.MessageMode.MessageThread
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.chat.android.ui.common.state.messages.composer.ScheduleMessageState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class MessageComposerScreenTest : MockedChatClientTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    val mockViewModel: MessageComposerViewModel = mock {
        on { inputFocusEvents } doReturn MutableSharedFlow()
        on { events } doReturn MutableSharedFlow()
        on { scheduleMessageState } doReturn MutableStateFlow(ScheduleMessageState())
    }

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
        composeTestRule.onNodeWithText(command.name.replaceFirstChar(Char::uppercase)).assertExists()
        composeTestRule.onNodeWithText(command.description).assertExists()
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

        composeTestRule.onNodeWithText("Also send in Channel").assertExists()
    }

    @Test
    @UiThread
    fun `slow mode disables input and attachment button`() {
        val capabilities = setOf(ChannelCapabilities.SEND_MESSAGE, ChannelCapabilities.UPLOAD_FILE)
        whenever(mockViewModel.messageComposerState) doReturn
            MutableStateFlow(MessageComposerState(ownCapabilities = capabilities, coolDownTime = 9))

        composeTestRule.setContent {
            ChatTheme {
                MessageComposer(viewModel = mockViewModel)
            }
        }

        composeTestRule.onNodeWithText("9").assertExists()
        composeTestRule.onNodeWithText("Slow mode, wait 9s…").assertExists()
        composeTestRule.onNodeWithTag(ComposerInputTestTag).assertIsNotEnabled()
        composeTestRule.onNodeWithTag("Stream_ComposerAttachmentsButton").assertIsNotEnabled()
    }

    @Test
    @UiThread
    fun `composer stays interactive without cooldown`() {
        val capabilities = setOf(ChannelCapabilities.SEND_MESSAGE, ChannelCapabilities.UPLOAD_FILE)
        whenever(mockViewModel.messageComposerState) doReturn
            MutableStateFlow(MessageComposerState(ownCapabilities = capabilities, coolDownTime = 0))

        composeTestRule.setContent {
            ChatTheme {
                MessageComposer(viewModel = mockViewModel)
            }
        }

        composeTestRule.onNodeWithTag(ComposerInputTestTag).assertIsEnabled()
        composeTestRule.onNodeWithTag("Stream_ComposerAttachmentsButton").assertIsEnabled()
    }

    @Test
    @UiThread
    fun `audio recording hint is rendered by the composer`() {
        whenever(mockViewModel.messageComposerState) doReturn MutableStateFlow(MessageComposerState())
        val hintHost = SnackbarHostState()
        val hintMessage = "Hold to record. Release to save."

        composeTestRule.setContent {
            ChatTheme {
                CompositionLocalProvider(LocalMessageComposerRecordingHintHostState provides hintHost) {
                    MessageComposer(viewModel = mockViewModel)
                }
                LaunchedEffect(Unit) { hintHost.showSnackbar(hintMessage, duration = SnackbarDuration.Indefinite) }
            }
        }

        // The composer, not the mic button, renders the hint from the provided host, so it anchors
        // above the composer.
        composeTestRule.onNodeWithText(hintMessage).assertExists()
    }

    @Test
    @UiThread
    fun `audio recording permission rationale is rendered by the composer`() {
        whenever(mockViewModel.messageComposerState) doReturn MutableStateFlow(MessageComposerState())
        val rationaleHost = SnackbarHostState()
        val rationaleMessage = "Microphone permission is required."

        composeTestRule.setContent {
            ChatTheme {
                CompositionLocalProvider(LocalMessageComposerRecordingRationaleHostState provides rationaleHost) {
                    MessageComposer(viewModel = mockViewModel)
                }
                LaunchedEffect(Unit) {
                    rationaleHost.showSnackbar(rationaleMessage, duration = SnackbarDuration.Indefinite)
                }
            }
        }

        composeTestRule.onNodeWithText(rationaleMessage).assertExists()
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
            ChatTheme {
                MessageComposer(viewModel = mockViewModel)
            }
        }

        composeTestRule.onNodeWithText("02:00").assertExists()
        composeTestRule.onNodeWithText("Slide to cancel").assertExists()
    }

    @Test
    @UiThread
    fun `long press on send opens the schedule sheet and a tap still sends`() {
        givenComposerWithText(canSchedule = true)

        composeTestRule.setContent {
            ChatTheme {
                MessageComposer(viewModel = mockViewModel)
            }
        }

        composeTestRule.onNodeWithTag("Stream_ComposerSendButton").performTouchInput { longClick() }
        waitForKeyboardToHide()
        composeTestRule.onNodeWithText("Schedule message").assertExists()
        composeTestRule.onNodeWithText("Custom time").assertExists()

        composeTestRule.onNodeWithTag("Stream_ComposerSendButton").performClick()
        verify(mockViewModel).sendMessage(any(), any())
    }

    @Test
    @UiThread
    fun `a screen reader can open the schedule sheet with a named action`() {
        givenComposerWithText(canSchedule = true)

        composeTestRule.setContent {
            ChatTheme {
                MessageComposer(viewModel = mockViewModel)
            }
        }

        val sendButton = composeTestRule.onNodeWithTag("Stream_ComposerSendButton")
        val actions = sendButton.fetchSemanticsNode().config[SemanticsActions.CustomActions]
        assertEquals(listOf("Schedule message"), actions.map { it.label })
        composeTestRule.runOnUiThread { actions.single().action() }
        waitForKeyboardToHide()
        composeTestRule.onNodeWithText("Custom time").assertExists()
    }

    @Test
    @UiThread
    fun `picking a quick date schedules the message of the composer`() {
        val message = givenComposerWithText(canSchedule = true)

        composeTestRule.setContent {
            ChatTheme {
                MessageComposer(viewModel = mockViewModel)
            }
        }

        composeTestRule.onNodeWithTag("Stream_ComposerSendButton").performTouchInput { longClick() }
        waitForKeyboardToHide()
        composeTestRule.onNodeWithTag("Stream_ScheduleMessageQuickDate0")
            .performSemanticsAction(SemanticsActions.OnClick)

        val dateCaptor = argumentCaptor<Date>()
        verify(mockViewModel).scheduleMessage(eq(message), dateCaptor.capture(), any())
        assertEquals(scheduleMessageQuickDates(Date()).first(), dateCaptor.firstValue)
    }

    @Test
    @UiThread
    fun `a scheduled message skips the push and the URL enrichment like the screen asks`() {
        val message = givenComposerWithText(canSchedule = true)

        composeTestRule.setContent {
            ChatTheme {
                CompositionLocalProvider(
                    LocalMessageScheduleOptions provides MessageScheduleOptions(
                        skipPushNotification = true,
                        skipEnrichUrl = true,
                    ),
                ) {
                    MessageComposer(viewModel = mockViewModel)
                }
            }
        }

        composeTestRule.onNodeWithTag("Stream_ComposerSendButton").performTouchInput { longClick() }
        waitForKeyboardToHide()
        composeTestRule.onNodeWithTag("Stream_ScheduleMessageQuickDate0")
            .performSemanticsAction(SemanticsActions.OnClick)

        verify(mockViewModel).scheduleMessage(
            eq(message.copy(skipPushNotification = true, skipEnrichUrl = true)),
            any(),
            any(),
        )
    }

    @Test
    @UiThread
    fun `a sheet closed by an emptied composer does not open again when the user types`() {
        givenComposerWithText(canSchedule = true)
        val composerState = MutableStateFlow(
            MessageComposerState(inputValue = "Heads up", ownCapabilities = setOf(ChannelCapabilities.SEND_MESSAGE)),
        )
        whenever(mockViewModel.messageComposerState) doReturn composerState

        composeTestRule.setContent {
            ChatTheme {
                MessageComposer(viewModel = mockViewModel)
            }
        }
        composeTestRule.onNodeWithTag("Stream_ComposerSendButton").performTouchInput { longClick() }
        waitForKeyboardToHide()
        composeTestRule.onNodeWithTag("Stream_ScheduleMessageSheet").assertExists()

        // For example, the draft was deleted on another device.
        composerState.value = composerState.value.copy(inputValue = "")
        composeTestRule.waitForIdle()
        composerState.value = composerState.value.copy(inputValue = "Next message")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("Stream_ScheduleMessageSheet").assertDoesNotExist()
    }

    @Test
    @UiThread
    fun `the message is built only when it is scheduled, as building clears the picked mentions`() {
        val message = givenComposerWithText(canSchedule = true)

        composeTestRule.setContent {
            ChatTheme {
                MessageComposer(viewModel = mockViewModel)
            }
        }
        composeTestRule.onNodeWithTag("Stream_ComposerSendButton").performTouchInput { longClick() }
        waitForKeyboardToHide()
        composeTestRule.onNodeWithTag("Stream_ScheduleMessageCustomTime")
            .performSemanticsAction(SemanticsActions.OnClick)
        composeTestRule.onNodeWithTag("Stream_ScheduleMessageCancel").performSemanticsAction(SemanticsActions.OnClick)
        composeTestRule.waitForIdle()
        verify(mockViewModel, never()).buildNewMessage(any(), any())

        composeTestRule.onNodeWithTag("Stream_ComposerSendButton").performTouchInput { longClick() }
        waitForKeyboardToHide()
        composeTestRule.onNodeWithTag("Stream_ScheduleMessageCustomTime")
            .performSemanticsAction(SemanticsActions.OnClick)
        composeTestRule.onNodeWithTag("Stream_ScheduleMessageConfirm").performSemanticsAction(SemanticsActions.OnClick)
        composeTestRule.waitForIdle()

        verify(mockViewModel, times(1)).buildNewMessage(any(), any())
        verify(mockViewModel).scheduleMessage(eq(message), any(), any())
    }

    @Test
    @UiThread
    fun `send has no schedule action without the capability`() {
        givenComposerWithText(canSchedule = false)

        composeTestRule.setContent {
            ChatTheme {
                MessageComposer(viewModel = mockViewModel)
            }
        }

        composeTestRule.onNodeWithTag("Stream_ComposerSendButton")
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.CustomActions))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnLongClick))
    }

    @Test
    @UiThread
    fun `send has no schedule action when the app turns scheduled messages off`() {
        givenComposerWithText(canSchedule = true)

        composeTestRule.setContent {
            ChatTheme(config = ChatUiConfig(composer = ComposerConfig(scheduledMessagesEnabled = false))) {
                MessageComposer(viewModel = mockViewModel)
            }
        }

        composeTestRule.onNodeWithTag("Stream_ComposerSendButton")
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.CustomActions))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnLongClick))
    }

    /** The schedule sheet opens once the keyboard is hidden, which the test window never reports. */
    private fun waitForKeyboardToHide() {
        composeTestRule.mainClock.advanceTimeBy(1_000)
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(1_000)
    }

    private fun givenComposerWithText(canSchedule: Boolean): Message {
        val message = Message(text = "Heads up")
        whenever(mockViewModel.messageComposerState) doReturn MutableStateFlow(
            MessageComposerState(inputValue = "Heads up", ownCapabilities = setOf(ChannelCapabilities.SEND_MESSAGE)),
        )
        whenever(mockViewModel.scheduleMessageState) doReturn
            MutableStateFlow(ScheduleMessageState(canSchedule = canSchedule))
        whenever(mockViewModel.buildNewMessage(any(), any())) doReturn message
        whenever(mockViewModel.channelState) doReturn MutableStateFlow(null)
        return message
    }
}
