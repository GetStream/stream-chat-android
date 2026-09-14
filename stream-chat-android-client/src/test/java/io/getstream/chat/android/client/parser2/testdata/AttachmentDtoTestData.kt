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

package io.getstream.chat.android.client.parser2.testdata

import io.getstream.chat.android.network.models.Attachment
import org.intellij.lang.annotations.Language
import io.getstream.chat.android.models.Attachment as DomainAttachment

internal object AttachmentDtoTestData {

    @Language("JSON")
    val json =
        """{
          "asset_url": "assetUrl",
          "author_name": "authorName",
          "author_link": "authorLink",
          "fallback": "fallback",
          "file_size": 1,
          "image": "image",
          "image_url": "imageUrl",
          "mime_type": "mimeType",
          "name": "name",
          "og_scrape_url": "ogScrapeUrl",
          "text": "text",
          "thumb_url": "thumbUrl",
          "title": "title",
          "title_link": "titleLink",
          "type": "type",
          "original_height": 100,
          "original_width": 100,
          "draft": true
        }
        """.withoutWhitespace()
    val generatedAttachment = Attachment(
        assetUrl = "assetUrl",
        authorName = "authorName",
        authorLink = "authorLink",
        fallback = "fallback",
        imageUrl = "imageUrl",
        ogScrapeUrl = "ogScrapeUrl",
        text = "text",
        thumbUrl = "thumbUrl",
        title = "title",
        titleLink = "titleLink",
        type = "type",
        originalHeight = 100,
        originalWidth = 100,
        // file_size, image, mime_type and name are undeclared by the spec, so they arrive here.
        // An undeclared number is parsed untyped, which makes it a Double.
        custom = mapOf(
            "draft" to true,
            "file_size" to 1.0,
            "image" to "image",
            "mime_type" to "mimeType",
            "name" to "name",
        ),
    )

    /** The domain attachment that the wire fixture above represents. */
    val domainAttachment = DomainAttachment(
        assetUrl = "assetUrl",
        authorName = "authorName",
        authorLink = "authorLink",
        fallback = "fallback",
        fileSize = 1,
        image = "image",
        imageUrl = "imageUrl",
        mimeType = "mimeType",
        name = "name",
        ogUrl = "ogScrapeUrl",
        text = "text",
        thumbUrl = "thumbUrl",
        title = "title",
        titleLink = "titleLink",
        type = "type",
        originalHeight = 100,
        originalWidth = 100,
        extraData = mutableMapOf("draft" to true),
    )

    @Language("JSON")
    val jsonWithNullFileSize =
        """{
          "file_size": null
        }
        """.withoutWhitespace()

    @Language("JSON")
    val jsonWithoutExtraData =
        """{
          "file_size": 0
        }
        """.withoutWhitespace()
}
