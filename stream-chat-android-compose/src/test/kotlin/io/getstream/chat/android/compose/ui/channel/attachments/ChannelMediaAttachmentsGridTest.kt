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

package io.getstream.chat.android.compose.ui.channel.attachments

import androidx.annotation.UiThread
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.test.MockedChatClientTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.randomString
import io.getstream.chat.android.ui.common.state.channel.attachments.ChannelAttachmentsViewState
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
internal class ChannelMediaAttachmentsGridTest : MockedChatClientTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun prepare() {
        whenever(mockClientState.connectionState) doReturn MutableStateFlow(ConnectionState.Connected)
    }

    @Test
    @UiThread
    fun `loading state`() {
        composeTestRule.setContent {
            ChatTheme {
                ChannelMediaAttachmentsGrid(
                    viewState = ChannelAttachmentsViewState.Loading,
                )
            }
        }

        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertExists()
    }

    @Test
    @UiThread
    fun `content state`() {
        val message = PreviewMessageData.messageWithUserAndAttachment
        val items = message.attachments.map { attachment ->
            ChannelAttachmentsViewState.Content.Item(
                message = message,
                attachment = attachment,
            )
        }

        composeTestRule.setContent {
            ChatTheme {
                ChannelMediaAttachmentsGrid(
                    viewState = ChannelAttachmentsViewState.Content(
                        items = items,
                    ),
                )
            }
        }

        composeTestRule.onNodeWithText("Today").assertExists()
        composeTestRule.onAllNodesWithText("T")[0].assertExists()
        composeTestRule.onAllNodesWithText("T")[1].assertExists()
        composeTestRule.onAllNodesWithText("T")[1].assert(hasContentDescription("Play button"))
    }

    @Test
    @UiThread
    fun `content click`() {
        val message = PreviewMessageData.messageWithUserAndAttachment
        val items = message.attachments.map { attachment ->
            ChannelAttachmentsViewState.Content.Item(
                message = message,
                attachment = attachment,
            )
        }

        composeTestRule.setContent {
            ChatTheme {
                ChannelMediaAttachmentsGrid(
                    viewState = ChannelAttachmentsViewState.Content(
                        items = items,
                    ),
                )
            }
        }

        composeTestRule.onAllNodesWithText("T")[0].performClick()

        composeTestRule.onNodeWithText("Test User").assertExists()
        composeTestRule.onNodeWithText("just now").assertExists()
        composeTestRule.onNodeWithText("1 of 2").assertExists()
    }

    @Test
    @UiThread
    fun `error state`() {
        composeTestRule.setContent {
            ChatTheme {
                ChannelMediaAttachmentsGrid(
                    viewState = ChannelAttachmentsViewState.Error(message = randomString()),
                )
            }
        }

        composeTestRule.onNodeWithText("Failed to load media attachments!").assertExists()
    }
}
