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

package io.getstream.chat.android.uitests.snapshot.compose.components

import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.uitests.snapshot.compose.ComposeScreenshotTest
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Test

class DocsOwnCapabilitiesTest : ComposeScreenshotTest() {

    @Test
    fun composerEmptyCapabilities() = runScreenshotTest {
        MessageComposer(
            messageComposerState = MessageComposerState(
                ownCapabilities = emptySet(),
                currentUser = TestData.alex(),
            ),
            onSendMessage = { _, _ -> },
        )
    }

    @Test
    fun composerWithCapabilities() = runScreenshotTest {
        MessageComposer(
            messageComposerState = MessageComposerState(
                ownCapabilities = setOf(
                    ChannelCapabilities.SEND_MESSAGE,
                    ChannelCapabilities.SEND_LINKS,
                    ChannelCapabilities.UPLOAD_FILE,
                    ChannelCapabilities.SEND_REPLY,
                    ChannelCapabilities.TYPING_EVENTS,
                ),
                currentUser = TestData.alex(),
                hasCommands = true,
            ),
            onSendMessage = { _, _ -> },
        )
    }
}
