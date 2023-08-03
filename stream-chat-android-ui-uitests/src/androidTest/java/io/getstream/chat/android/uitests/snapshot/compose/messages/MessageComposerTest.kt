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

import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.uitests.snapshot.compose.ComposeScreenshotTest
import org.junit.Test

@OptIn(InternalStreamChatApi::class)
class MessageComposerTest : ComposeScreenshotTest() {

    @Test
    fun messageComposerWithEmptyInput() = runScreenshotTest {
        MessageComposer(
            messageComposerState = MessageComposerState(
                ownCapabilities = ChannelCapabilities.toSet(),
            ),
            onSendMessage = { _, _ -> },
        )
    }

    @Test
    fun messageComposerWithTextInput() = runScreenshotTest {
        MessageComposer(
            messageComposerState = MessageComposerState(
                inputValue = "Message text",
                ownCapabilities = ChannelCapabilities.toSet(),
            ),
            onSendMessage = { _, _ -> },
        )
    }
}
