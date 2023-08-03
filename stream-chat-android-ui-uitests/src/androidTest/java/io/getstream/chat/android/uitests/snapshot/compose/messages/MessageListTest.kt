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

import io.getstream.chat.android.compose.ui.messages.list.MessageList
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessageListState
import io.getstream.chat.android.uitests.snapshot.compose.ComposeScreenshotTest
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Test

class MessageListTest : ComposeScreenshotTest() {

    @Test
    fun messageListWithTheirMessages() = runScreenshotTest {
        MessageList(
            currentState = MessageListState(
                isLoading = false,
                isLoadingNewerMessages = false,
                messageItems = listOf(
                    MessageItemState(
                        message = TestData.message1(),
                        isMine = false,
                        showMessageFooter = true,
                    ),
                    MessageItemState(
                        message = TestData.message2(),
                        isMine = false,
                        showMessageFooter = false,
                    ),
                    MessageItemState(
                        message = TestData.message3(),
                        isMine = false,
                        showMessageFooter = false,
                    ),
                ),
                currentUser = TestData.user1(),
            ),
        )
    }

    @Test
    fun messageListWithMineMessages() = runScreenshotTest {
        MessageList(
            currentState = MessageListState(
                isLoading = false,
                isLoadingNewerMessages = false,
                messageItems = listOf(
                    MessageItemState(
                        message = TestData.message1(),
                        isMine = true,
                        showMessageFooter = true,
                        isMessageRead = true,
                    ),
                    MessageItemState(
                        message = TestData.message2(),
                        isMine = true,
                        showMessageFooter = false,
                        isMessageRead = true,
                    ),
                    MessageItemState(
                        message = TestData.message3(),
                        isMine = true,
                        showMessageFooter = false,
                        isMessageRead = true,
                    ),
                ),
                currentUser = TestData.user1(),
            ),
        )
    }
}
