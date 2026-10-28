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
    val jsonOptionalFieldsMissing =
        """{}"""

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
        """{"custom":{"file_size":null}}"""

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
