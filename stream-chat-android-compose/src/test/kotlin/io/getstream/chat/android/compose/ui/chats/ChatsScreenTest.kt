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

package io.getstream.chat.android.compose.ui.chats

import androidx.annotation.UiThread
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.test.MockedChatClientTest
import io.getstream.chat.android.compose.ui.channels.SearchMode
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.randomUser
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
internal class ChatsScreenTest : MockedChatClientTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun prepare() {
        val user = randomUser()
        whenever(mockChatClient.getCurrentUser()) doReturn user
        whenever(mockClientState.user) doReturn MutableStateFlow(user)
        whenever(mockClientState.connectionState) doReturn MutableStateFlow(ConnectionState.Connected)
    }

    @Test
    @UiThread
    fun `with search mode none`() {
        composeTestRule.setContent {
            ChatTheme {
                ChatsScreen()
            }
        }

        composeTestRule.onNodeWithText("Stream Chat").assertExists()
        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertExists()
    }

    @Test
    @UiThread
    fun `with search mode messages`() {
        composeTestRule.setContent {
            ChatTheme {
                ChatsScreen(searchMode = SearchMode.Messages)
            }
        }

        composeTestRule.onNodeWithText("Search").assertExists()
        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertExists()
    }

    @Test
    @UiThread
    fun `with list content mode threads`() {
        composeTestRule.setContent {
            ChatTheme {
                ChatsScreen(listContentMode = ChatListContentMode.Threads)
            }
        }

        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertExists()
    }
}
