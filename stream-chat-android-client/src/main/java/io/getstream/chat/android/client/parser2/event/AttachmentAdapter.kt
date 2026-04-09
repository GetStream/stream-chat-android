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

package io.getstream.chat.android.client.parser2.event

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import io.getstream.chat.android.models.Attachment

internal class AttachmentAdapter : JsonAdapter<Attachment>() {

    @Suppress("LongMethod")
    override fun fromJson(reader: JsonReader): Attachment? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

        reader.beginObject()
        var assetUrl: String? = null
        var authorName: String? = null
        var authorLink: String? = null
        var fallback: String? = null
        var fileSize: Int = 0
        var image: String? = null
        var imageUrl: String? = null
        var mimeType: String? = null
        var name: String? = null
        var ogScrapeUrl: String? = null
        var text: String? = null
        var thumbUrl: String? = null
        var title: String? = null
        var titleLink: String? = null
        var type: String? = null
        var originalHeight: Int? = null
        var originalWidth: Int? = null
        var extraData: MutableMap<String, Any>? = null

        while (reader.hasNext()) {
            val key = reader.nextName()
            when (key) {
                "asset_url" -> assetUrl = JsonParsingUtils.readNullableString(reader)
                "author_name" -> authorName = JsonParsingUtils.readNullableString(reader)
                "author_link" -> authorLink = JsonParsingUtils.readNullableString(reader)
                "fallback" -> fallback = JsonParsingUtils.readNullableString(reader)
                "file_size" -> fileSize = reader.nextInt()
                "image" -> image = JsonParsingUtils.readNullableString(reader)
                "image_url" -> imageUrl = JsonParsingUtils.readNullableString(reader)
                "mime_type" -> mimeType = JsonParsingUtils.readNullableString(reader)
                "name" -> name = JsonParsingUtils.readNullableString(reader)
                "og_scrape_url" -> ogScrapeUrl = JsonParsingUtils.readNullableString(reader)
                "text" -> text = JsonParsingUtils.readNullableString(reader)
                "thumb_url" -> thumbUrl = JsonParsingUtils.readNullableString(reader)
                "title" -> title = JsonParsingUtils.readNullableString(reader)
                "title_link" -> titleLink = JsonParsingUtils.readNullableString(reader)
                "type" -> type = JsonParsingUtils.readNullableString(reader)
                "original_height" -> originalHeight = JsonParsingUtils.readNullableInt(reader)
                "original_width" -> originalWidth = JsonParsingUtils.readNullableInt(reader)
                else -> extraData = JsonParsingUtils.accumulateExtraData(key, reader, extraData)
            }
        }
        reader.endObject()

        return Attachment(
            assetUrl = assetUrl,
            authorName = authorName,
            authorLink = authorLink,
            fallback = fallback,
            fileSize = fileSize,
            image = image,
            imageUrl = imageUrl,
            mimeType = mimeType,
            name = name,
            ogUrl = ogScrapeUrl,
            text = text,
            thumbUrl = thumbUrl,
            title = title,
            titleLink = titleLink,
            type = type,
            originalHeight = originalHeight,
            originalWidth = originalWidth,
            extraData = extraData?.toMutableMap() ?: mutableMapOf(),
        )
    }

    override fun toJson(p0: JsonWriter, p1: Attachment?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
