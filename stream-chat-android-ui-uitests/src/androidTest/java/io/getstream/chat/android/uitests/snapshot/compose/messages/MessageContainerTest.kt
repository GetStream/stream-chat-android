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

import io.getstream.chat.android.compose.ui.messages.list.MessageContainer
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.list.DateSeparatorItemState
import io.getstream.chat.android.ui.common.state.messages.list.SystemMessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.ThreadDateSeparatorItemState
import io.getstream.chat.android.uitests.snapshot.compose.ComposeScreenshotTest
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Test
import java.util.Date

class MessageContainerTest : ComposeScreenshotTest() {

    @Test
    fun dateSeparator() = runScreenshotTest {
        MessageContainer(DateSeparatorItemState(TestData.date1()))
    }

    @Test
    fun threadSeparator() = runScreenshotTest {
        MessageContainer(ThreadDateSeparatorItemState(date = Date(), replyCount = 5))
    }

    @Test
    fun systemMessage() = runScreenshotTest {
        MessageContainer(SystemMessageItemState(Message(text = "System message")))
    }
}
