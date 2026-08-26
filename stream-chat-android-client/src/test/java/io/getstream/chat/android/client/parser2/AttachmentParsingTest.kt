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
import org.amshove.kluent.shouldBeEqualTo
import org.intellij.lang.annotations.Language
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

    // region Generated path (JSON → network Attachment)

    @Language("JSON")
    private val giphyAttachment =
        """{
          "type": "giphy",
          "title": "cat",
          "thumb_url": "https://giphy.com/thumb.gif",
          "file_size": 2048,
          "image": "https://giphy.com/i.gif",
          "mime_type": "image/gif",
          "name": "cat.gif",
          "color": "#ff0000",
          "pretext": "look",
          "actions": [{ "name": "send", "text": "Send", "style": "primary", "type": "button", "value": "send" }],
          "giphy": {
            "original": { "url": "https://giphy.com/original.gif", "width": "480", "height": "270", "size": "1024", "frames": "12" },
            "fixed_height": { "url": "https://giphy.com/fixed_height.gif", "width": "480", "height": "270", "size": "1024", "frames": "" },
            "fixed_height_downsampled": { "url": "https://giphy.com/fixed_height_downsampled.gif", "width": "480", "height": "270", "size": "1024", "frames": "" },
            "fixed_height_still": { "url": "https://giphy.com/fixed_height_still.gif", "width": "480", "height": "270", "size": "1024", "frames": "" },
            "fixed_width": { "url": "https://giphy.com/fixed_width.gif", "width": "480", "height": "270", "size": "1024", "frames": "" },
            "fixed_width_downsampled": { "url": "https://giphy.com/fixed_width_downsampled.gif", "width": "480", "height": "270", "size": "1024", "frames": "" },
            "fixed_width_still": { "url": "https://giphy.com/fixed_width_still.gif", "width": "480", "height": "270", "size": "1024", "frames": "" }
          },
          "sentinel": "keep-me"
        }"""

    @Test
    fun `Keys the spec does not declare are collected into custom`() {
        val attachment = parser.fromJson(giphyAttachment, io.getstream.chat.android.network.models.Attachment::class.java)

        // An undeclared number arrives untyped, so it is a Double rather than an Int.
        attachment.custom["file_size"] shouldBeEqualTo 2048.0
        attachment.custom["image"] shouldBeEqualTo "https://giphy.com/i.gif"
        attachment.custom["mime_type"] shouldBeEqualTo "image/gif"
        attachment.custom["name"] shouldBeEqualTo "cat.gif"
        attachment.custom["sentinel"] shouldBeEqualTo "keep-me"
    }

    @Test
    fun `Keys the hand-written DTO did not declare stay in custom as well`() {
        val attachment = parser.fromJson(giphyAttachment, io.getstream.chat.android.network.models.Attachment::class.java)

        // Declared on the generated model, so without the keep set they would leave the map.
        attachment.custom["color"] shouldBeEqualTo "#ff0000"
        attachment.custom["pretext"] shouldBeEqualTo "look"
        (attachment.custom["actions"] as List<*>).size shouldBeEqualTo 1
        // Still parsed into their own fields too.
        attachment.color shouldBeEqualTo "#ff0000"
        attachment.actions?.size shouldBeEqualTo 1
    }

    @Test
    fun `The giphy object is kept in the exact shape giphyInfo reads`() {
        val attachment = parser.fromJson(giphyAttachment, io.getstream.chat.android.network.models.Attachment::class.java)

        @Suppress("UNCHECKED_CAST")
        val giphy = attachment.custom["giphy"] as Map<String, Map<String, String>>
        giphy["original"]?.get("url") shouldBeEqualTo "https://giphy.com/original.gif"
        giphy["original"]?.get("width") shouldBeEqualTo "480"
        giphy["fixed_height"]?.get("url") shouldBeEqualTo "https://giphy.com/fixed_height.gif"
        giphy.keys.size shouldBeEqualTo 7
        // The typed field parses as well; the map is what the UI reads.
        attachment.giphy?.original?.url shouldBeEqualTo "https://giphy.com/original.gif"
    }
    // endregion
}
