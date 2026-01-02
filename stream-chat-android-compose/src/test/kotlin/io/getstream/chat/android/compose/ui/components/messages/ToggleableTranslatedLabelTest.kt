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

package io.getstream.chat.android.compose.ui.components.messages

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.feature.messages.translations.MessageOriginalTranslationsStore
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class ToggleableTranslatedLabelTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    private val cid = "cid"
    private lateinit var translationsStore: MessageOriginalTranslationsStore

    @Before
    fun setUpStore() {
        translationsStore = MessageOriginalTranslationsStore.forChannel(cid)
    }

    @After
    fun tearDownStore() {
        translationsStore.clear()
    }

    @Test
    fun `show translation label when showOriginalText is true`() {
        val messageId = "message-id"
        translationsStore.showOriginalText(messageId)
        val message = Message(
            id = messageId,
            cid = cid,
            text = "Original text",
            i18n = mapOf("en" to "Translated text"),
        )
        val messageItem = MessageItemState(
            message = message,
            showOriginalText = true,
            ownCapabilities = emptySet(),
        )
        snapshotWithDarkModeRow {
            ToggleableTranslatedLabel(
                messageItem = messageItem,
                translatedTo = "English",
                onToggleOriginalText = {},
            )
        }
    }

    @Test
    fun `show original label and translated label when showOriginalText is false`() {
        val messageId = "message-id"
        translationsStore.hideOriginalText(messageId)
        val message = Message(
            id = messageId,
            cid = cid,
            text = "Original text",
            i18n = mapOf("en" to "Translated text"),
        )
        val messageItem = MessageItemState(
            message = message,
            showOriginalText = false,
            ownCapabilities = emptySet(),
        )
        snapshotWithDarkModeRow {
            ToggleableTranslatedLabel(
                messageItem = messageItem,
                translatedTo = "English",
                onToggleOriginalText = {},
            )
        }
    }
}
