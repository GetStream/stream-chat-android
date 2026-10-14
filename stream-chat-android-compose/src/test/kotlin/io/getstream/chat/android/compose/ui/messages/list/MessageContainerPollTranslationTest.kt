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

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.test.MockedChatClientTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewPollData
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
internal class MessageContainerPollTranslationTest : MockedChatClientTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun prepare() {
        whenever(mockClientState.user) doReturn MutableStateFlow(randomUser())
        whenever(mockClientState.connectionState) doReturn MutableStateFlow(ConnectionState.Connected)
    }

    @Test
    fun `translated poll shows the translation annotation and switches to the original`() {
        setPollMessage(cid = "messaging:poll-translation-toggle", language = "it")

        composeTestRule.onNodeWithText("Translated").assertExists()
        composeTestRule.onNodeWithText("Pranzo?").assertExists()
        composeTestRule.onNodeWithText("Pizza!").assertExists()

        composeTestRule.onNodeWithText("View original").performClick()

        composeTestRule.onNodeWithText("Lunch?").assertExists()
        composeTestRule.onNodeWithText("Pizza").assertExists()
    }

    @Test
    fun `untranslated poll shows no translation annotation`() {
        setPollMessage(cid = "messaging:poll-translation-none", language = "fr")

        composeTestRule.onNodeWithText("Lunch?").assertExists()
        composeTestRule.onNodeWithText("Translated").assertDoesNotExist()
    }

    private fun setPollMessage(cid: String, language: String) {
        val poll = PreviewPollData.poll1.copy(
            name = "Lunch?",
            nameI18n = mapOf("language" to "en", "it_text" to "Pranzo?"),
            options = listOf(
                Option(id = "o1", text = "Pizza", textI18n = mapOf("language" to "en", "it_text" to "Pizza!")),
            ),
        )
        val messageItem = MessageItemState(
            message = Message(id = "m", cid = cid, text = "", poll = poll, user = User(id = "u2", name = "Jc")),
            isMine = false,
            currentUser = User(id = "me", language = language),
            ownCapabilities = ChannelCapabilities.toSet(),
        )
        composeTestRule.setContent {
            ChatTheme {
                MessageContainer(messageItem = messageItem, onLongItemClick = {})
            }
        }
    }
}
