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

import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.model.dto.AttachmentDto
import io.getstream.chat.android.client.parser2.direct.AttachmentAdapter
import io.getstream.chat.android.client.parser2.testdata.AttachmentTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AttachmentParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private val attachmentAdapter = AttachmentAdapter()

    // region DTO path (JSON → AttachmentDto → Attachment)

    @Test
    fun `DTO path - deserializes all fields`() {
        val dto = parser.fromJson(AttachmentTestData.jsonAllFields, AttachmentDto::class.java)
        val attachment = with(domainMapping) { dto.toDomain() }
        assertEquals(AttachmentTestData.expectedAllFields, attachment)
    }

    @Test
    fun `DTO path - deserializes with optional fields missing`() {
        val dto = parser.fromJson(AttachmentTestData.jsonOptionalFieldsMissing, AttachmentDto::class.java)
        val attachment = with(domainMapping) { dto.toDomain() }
        assertEquals(AttachmentTestData.expectedOptionalFieldsMissing, attachment)
    }

    // endregion

    // region Direct path (JSON → Attachment via AttachmentAdapter)

    @Test
    fun `Direct path - deserializes all fields`() {
        val attachment = attachmentAdapter.fromJson(AttachmentTestData.jsonAllFields)
        assertEquals(AttachmentTestData.expectedAllFields, attachment)
    }

    @Test
    fun `Direct path - deserializes with optional fields missing`() {
        val attachment = attachmentAdapter.fromJson(AttachmentTestData.jsonOptionalFieldsMissing)
        assertEquals(AttachmentTestData.expectedOptionalFieldsMissing, attachment)
    }

    // endregion

    // region Explicit null values ({"asset_url": null, ...})

    @Test
    fun `DTO path - deserializes with explicit null values`() {
        val dto = parser.fromJson(AttachmentTestData.jsonWithExplicitNulls, AttachmentDto::class.java)
        val attachment = with(domainMapping) { dto.toDomain() }
        assertEquals(AttachmentTestData.expectedWithExplicitNulls, attachment)
    }

    @Test
    fun `Direct path - deserializes with explicit null values`() {
        val attachment = attachmentAdapter.fromJson(AttachmentTestData.jsonWithExplicitNulls)
        assertEquals(AttachmentTestData.expectedWithExplicitNulls, attachment)
    }

    // endregion

    // region file_size: null (DTO accepts null since #6462 — Direct path mirrors it by defaulting to 0)

    @Test
    fun `DTO path - defaults to 0 on file_size null`() {
        val dto = parser.fromJson(AttachmentTestData.jsonWithFileSizeNull, AttachmentDto::class.java)
        val attachment = with(domainMapping) { dto.toDomain() }
        assertEquals(0, attachment.fileSize)
    }

    @Test
    fun `Direct path - defaults to 0 on file_size null`() {
        val attachment = attachmentAdapter.fromJson(AttachmentTestData.jsonWithFileSizeNull)
        assertEquals(0, attachment?.fileSize)
    }

    // endregion

    // region extraData edge case (literal "extraData" JSON field + custom keys)

    @Test
    fun `DTO path - extraData JSON field and custom keys both land in extraData`() {
        val dto = parser.fromJson(AttachmentTestData.jsonWithExtraDataFieldAndCustomKey, AttachmentDto::class.java)
        val attachment = with(domainMapping) { dto.toDomain() }
        assertEquals(AttachmentTestData.expectedWithExtraDataFieldAndCustomKey, attachment)
    }

    @Test
    fun `Direct path - extraData JSON field and custom keys both land in extraData`() {
        val attachment = attachmentAdapter.fromJson(AttachmentTestData.jsonWithExtraDataFieldAndCustomKey)
        assertEquals(AttachmentTestData.expectedWithExtraDataFieldAndCustomKey, attachment)
    }

    // endregion
}
