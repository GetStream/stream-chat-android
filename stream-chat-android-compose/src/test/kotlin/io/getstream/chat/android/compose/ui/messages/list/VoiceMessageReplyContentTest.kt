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

package io.getstream.chat.android.compose.ui.messages.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.test.MockedChatClientTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class VoiceMessageReplyContentTest : MockedChatClientTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun prepare() {
        whenever(mockClientState.user) doReturn MutableStateFlow(randomUser())
        whenever(mockClientState.connectionState) doReturn MutableStateFlow(ConnectionState.Connected)
    }

    @Test
    fun `voice message reply without a caption renders`() {
        setMessageContent(voiceReply(text = ""))

        composeTestRule.onNodeWithTag("Stream_MessageCell").assertIsDisplayed()
    }

    @Test
    fun `voice message reply with a caption renders`() {
        setMessageContent(voiceReply(text = "Nice"))

        composeTestRule.onNodeWithTag("Stream_MessageCell").assertIsDisplayed()
    }

    @Test
    fun `voice message reply with a custom attachment renders`() {
        setMessageContent(voiceReply(text = "", extra = Attachment(type = "custom")))

        composeTestRule.onNodeWithTag("Stream_MessageCell").assertIsDisplayed()
    }

    private fun voiceReply(text: String, extra: Attachment? = null) = MessageItemState(
        message = Message(
            id = "m",
            text = text,
            attachments = listOfNotNull(
                Attachment(type = AttachmentType.AUDIO_RECORDING, assetUrl = "https://example.com/audio.aac"),
                extra,
            ),
            user = User(id = "me"),
            replyTo = Message(id = "q", text = "Original", user = User(id = "u2")),
        ),
        isMine = true,
        currentUser = User(id = "me"),
        ownCapabilities = ChannelCapabilities.toSet(),
    )

    private fun setMessageContent(messageItem: MessageItemState) {
        composeTestRule.setContent {
            ChatTheme {
                MessageContainer(messageItem = messageItem, onLongItemClick = {})
            }
        }
    }
}
