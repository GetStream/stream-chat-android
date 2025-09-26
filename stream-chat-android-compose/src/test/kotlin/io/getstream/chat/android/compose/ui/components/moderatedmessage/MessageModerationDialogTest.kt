/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.components.moderatedmessage

import androidx.annotation.UiThread
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.ui.common.state.messages.list.DeleteMessage
import io.getstream.chat.android.ui.common.state.messages.list.EditMessage
import io.getstream.chat.android.ui.common.state.messages.list.ModeratedMessageOption
import io.getstream.chat.android.ui.common.state.messages.list.SendAnyway
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class MessageModerationDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    @UiThread
    fun `shows MessageModerationDialog with correct content`() {
        val onDismissRequest = mock<() -> Unit>()
        val onDialogOptionInteraction = mock<(Message, ModeratedMessageOption) -> Unit>()

        composeTestRule.setContent {
            ChatTheme {
                ModeratedMessageDialog(
                    message = ModeratedMessage,
                    onDismissRequest = onDismissRequest,
                    onDialogOptionInteraction = onDialogOptionInteraction,
                )
            }
        }

        composeTestRule.onNodeWithText("Failed to send message").assertExists()
        composeTestRule.onNodeWithText(
            "This message might violate our moderation policy. " +
                "Are you sure you want to send it? Please take a look at our Community Guidelines.",
        ).assertExists()
        composeTestRule.onNodeWithText("Send anyway").assertExists()
        composeTestRule.onNodeWithText("Edit message").assertExists()
        composeTestRule.onNodeWithText("Delete message").assertExists()
    }

    @Test
    @UiThread
    fun `calls onDialogOptionInteraction when Send anyway is clicked`() {
        val onDismissRequest = mock<() -> Unit>()
        val onDialogOptionInteraction = mock<(Message, ModeratedMessageOption) -> Unit>()

        composeTestRule.setContent {
            ChatTheme {
                ModeratedMessageDialog(
                    message = ModeratedMessage,
                    onDismissRequest = onDismissRequest,
                    onDialogOptionInteraction = onDialogOptionInteraction,
                )
            }
        }

        composeTestRule.onNodeWithText("Send anyway").performClick()

        verify(onDialogOptionInteraction).invoke(ModeratedMessage, SendAnyway)
        verify(onDismissRequest).invoke()
    }

    @Test
    @UiThread
    fun `calls onDialogOptionInteraction when Edit message is clicked`() {
        val onDismissRequest = mock<() -> Unit>()
        val onDialogOptionInteraction = mock<(Message, ModeratedMessageOption) -> Unit>()

        composeTestRule.setContent {
            ChatTheme {
                ModeratedMessageDialog(
                    message = ModeratedMessage,
                    onDismissRequest = onDismissRequest,
                    onDialogOptionInteraction = onDialogOptionInteraction,
                )
            }
        }

        composeTestRule.onNodeWithText("Edit message").performClick()

        verify(onDialogOptionInteraction).invoke(ModeratedMessage, EditMessage)
        verify(onDismissRequest).invoke()
    }

    @Test
    @UiThread
    fun `calls onDialogOptionInteraction when Delete message is clicked`() {
        val onDismissRequest = mock<() -> Unit>()
        val onDialogOptionInteraction = mock<(Message, ModeratedMessageOption) -> Unit>()

        composeTestRule.setContent {
            ChatTheme {
                ModeratedMessageDialog(
                    message = ModeratedMessage,
                    onDismissRequest = onDismissRequest,
                    onDialogOptionInteraction = onDialogOptionInteraction,
                )
            }
        }

        composeTestRule.onNodeWithText("Delete message").performClick()

        verify(onDialogOptionInteraction).invoke(ModeratedMessage, DeleteMessage)
        verify(onDismissRequest).invoke()
    }
}

private val ModeratedMessage = randomMessage()
