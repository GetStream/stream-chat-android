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

package io.getstream.chat.android.client.parser2

import com.squareup.moshi.JsonDataException
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.model.dto.PrivacySettingsDto
import io.getstream.chat.android.client.parser2.event.PrivacySettingsAdapter
import io.getstream.chat.android.client.parser2.testdata.PrivacySettingsTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PrivacySettingsParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private val privacySettingsAdapter = PrivacySettingsAdapter()

    // region DTO path (JSON → PrivacySettingsDto → PrivacySettings)

    @Test
    fun `DTO path - deserializes all fields`() {
        val dto = parser.fromJson(PrivacySettingsTestData.jsonAllFields, PrivacySettingsDto::class.java)
        val privacySettings = with(domainMapping) { dto.toDomain() }
        assertEquals(PrivacySettingsTestData.expectedAllFields, privacySettings)
    }

    @Test
    fun `DTO path - deserializes with optional fields missing`() {
        val dto = parser.fromJson(PrivacySettingsTestData.jsonOptionalFieldsMissing, PrivacySettingsDto::class.java)
        val privacySettings = with(domainMapping) { dto.toDomain() }
        assertEquals(PrivacySettingsTestData.expectedOptionalFieldsMissing, privacySettings)
    }

    // endregion

    // region Direct path (JSON → PrivacySettings via PrivacySettingsAdapter)

    @Test
    fun `Direct path - deserializes all fields`() {
        val privacySettings = privacySettingsAdapter.fromJson(PrivacySettingsTestData.jsonAllFields)
        assertEquals(PrivacySettingsTestData.expectedAllFields, privacySettings)
    }

    @Test
    fun `Direct path - deserializes with optional fields missing`() {
        val privacySettings = privacySettingsAdapter.fromJson(PrivacySettingsTestData.jsonOptionalFieldsMissing)
        assertEquals(PrivacySettingsTestData.expectedOptionalFieldsMissing, privacySettings)
    }

    // endregion

    // region Error message parity (nested mandatory fields)

    @Test
    fun `Both paths - same error on typing_indicators missing enabled`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(PrivacySettingsTestData.jsonTypingIndicatorsMissingEnabled, PrivacySettingsDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            privacySettingsAdapter.fromJson(PrivacySettingsTestData.jsonTypingIndicatorsMissingEnabled)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error on delivery_receipts missing enabled`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(PrivacySettingsTestData.jsonDeliveryReceiptsMissingEnabled, PrivacySettingsDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            privacySettingsAdapter.fromJson(PrivacySettingsTestData.jsonDeliveryReceiptsMissingEnabled)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error on read_receipts missing enabled`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(PrivacySettingsTestData.jsonReadReceiptsMissingEnabled, PrivacySettingsDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            privacySettingsAdapter.fromJson(PrivacySettingsTestData.jsonReadReceiptsMissingEnabled)
        }
        assertEquals(dtoException.message, directException.message)
    }

    // endregion
}
