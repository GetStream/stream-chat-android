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

import org.intellij.lang.annotations.Language
import io.getstream.chat.android.network.models.Attachment as AttachmentDto

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
          "actions": [],
          "fields": [],
          "draft": true
        }
        """.withoutWhitespace()
    val attachment = AttachmentDto(
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
        actions = emptyList(),
        fields = emptyList(),
        custom = mapOf(
            "file_size" to 1.0,
            "image" to "image",
            "mime_type" to "mimeType",
            "name" to "name",
            "draft" to true,
        ),
    )

    @Language("JSON")
    val jsonWithNullFileSize =
        """{
          "actions": [],
          "fields": [],
          "file_size": null
        }
        """.withoutWhitespace()
    val attachmentWithNullFileSize = AttachmentDto(
        actions = emptyList(),
        fields = emptyList(),
        custom = mapOf("file_size" to null),
    )

    @Language("JSON")
    val jsonWithoutExtraData =
        """{
          "actions": [],
          "fields": [],
          "file_size": 0
        }
        """.withoutWhitespace()
    val attachmentWithoutExtraData = AttachmentDto(
        actions = emptyList(),
        fields = emptyList(),
        custom = mapOf("file_size" to 0.0),
    )
}
