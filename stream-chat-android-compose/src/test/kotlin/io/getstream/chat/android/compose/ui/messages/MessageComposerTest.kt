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

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import org.junit.Rule
import org.junit.Test

internal class MessageComposerTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun `empty input`() {
        snapshotWithDarkMode {
            MessageComposer(
                messageComposerState = MessageComposerState(
                    ownCapabilities = ChannelCapabilities.toSet(),
                ),
                onSendMessage = { _, _ -> },
            )
        }
    }

    @Test
    fun `text input`() {
        snapshotWithDarkMode {
            MessageComposer(
                messageComposerState = MessageComposerState(
                    inputValue = "Message text",
                    ownCapabilities = ChannelCapabilities.toSet(),
                ),
                onSendMessage = { _, _ -> },
            )
        }
    }
}
