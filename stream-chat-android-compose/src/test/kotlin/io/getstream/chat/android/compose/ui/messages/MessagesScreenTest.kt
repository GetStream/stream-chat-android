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

package io.getstream.chat.android.compose.ui.messages

import androidx.annotation.UiThread
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import io.getstream.chat.android.compose.ui.ComposeTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
internal class MessagesScreenTest : ComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun prepare() {
        whenever(mockClientState.user) doReturn MutableStateFlow(randomUser())
        whenever(mockClientState.connectionState) doReturn MutableStateFlow(ConnectionState.Connected)
    }

    @Test
    @UiThread
    fun `initial state`() {
        composeTestRule.setContent {
            ChatTheme {
                MessagesScreen()
            }
        }

        composeTestRule.onNodeWithText("Channel without name").assertExists()
        composeTestRule.onNodeWithText("0 Members").assertExists()
    }
}

@Composable
private fun MessagesScreen() {
    MessagesScreen(
        viewModelFactory = MessagesViewModelFactory(
            context = ApplicationProvider.getApplicationContext(),
            channelId = randomCID(),
        ),
    )
}
