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

import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.test.MockedChatClientTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.models.Attachment
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
import java.util.Date

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class MessageContainerSenderAttributionTest : MockedChatClientTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun prepare() {
        whenever(mockClientState.user) doReturn MutableStateFlow(randomUser())
        whenever(mockClientState.connectionState) doReturn MutableStateFlow(ConnectionState.Connected)
    }

    @Test
    fun `outgoing message is announced as sent by the current user`() {
        setMessageContent(
            MessageItemState(
                message = Message(id = "m1", text = "Hello", user = User(id = "me")),
                isMine = true,
                currentUser = User(id = "me"),
                ownCapabilities = ChannelCapabilities.toSet(),
            ),
        )

        // Assert on the merged tree to prove the label joins the message cell's single
        // TalkBack announcement, not just that the leaf node exists.
        composeTestRule
            .onNodeWithContentDescription("You said, Hello")
            .assertExists()
    }

    @Test
    fun `incoming message is announced with the sender name`() {
        setMessageContent(
            MessageItemState(
                message = Message(id = "m2", text = "Hi", user = User(id = "u2", name = "Petar Velikov")),
                isMine = false,
                currentUser = User(id = "me"),
                showMessageFooter = true,
                ownCapabilities = ChannelCapabilities.toSet(),
            ),
        )

        composeTestRule
            .onNodeWithContentDescription("Petar Velikov said, Hi")
            .assertExists()
    }

    @Test
    fun `single image attachment without text announces the sender`() {
        setMessageContent(
            ownAttachmentMessage(Attachment(type = "image", imageUrl = "https://example.com/i.png")),
        )

        composeTestRule
            .onNodeWithContentDescription("You said, Image attachment")
            .assertExists()
    }

    @Test
    fun `multiple image attachments without text announce the sender once`() {
        setMessageContent(
            ownAttachmentMessage(
                Attachment(type = "image", imageUrl = "https://example.com/1.png"),
                Attachment(type = "image", imageUrl = "https://example.com/2.png"),
            ),
        )

        composeTestRule
            .onNodeWithContentDescription("You said, 2 attachments")
            .assertExists()
    }

    @Test
    fun `file attachment without text announces the sender`() {
        setMessageContent(
            ownAttachmentMessage(
                Attachment(type = "file", title = "report.pdf", assetUrl = "https://example.com/report.pdf"),
            ),
        )

        composeTestRule
            .onNodeWithContentDescription("You said, report.pdf")
            .assertExists()
    }

    @Test
    fun `giphy attachment without text announces the sender`() {
        setMessageContent(
            ownAttachmentMessage(
                Attachment(type = "giphy", title = "cat", titleLink = "https://giphy.com/cat.gif"),
            ),
        )

        composeTestRule
            .onNodeWithContentDescription("You said, cat, GIPHY")
            .assertExists()
    }

    @Test
    fun `message with text and an attachment announces both`() {
        setMessageContent(
            MessageItemState(
                message = Message(
                    id = "m",
                    text = "Hello",
                    attachments = listOf(Attachment(type = "image", imageUrl = "https://example.com/i.png")),
                    user = User(id = "me"),
                ),
                isMine = true,
                currentUser = User(id = "me"),
                ownCapabilities = ChannelCapabilities.toSet(),
            ),
        )

        // The text leaf carries the sender; the attachment still announces its content on the row.
        composeTestRule.onNodeWithTag("Stream_MessageCell")
            .assertContentDescriptionContains("You said, Hello")
        composeTestRule.onNodeWithTag("Stream_MessageCell")
            .assertContentDescriptionContains("Image attachment")
    }

    @Test
    fun `text reply announces that it is a reply`() {
        setMessageContent(
            MessageItemState(
                message = Message(
                    id = "m",
                    text = "Hi",
                    user = User(id = "me"),
                    replyTo = Message(id = "q", text = "Original", user = User(id = "u2")),
                ),
                isMine = true,
                currentUser = User(id = "me"),
                ownCapabilities = ChannelCapabilities.toSet(),
            ),
        )

        composeTestRule.onNodeWithTag("Stream_MessageCell")
            .assertContentDescriptionContains("You replied, Hi")
    }

    @Test
    fun `attachment-only reply announces that it is a reply`() {
        setMessageContent(
            MessageItemState(
                message = Message(
                    id = "m",
                    text = "",
                    attachments = listOf(Attachment(type = "image", imageUrl = "https://example.com/i.png")),
                    user = User(id = "me"),
                    replyTo = Message(id = "q", text = "Original", user = User(id = "u2")),
                ),
                isMine = true,
                currentUser = User(id = "me"),
                ownCapabilities = ChannelCapabilities.toSet(),
            ),
        )

        composeTestRule.onNodeWithTag("Stream_MessageCell")
            .assertContentDescriptionContains("You replied, Image attachment")
    }

    @Test
    fun `deleted message announces the sender`() {
        setMessageContent(
            MessageItemState(
                message = Message(id = "m", user = User(id = "me"), deletedAt = Date()),
                isMine = true,
                currentUser = User(id = "me"),
                ownCapabilities = ChannelCapabilities.toSet(),
            ),
        )

        composeTestRule
            .onNodeWithContentDescription("You said, Message deleted")
            .assertExists()
    }

    private fun ownAttachmentMessage(vararg attachments: Attachment) = MessageItemState(
        message = Message(id = "m", text = "", attachments = attachments.toList(), user = User(id = "me")),
        isMine = true,
        currentUser = User(id = "me"),
        ownCapabilities = ChannelCapabilities.toSet(),
    )

    private fun setMessageContent(messageItem: MessageItemState) {
        composeTestRule.setContent {
            ChatTheme {
                MessageContainer(
                    messageItem = messageItem,
                    onLongItemClick = {},
                )
            }
        }
    }
}
