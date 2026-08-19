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

package io.getstream.chat.android.compose.ui.components.messages

import androidx.annotation.UiThread
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.test.MockedChatClientTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class PollMessageContentInteractionsTest : MockedChatClientTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun prepare() {
        whenever(mockClientState.user) doReturn MutableStateFlow(randomUser())
        whenever(mockClientState.connectionState) doReturn MutableStateFlow(ConnectionState.Connected)
    }

    // A dialog window opened by a later recomposition does not register with the compose test
    // rule under Robolectric, so the end-poll flow is split: this test pins that the End Poll
    // button never closes the poll directly, and the dialog itself is composed directly below.
    @Test
    @UiThread
    fun `ending a poll does not close it without confirmation`() {
        var closedPollId: String? = null
        composeTestRule.setContent {
            ChatTheme {
                PollMessageContent(
                    modifier = Modifier,
                    onCastVote = { _, _, _ -> },
                    onRemoveVote = { _, _, _ -> },
                    selectPoll = { _, _, _ -> },
                    onAddAnswer = { _, _, _ -> },
                    onClosePoll = { closedPollId = it },
                    onAddPollOption = { _, _ -> },
                    messageItem = MessageItemState(
                        message = PreviewMessageData.messageWithPoll,
                        isMine = true,
                        ownCapabilities = ChannelCapabilities.toSet(),
                    ),
                )
            }
        }

        composeTestRule.onNodeWithTag("Stream_PollEndButton").performClick()

        assertNull(closedPollId)
    }

    @Test
    @UiThread
    fun `end poll confirmation confirms`() {
        var confirmed = 0
        var dismissed = 0
        setConfirmationDialog(onConfirm = { confirmed++ }, onDismiss = { dismissed++ })

        composeTestRule.onNodeWithTag("Stream_PollEndConfirmButton").performClick()

        assertEquals(1, confirmed)
        assertEquals(0, dismissed)
    }

    @Test
    @UiThread
    fun `end poll confirmation dismisses`() {
        var confirmed = 0
        var dismissed = 0
        setConfirmationDialog(onConfirm = { confirmed++ }, onDismiss = { dismissed++ })

        composeTestRule.onNodeWithTag("Stream_PollEndDismissButton").performClick()

        assertEquals(0, confirmed)
        assertEquals(1, dismissed)
    }

    private fun setConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
        composeTestRule.setContent {
            ChatTheme {
                EndPollConfirmationDialog(onConfirm = onConfirm, onDismiss = onDismiss)
            }
        }
    }
}
