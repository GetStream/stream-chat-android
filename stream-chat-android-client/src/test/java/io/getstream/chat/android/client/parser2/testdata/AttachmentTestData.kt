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

import io.getstream.chat.android.models.Attachment
import org.intellij.lang.annotations.Language

internal object AttachmentTestData {

    @Language("JSON")
    val jsonAllFields =
        """{"asset_url":"https://example.com/asset.pdf","author_name":"John","author_link":"https://example.com","fallback":"alt text","file_size":1024,"image":"https://example.com/img.png","image_url":"https://example.com/img_url.png","mime_type":"image/png","name":"photo.png","og_scrape_url":"https://example.com/og","text":"description","thumb_url":"https://example.com/thumb.png","title":"My Attachment","title_link":"https://example.com/title","type":"image","original_height":600,"original_width":800,"custom_key":"custom_value"}"""

    @Language("JSON")
    val jsonOptionalFieldsMissing =
        """{}"""

    val expectedAllFields = Attachment(
        assetUrl = "https://example.com/asset.pdf",
        authorName = "John",
        authorLink = "https://example.com",
        fallback = "alt text",
        fileSize = 1024,
        image = "https://example.com/img.png",
        imageUrl = "https://example.com/img_url.png",
        mimeType = "image/png",
        name = "photo.png",
        ogUrl = "https://example.com/og",
        text = "description",
        thumbUrl = "https://example.com/thumb.png",
        title = "My Attachment",
        titleLink = "https://example.com/title",
        type = "image",
        originalHeight = 600,
        originalWidth = 800,
        extraData = mutableMapOf("custom_key" to "custom_value"),
    )

    @Language("JSON")
    val jsonWithExtraDataFieldAndCustomKey =
        """{"type":"image","extraData":{"nested":"val"},"customKey":"hello"}"""

    val expectedWithExtraDataFieldAndCustomKey = Attachment(
        type = "image",
        extraData = mutableMapOf(
            "extraData" to mapOf("nested" to "val"),
            "customKey" to "hello",
        ),
    )

    val expectedOptionalFieldsMissing = Attachment(
        assetUrl = null,
        authorName = null,
        authorLink = null,
        fallback = null,
        fileSize = 0,
        image = null,
        imageUrl = null,
        mimeType = null,
        name = null,
        ogUrl = null,
        text = null,
        thumbUrl = null,
        title = null,
        titleLink = null,
        type = null,
        originalHeight = null,
        originalWidth = null,
        extraData = mutableMapOf(),
    )

    @Language("JSON")
    val jsonWithExplicitNulls =
        """{"asset_url":null,"author_name":null,"author_link":null,"fallback":null,"image":null,"image_url":null,"mime_type":null,"name":null,"og_scrape_url":null,"text":null,"thumb_url":null,"title":null,"title_link":null,"type":null,"original_height":null,"original_width":null}"""

    @Language("JSON")
    val jsonWithFileSizeNull =
        """{"file_size":null}"""

    val expectedWithExplicitNulls = Attachment(
        assetUrl = null,
        authorName = null,
        authorLink = null,
        fallback = null,
        fileSize = 0,
        image = null,
        imageUrl = null,
        mimeType = null,
        name = null,
        ogUrl = null,
        text = null,
        thumbUrl = null,
        title = null,
        titleLink = null,
        type = null,
        originalHeight = null,
        originalWidth = null,
        extraData = mutableMapOf(),
    )
}
