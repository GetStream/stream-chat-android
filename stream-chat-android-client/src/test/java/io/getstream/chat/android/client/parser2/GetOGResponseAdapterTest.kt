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

import io.getstream.chat.android.network.models.GetOGResponse
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class GetOGResponseAdapterTest {

    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Gathers root-level custom fields into the custom map`() {
        // The OG scrape wire flattens custom ExtraFields at root (file_size/image/mime_type/name
        // aren't declared in the OpenAPI spec). The adapter sweeps them into `custom`.
        val json = """
            {
                "duration": "12ms",
                "type": "image",
                "title": "Example",
                "og_scrape_url": "https://example.com",
                "file_size": 1234,
                "image": "https://example.com/i.png",
                "mime_type": "image/png",
                "name": "i.png",
                "foo": "bar"
            }
        """.trimIndent()

        val response = parser.fromJson(json, GetOGResponse::class.java)

        response.duration shouldBeEqualTo "12ms"
        response.type shouldBeEqualTo "image"
        response.title shouldBeEqualTo "Example"
        response.ogScrapeUrl shouldBeEqualTo "https://example.com"
        response.custom["file_size"] shouldBeEqualTo 1234.0
        response.custom["image"] shouldBeEqualTo "https://example.com/i.png"
        response.custom["mime_type"] shouldBeEqualTo "image/png"
        response.custom["name"] shouldBeEqualTo "i.png"
        response.custom["foo"] shouldBeEqualTo "bar"
    }

    @Test
    fun `Leaves custom empty when the wire has no extra fields`() {
        val json = """
            {
                "duration": "5ms",
                "type": "video",
                "title": "No extras"
            }
        """.trimIndent()

        val response = parser.fromJson(json, GetOGResponse::class.java)

        response.custom shouldBeEqualTo emptyMap()
    }
}
