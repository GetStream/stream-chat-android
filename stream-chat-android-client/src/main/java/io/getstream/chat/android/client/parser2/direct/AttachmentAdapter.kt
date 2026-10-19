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

package io.getstream.chat.android.client.parser2.direct

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Types
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.network.infrastructure.Serializer
import io.getstream.chat.android.network.models.Action
import io.getstream.chat.android.network.models.Field
import io.getstream.chat.android.network.models.Images

/**
 * Reads an attachment straight into its domain model: custom data under `custom`, built by [mapping] as the
 * generated path builds it.
 */
internal class AttachmentAdapter(
    private val mapping: DomainMapping,
) : JsonAdapter<Attachment>() {

    // The typed fields apps rarely send: read through their generated models, as the generated path reads them.
    private val actionsAdapter by lazy {
        Serializer.moshi.adapter<List<Action>>(Types.newParameterizedType(List::class.java, Action::class.java))
    }
    private val fieldsAdapter by lazy {
        Serializer.moshi.adapter<List<Field>>(Types.newParameterizedType(List::class.java, Field::class.java))
    }
    private val giphyAdapter by lazy { Serializer.moshi.adapter(Images::class.java) }

    override fun fromJson(reader: JsonReader): Attachment? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
        return read(reader)
    }

    @Suppress("LongMethod", "CyclomaticComplexMethod", "MagicNumber")
    private fun read(reader: JsonReader): Attachment {
        reader.beginObject()
        var assetUrl: String? = null
        var authorName: String? = null
        var authorLink: String? = null
        var fallback: String? = null
        var imageUrl: String? = null
        var ogScrapeUrl: String? = null
        var text: String? = null
        var thumbUrl: String? = null
        var title: String? = null
        var titleLink: String? = null
        var type: String? = null
        var originalHeight: Int? = null
        var originalWidth: Int? = null
        var custom: Map<String, Any?>? = null
        var actions: List<Action>? = null
        var authorIcon: String? = null
        var color: String? = null
        var fields: List<Field>? = null
        var footer: String? = null
        var footerIcon: String? = null
        var giphy: Images? = null
        var pretext: String? = null

        while (reader.hasNext()) {
            when (reader.selectName(KEYS)) {
                0 -> assetUrl = JsonParsingUtils.readNullableString(reader)
                1 -> authorName = JsonParsingUtils.readNullableString(reader)
                2 -> authorLink = JsonParsingUtils.readNullableString(reader)
                3 -> fallback = JsonParsingUtils.readNullableString(reader)
                4 -> imageUrl = JsonParsingUtils.readNullableString(reader)
                5 -> ogScrapeUrl = JsonParsingUtils.readNullableString(reader)
                6 -> text = JsonParsingUtils.readNullableString(reader)
                7 -> thumbUrl = JsonParsingUtils.readNullableString(reader)
                8 -> title = JsonParsingUtils.readNullableString(reader)
                9 -> titleLink = JsonParsingUtils.readNullableString(reader)
                10 -> type = JsonParsingUtils.readNullableString(reader)
                11 -> originalHeight = JsonParsingUtils.readNullableInt(reader)
                12 -> originalWidth = JsonParsingUtils.readNullableInt(reader)
                13 -> if (JsonParsingUtils.expectV2(reader, JsonReader.Token.BEGIN_OBJECT, nullable = false)) {
                    custom = JsonParsingUtils.readJsonObject(reader)
                }
                14 -> actions = actionsAdapter.fromJson(reader)
                15 -> authorIcon = JsonParsingUtils.readNullableString(reader)
                16 -> color = JsonParsingUtils.readNullableString(reader)
                17 -> fields = fieldsAdapter.fromJson(reader)
                18 -> footer = JsonParsingUtils.readNullableString(reader)
                19 -> footerIcon = JsonParsingUtils.readNullableString(reader)
                20 -> giphy = giphyAdapter.fromJson(reader)
                21 -> pretext = JsonParsingUtils.readNullableString(reader)
                else -> {
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()

        return mapping.v2Attachment(
            assetUrl = assetUrl,
            authorName = authorName,
            authorLink = authorLink,
            fallback = fallback,
            imageUrl = imageUrl,
            ogScrapeUrl = ogScrapeUrl,
            text = text,
            thumbUrl = thumbUrl,
            title = title,
            titleLink = titleLink,
            type = type,
            originalHeight = originalHeight,
            originalWidth = originalWidth,
            custom = custom.orEmpty(),
            actions = actions,
            authorIcon = authorIcon,
            color = color,
            fields = fields,
            footer = footer,
            footerIcon = footerIcon,
            giphy = giphy,
            pretext = pretext,
        )
    }

    private companion object {
        /** The keys, matched without allocating a key string, as the generated adapters match them. */
        private val KEYS = JsonReader.Options.of(
            "asset_url",
            "author_name",
            "author_link",
            "fallback",
            "image_url",
            "og_scrape_url",
            "text",
            "thumb_url",
            "title",
            "title_link",
            "type",
            "original_height",
            "original_width",
            "custom",
            "actions",
            "author_icon",
            "color",
            "fields",
            "footer",
            "footer_icon",
            "giphy",
            "pretext",
        )
    }

    override fun toJson(p0: JsonWriter, p1: Attachment?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
