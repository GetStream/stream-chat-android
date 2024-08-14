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

package io.getstream.chat.android.uitests.snapshot.compose.messages

import io.getstream.chat.android.compose.ui.messages.header.MessageListHeader
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.uitests.snapshot.compose.ComposeScreenshotTest
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Test

class MessageListHeaderTest : ComposeScreenshotTest() {

    @Test
    fun messageListHeaderForConnectedState() = runScreenshotTest {
        MessageListHeader(
            channel = TestData.channel1().copy(
                members = listOf(
                    TestData.member1(),
                    TestData.member2(),
                    TestData.member3(),
                ),
                memberCount = 3,
            ),
            currentUser = TestData.user1(),
            connectionState = ConnectionState.Connected,
        )
    }

    @Test
    fun messageListHeaderForThreadMode() = runScreenshotTest {
        MessageListHeader(
            channel = TestData.channel1().copy(
                members = listOf(
                    TestData.member1(),
                    TestData.member2(),
                    TestData.member3(),
                ),
            ),
            currentUser = TestData.user1(),
            connectionState = ConnectionState.Connected,
            messageMode = MessageMode.MessageThread(TestData.message1(), null),
        )
    }

    @Test
    fun messageListHeaderForOfflineState() = runScreenshotTest {
        MessageListHeader(
            channel = TestData.channel1().copy(
                members = listOf(
                    TestData.member1(),
                    TestData.member2(),
                    TestData.member3(),
                ),
            ),
            currentUser = TestData.user1(),
            connectionState = ConnectionState.Offline,
            messageMode = MessageMode.Normal,
        )
    }
}
