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

package io.getstream.chat.android.offline.repository.database.converter

import io.getstream.chat.android.offline.repository.database.converter.internal.ModerationDetailsConverter
import io.getstream.chat.android.offline.repository.domain.message.internal.ModerationDetailsEntity
import org.amshove.kluent.shouldBeEqualTo
import org.intellij.lang.annotations.Language
import org.junit.Test

internal class ModerationDetailsConverterTest {
    @Test
    fun testNullEncoding() {
        val converter = ModerationDetailsConverter()
        converter.moderationDetailsToString(null) shouldBeEqualTo null
    }

    @Test
    fun testEncoding() {
        val converter = ModerationDetailsConverter()
        val details = ModerationDetailsEntity(
            originalText = "test",
            action = "bounce",
            errorMsg = "message",
        )

        @Language("JSON")
        val encoded = """
            {"originalText":"test","action":"bounce","errorMsg":"message"}
        """.trimIndent()
        converter.moderationDetailsToString(details) shouldBeEqualTo encoded
    }

    @Test
    fun testDecoding() {
        val converter = ModerationDetailsConverter()

        @Language("JSON")
        val encoded = """
            {"originalText":"test","action":"bounce","errorMsg":"message"}
        """.trimIndent()
        val decoded = converter.stringToModerationDetails(encoded)
        decoded shouldBeEqualTo ModerationDetailsEntity(
            originalText = "test",
            action = "bounce",
            errorMsg = "message",
        )
    }

    @Test
    fun testEncodingEmptyErrorMsg() {
        val converter = ModerationDetailsConverter()
        val details = ModerationDetailsEntity(
            originalText = "test",
            action = "bounce",
            errorMsg = "",
        )

        @Language("JSON")
        val encoded = """
            {"originalText":"test","action":"bounce","errorMsg":""}
        """.trimIndent()
        converter.moderationDetailsToString(details) shouldBeEqualTo encoded
    }

    @Test
    fun testDecodingEmptyErrorMsg() {
        val converter = ModerationDetailsConverter()

        @Language("JSON")
        val encoded = """
            {"originalText":"test","action":"bounce","errorMsg":""}
        """.trimIndent()
        val decoded = converter.stringToModerationDetails(encoded)
        decoded shouldBeEqualTo ModerationDetailsEntity(
            originalText = "test",
            action = "bounce",
            errorMsg = "",
        )
    }
}
