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
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.models.GetOGResponse
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test

internal class GetOGResponseParsingTest {
    private val parser = ParserFactory.createMoshiChatParser()
    private val mapping = DomainMapping(
        currentUserIdProvider = { "me" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    @Test
    fun `Custom root fields are collected into extraData`() {
        val response = parser.fromJson(
            """
            {
              "duration": "1ms",
              "type": "image",
              "title": "A page",
              "og_scrape_url": "https://example.com",
              "image_url": "https://example.com/i.png",
              "thumb_url": "https://example.com/i.png",
              "sentinel": "keep-me"
            }
            """.trimIndent(),
            GetOGResponse::class.java,
        )

        val attachment = with(mapping) { response.toDomain() }

        attachment.type shouldBeEqualTo "image"
        attachment.title shouldBeEqualTo "A page"
        attachment.ogUrl shouldBeEqualTo "https://example.com"
        attachment.imageUrl shouldBeEqualTo "https://example.com/i.png"
        attachment.thumbUrl shouldBeEqualTo "https://example.com/i.png"
        attachment.extraData shouldBeEqualTo mapOf("sentinel" to "keep-me")
    }

    @Test
    fun `An empty scrape maps to an attachment with domain defaults`() {
        val response = parser.fromJson("""{"duration":"1ms"}""", GetOGResponse::class.java)

        val attachment = with(mapping) { response.toDomain() }

        attachment.type.shouldBeNull()
        attachment.ogUrl.shouldBeNull()
        attachment.fileSize shouldBeEqualTo 0
        attachment.extraData shouldBeEqualTo emptyMap()
    }

    // The scraper does not appear to populate site_name/site for any page tried on the wire, so these two
    // are covered here instead. Same-named on both sides, which is exactly how a swap goes unnoticed.
    @Test
    fun `Author name and link are mapped from their own fields`() {
        val response = parser.fromJson(
            """
            {
              "duration": "1ms",
              "author_name": "Example Site",
              "author_link": "https://example.com",
              "asset_url": "https://example.com/v.mp4",
              "type": "video"
            }
            """.trimIndent(),
            GetOGResponse::class.java,
        )

        val attachment = with(mapping) { response.toDomain() }

        attachment.authorName shouldBeEqualTo "Example Site"
        attachment.authorLink shouldBeEqualTo "https://example.com"
        attachment.assetUrl shouldBeEqualTo "https://example.com/v.mp4"
        attachment.type shouldBeEqualTo "video"
    }

    // The wire was not observed sending these on /og, but three sampled pages cannot prove it never
    // will, and losing them would be silent. They arrive in `custom` because the spec omits them.
    @Test
    fun `Undeclared root fields are mapped and removed from extraData`() {
        val response = parser.fromJson(
            """
            {
              "duration": "1ms",
              "type": "file",
              "og_scrape_url": "https://example.com/doc.pdf",
              "file_size": 2048,
              "image": "https://example.com/legacy.png",
              "mime_type": "application/pdf",
              "name": "doc.pdf",
              "fallback": "a fallback",
              "original_width": 640,
              "original_height": 480,
              "sentinel": "keep-me"
            }
            """.trimIndent(),
            GetOGResponse::class.java,
        )

        val attachment = with(mapping) { response.toDomain() }

        attachment.fileSize shouldBeEqualTo 2048
        attachment.image shouldBeEqualTo "https://example.com/legacy.png"
        attachment.mimeType shouldBeEqualTo "application/pdf"
        attachment.name shouldBeEqualTo "doc.pdf"
        attachment.fallback shouldBeEqualTo "a fallback"
        attachment.originalWidth shouldBeEqualTo 640
        attachment.originalHeight shouldBeEqualTo 480
        // Read out of `custom`, so they must not also linger under their wire names.
        attachment.extraData shouldBeEqualTo mapOf("sentinel" to "keep-me")
    }
}
