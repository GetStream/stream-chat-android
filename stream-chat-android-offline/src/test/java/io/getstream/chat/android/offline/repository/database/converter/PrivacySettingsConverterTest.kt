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

package io.getstream.chat.android.offline.repository.database.converter

import io.getstream.chat.android.offline.repository.database.converter.internal.PrivacySettingsConverter
import io.getstream.chat.android.offline.repository.domain.user.internal.PrivacySettingsEntity
import io.getstream.chat.android.offline.repository.domain.user.internal.ReadReceiptsEntity
import io.getstream.chat.android.offline.repository.domain.user.internal.TypingIndicatorsEntity
import org.amshove.kluent.shouldBeEqualTo
import org.intellij.lang.annotations.Language
import org.junit.Test

internal class PrivacySettingsConverterTest {
    @Test
    fun testNullEncoding() {
        val converter = PrivacySettingsConverter()
        converter.privacySettingsToString(null) shouldBeEqualTo null
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
        )

        @Language("JSON")
        val encoded = """
            {"typingIndicators":{"enabled":false},"readReceipts":{"enabled":false}}
        """.trimIndent()
        converter.privacySettingsToString(settings) shouldBeEqualTo encoded
    }

    @Test
    fun testDecoding() {
        val converter = PrivacySettingsConverter()

        @Language("JSON")
        val encoded = """
            {"typingIndicators":{"enabled":false},"readReceipts":{"enabled":false}}
        """.trimIndent()
        val decoded = converter.stringToPrivacySettings(encoded)
        decoded shouldBeEqualTo PrivacySettingsEntity(
            typingIndicators = TypingIndicatorsEntity(
                enabled = false,
            ),
            readReceipts = ReadReceiptsEntity(
                enabled = false,
            ),
        )
    }
}
