/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.components.messages

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.SnapshotTest
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.utils.MessageOriginalTranslationsStore
import org.junit.After
import org.junit.Rule
import org.junit.Test

internal class ToggleableTranslatedLabelTest : SnapshotTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @After
    fun tearDown() {
        MessageOriginalTranslationsStore.clear()
    }

    @Test
    fun `show translation label when showOriginalText is true`() {
        val messageId = "message-id"
        MessageOriginalTranslationsStore.showOriginalText(messageId)
        val message = Message(
            id = messageId,
            text = "Original text",
            i18n = mapOf("en" to "Translated text"),
        )
        snapshotWithDarkModeRow {
            ToggleableTranslatedLabel(
                message = message,
                translatedTo = "en",
            )
        }
    }

    @Test
    fun `show original label and translated label when showOriginalText is false`() {
        val messageId = "message-id"
        MessageOriginalTranslationsStore.hideOriginalText(messageId)
        val message = Message(
            text = "Original text",
            i18n = mapOf("en" to "Translated text"),
        )
        snapshotWithDarkModeRow {
            ToggleableTranslatedLabel(
                message = message,
                translatedTo = "en",
            )
        }
    }
}
