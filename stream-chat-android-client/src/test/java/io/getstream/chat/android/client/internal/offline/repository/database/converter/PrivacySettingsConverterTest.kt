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

package io.getstream.chat.android.client.internal.offline.repository.database.converter

import io.getstream.chat.android.client.internal.offline.repository.database.converter.internal.PrivacySettingsConverter
import io.getstream.chat.android.client.internal.offline.repository.domain.user.internal.DeliveryReceiptsEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.user.internal.PrivacySettingsEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.user.internal.ReadReceiptsEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.user.internal.TypingIndicatorsEntity
import org.intellij.lang.annotations.Language
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull

internal class PrivacySettingsConverterTest {
    @Test
    fun testNullEncoding() {
        val converter = PrivacySettingsConverter()
        assertNull(converter.privacySettingsToString(null))
    }

    @Test
    fun testEncoding() {
        val converter = PrivacySettingsConverter()
        val settings = PrivacySettingsEntity(
            typingIndicators = TypingIndicatorsEntity(
                enabled = false,
            ),
            readReceipts = ReadReceiptsEntity(
                enabled = false,
            ),
            deliveryReceipts = DeliveryReceiptsEntity(
                enabled = false,
            ),
        )

        @Language("JSON")
        val expected = """
            {"typingIndicators":{"enabled":false},"readReceipts":{"enabled":false},"deliveryReceipts":{"enabled":false}}
        """.trimIndent()
        assertEquals(expected, converter.privacySettingsToString(settings))
    }

    @Test
    fun testDecoding() {
        val converter = PrivacySettingsConverter()

        @Language("JSON")
        val encoded = """
            {"typingIndicators":{"enabled":false},"readReceipts":{"enabled":false},"deliveryReceipts":{"enabled":false}}
        """.trimIndent()
        val actual = converter.stringToPrivacySettings(encoded)

        val expected = PrivacySettingsEntity(
            typingIndicators = TypingIndicatorsEntity(
                enabled = false,
            ),
            readReceipts = ReadReceiptsEntity(
                enabled = false,
            ),
            deliveryReceipts = DeliveryReceiptsEntity(
                enabled = false,
            ),
        )
        assertEquals(expected, actual)
    }
}
