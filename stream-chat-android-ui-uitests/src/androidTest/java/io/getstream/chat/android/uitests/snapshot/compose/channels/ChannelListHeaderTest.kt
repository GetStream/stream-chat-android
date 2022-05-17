/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.uitests.snapshot.compose.channels

import androidx.compose.ui.test.junit4.createComposeRule
import com.karumi.shot.ScreenshotTest
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.ui.channels.header.ChannelListHeader
import io.getstream.chat.android.offline.model.connection.ConnectionState
import io.getstream.chat.android.uitests.snapshot.compose.TestChatTheme
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Rule
import org.junit.Test

class ChannelListHeaderTest : ScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun channelListHeaderForConnectedState() {
        renderChannelListHeader(
            title = "Stream Chat",
            currentUser = TestData.user1(),
            connectionState = ConnectionState.CONNECTED,
        )
    }

    @Test
    fun channelListHeaderForConnectingState() {
        renderChannelListHeader(
            title = "Stream Chat",
            currentUser = TestData.user1(),
            connectionState = ConnectionState.CONNECTING,
        )
    }

    @Test
    fun channelListHeaderForOfflineState() {
        renderChannelListHeader(
            title = "Stream Chat",
            currentUser = TestData.user1(),
            connectionState = ConnectionState.OFFLINE,
        )
    }

    @Test
    fun channelListHeaderWithoutUser() {
        renderChannelListHeader(
            title = "Stream Chat",
            currentUser = null,
            connectionState = ConnectionState.CONNECTED,
        )
    }

    @Test
    fun channelListHeaderWithoutTitle() {
        renderChannelListHeader(
            title = "",
            currentUser = TestData.user1(),
            connectionState = ConnectionState.CONNECTED,
        )
    }

    private fun renderChannelListHeader(
        title: String,
        currentUser: User?,
        connectionState: ConnectionState,
    ) {
        composeRule.setContent {
            TestChatTheme {
                ChannelListHeader(
                    title = title,
                    currentUser = currentUser,
                    connectionState = connectionState
                )
            }
        }
        compareScreenshot(composeRule)
    }
}
