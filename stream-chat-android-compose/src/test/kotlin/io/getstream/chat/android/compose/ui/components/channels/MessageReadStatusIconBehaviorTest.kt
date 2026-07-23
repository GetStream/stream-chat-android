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

package io.getstream.chat.android.compose.ui.components.channels

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.test.MockedChatClientTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomMessage
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
internal class MessageReadStatusIconBehaviorTest : MockedChatClientTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun prepare() {
        whenever(mockClientState.user) doReturn MutableStateFlow(randomUser())
        whenever(mockClientState.connectionState) doReturn MutableStateFlow(ConnectionState.Connected)
    }

    @Test
    fun `sent icon is shown when read events are enabled`() {
        setIconContent(readEventsEnabled = true)

        composeTestRule
            .onNodeWithTag("Stream_MessageReadStatus_isSent", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun `sent icon is hidden when read events are disabled`() {
        setIconContent(readEventsEnabled = false)

        composeTestRule
            .onNodeWithTag("Stream_MessageReadStatus_isSent", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun `read icon is hidden when read events are disabled`() {
        setIconContent(isMessageRead = true, readEventsEnabled = false)

        composeTestRule
            .onNodeWithTag("Stream_MessageReadStatus_isRead", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun `delivered icon is hidden when delivery events are disabled`() {
        setIconContent(isMessageDelivered = true, deliveryEventsEnabled = false)

        composeTestRule
            .onNodeWithTag("Stream_MessageReadStatus_isDelivered", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun `pending icon is shown when read events are disabled`() {
        setIconContent(syncStatus = SyncStatus.IN_PROGRESS, readEventsEnabled = false)

        composeTestRule
            .onNodeWithTag("Stream_MessageReadStatus_isPending", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun `error icon is shown when read events are disabled`() {
        setIconContent(syncStatus = SyncStatus.FAILED_PERMANENTLY, readEventsEnabled = false)

        composeTestRule
            .onNodeWithTag("Stream_MessageReadStatus_isError", useUnmergedTree = true)
            .assertExists()
    }

    private fun setIconContent(
        syncStatus: SyncStatus = SyncStatus.COMPLETED,
        isMessageRead: Boolean = false,
        isMessageDelivered: Boolean = false,
        readEventsEnabled: Boolean = true,
        deliveryEventsEnabled: Boolean = true,
    ) {
        composeTestRule.setContent {
            ChatTheme {
                MessageReadStatusIcon(
                    message = randomMessage(syncStatus = syncStatus),
                    isMessageRead = isMessageRead,
                    isMessageDelivered = isMessageDelivered,
                    readEventsEnabled = readEventsEnabled,
                    deliveryEventsEnabled = deliveryEventsEnabled,
                )
            }
        }
    }
}
