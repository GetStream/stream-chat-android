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
import io.getstream.chat.android.client.parser2.testdata.AttachmentTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import io.getstream.chat.android.network.models.Attachment as NetworkAttachment

internal class AttachmentParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    @Test
    fun `Response path - deserializes with optional fields missing`() {
        val dto = parser.fromJson(AttachmentTestData.jsonOptionalFieldsMissing, NetworkAttachment::class.java)
        val attachment = with(domainMapping) { dto.toDomain() }
        assertEquals(AttachmentTestData.expectedOptionalFieldsMissing, attachment)
    }

    // endregion

    // region Explicit null values ({"asset_url": null, ...})

    @Test
    fun `Response path - deserializes with explicit null values`() {
        val dto = parser.fromJson(AttachmentTestData.jsonWithExplicitNulls, NetworkAttachment::class.java)
        val attachment = with(domainMapping) { dto.toDomain() }
        assertEquals(AttachmentTestData.expectedWithExplicitNulls, attachment)
    }

    // endregion

    // region file_size: null (defaults to 0)

    @Test
    fun `Response path - defaults to 0 on file_size null`() {
        val dto = parser.fromJson(AttachmentTestData.jsonWithFileSizeNull, NetworkAttachment::class.java)
        val attachment = with(domainMapping) { dto.toDomain() }
        assertEquals(0, attachment.fileSize)
    }

    // endregion

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

    // endregion
}
