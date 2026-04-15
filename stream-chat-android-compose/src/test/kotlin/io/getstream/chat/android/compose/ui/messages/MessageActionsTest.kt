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

package io.getstream.chat.android.compose.ui.messages

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.compose.ui.components.messageactions.MessageActionsForFailedMessage
import io.getstream.chat.android.compose.ui.components.messageactions.MessageActionsForIncomingMessage
import io.getstream.chat.android.compose.ui.components.messageactions.MessageActionsForOutgoingMessage
import org.junit.Rule
import org.junit.Test

internal class MessageActionsTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun `my message`() {
        snapshot(isInDarkMode = false) {
            MessageActionsForOutgoingMessage()
        }
    }

    @Test
    fun `my message in dark mode`() {
        snapshot(isInDarkMode = true) {
            MessageActionsForOutgoingMessage()
        }
    }

    @Test
    fun `their message`() {
        snapshot(isInDarkMode = false) {
            MessageActionsForIncomingMessage()
        }
    }

    @Test
    fun `their message in dark mode`() {
        snapshot(isInDarkMode = true) {
            MessageActionsForIncomingMessage()
        }
    }

    @Test
    fun `failed message`() {
        snapshot(isInDarkMode = false) {
            MessageActionsForFailedMessage()
        }
    }

    @Test
    fun `failed message in dark mode`() {
        snapshot(isInDarkMode = true) {
            MessageActionsForFailedMessage()
        }
    }
}
