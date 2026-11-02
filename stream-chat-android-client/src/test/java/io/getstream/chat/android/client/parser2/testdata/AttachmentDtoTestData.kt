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

internal object AttachmentDtoTestData {

    @Language("JSON")
    val json =
        """{
          "asset_url": "assetUrl",
          "author_name": "authorName",
          "author_link": "authorLink",
          "fallback": "fallback",
          "image_url": "imageUrl",
          "og_scrape_url": "ogScrapeUrl",
          "text": "text",
          "thumb_url": "thumbUrl",
          "title": "title",
          "title_link": "titleLink",
          "type": "type",
          "original_height": 100,
          "original_width": 100,
          "custom": {
            "draft": true,
            "file_size": 1,
            "image": "image",
            "mime_type": "mimeType",
            "name": "name"
          }
        }
        """.withoutWhitespace()
}
