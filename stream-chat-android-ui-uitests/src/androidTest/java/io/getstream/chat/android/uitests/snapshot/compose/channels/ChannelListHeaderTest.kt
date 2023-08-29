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

import io.getstream.chat.android.compose.ui.channels.header.ChannelListHeader
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.uitests.snapshot.compose.ComposeScreenshotTest
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Test

class ChannelListHeaderTest : ComposeScreenshotTest() {

    @Test
    fun channelListHeaderForConnectedState() = runScreenshotTest {
        ChannelListHeader(
            title = "Stream Chat",
            currentUser = TestData.user1(),
            connectionState = ConnectionState.Connected,
        )
    }

    @Test
    fun channelListHeaderForConnectingState() = runScreenshotTest {
        ChannelListHeader(
            title = "Stream Chat",
            currentUser = TestData.user1(),
            connectionState = ConnectionState.Connecting,
        )
    }

    @Test
    fun channelListHeaderForOfflineState() = runScreenshotTest {
        ChannelListHeader(
            title = "Stream Chat",
            currentUser = TestData.user1(),
            connectionState = ConnectionState.Offline,
        )
    }

    @Test
    fun channelListHeaderWithoutUser() = runScreenshotTest {
        ChannelListHeader(
            title = "Stream Chat",
            currentUser = null,
            connectionState = ConnectionState.Connected,
        )
    }

    @Test
    fun channelListHeaderWithoutTitle() = runScreenshotTest {
        ChannelListHeader(
            title = "",
            currentUser = TestData.user1(),
            connectionState = ConnectionState.Connected,
        )
    }
}
