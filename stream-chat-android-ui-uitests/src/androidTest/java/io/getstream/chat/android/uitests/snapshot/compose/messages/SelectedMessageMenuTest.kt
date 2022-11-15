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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.ui.components.messageoptions.defaultMessageOptionsState
import io.getstream.chat.android.compose.ui.components.selectedmessage.SelectedMessageMenu
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.uitests.snapshot.compose.ComposeScreenshotTest
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Test

@OptIn(InternalStreamChatApi::class)
class SelectedMessageMenuTest : ComposeScreenshotTest() {

    @Test
    fun selectedMessageMenuForMineMessage() {
        selectedMessageMenu(TestData.user1(), TestData.message1())
    }

    @Test
    fun selectedMessageMenuForTheirMessage() {
        selectedMessageMenu(TestData.user1(), TestData.message2())
    }

    private fun selectedMessageMenu(currentUser: User, selectedMessage: Message) = runScreenshotTest {
        val channelCapabilities = ChannelCapabilities.toSet()
            .filterNot { it == ChannelCapabilities.DELETE_ANY_MESSAGE }
            .filterNot { it == ChannelCapabilities.UPDATE_ANY_MESSAGE }
            .toSet()

        val messageOptionsStateList = defaultMessageOptionsState(
            selectedMessage = selectedMessage,
            currentUser = currentUser,
            isInThread = false,
            ownCapabilities = channelCapabilities
        )

        Box(modifier = Modifier.fillMaxSize()) {
            SelectedMessageMenu(
                modifier = Modifier.align(Alignment.BottomCenter),
                message = selectedMessage,
                messageOptions = messageOptionsStateList,
                onMessageAction = {},
                onShowMoreReactionsSelected = {},
                ownCapabilities = channelCapabilities
            )
        }
    }
}
