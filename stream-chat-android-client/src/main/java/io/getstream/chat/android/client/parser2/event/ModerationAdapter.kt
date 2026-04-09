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
import io.getstream.chat.android.models.Moderation
import io.getstream.chat.android.models.ModerationAction

internal class ModerationAdapter : JsonAdapter<Moderation>() {
    override fun fromJson(reader: JsonReader): Moderation? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

        reader.beginObject()
        var action: String? = null
        var originalText: String? = null
        var textHarms: List<String>? = null
        var imageHarms: List<String>? = null
        var blocklistMatched: String? = null
        var semanticFilterMatched: String? = null
        var platformCircumvented: Boolean? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "action" -> action = reader.nextString()
                "original_text" -> originalText = reader.nextString()
                "text_harms" -> textHarms = JsonParsingUtils.parseStringList(reader)
                "image_harms" -> imageHarms = JsonParsingUtils.parseStringList(reader)
                "blocklist_matched" -> blocklistMatched = JsonParsingUtils.readNullableString(reader)
                "semantic_filter_matched" -> semanticFilterMatched = JsonParsingUtils.readNullableString(reader)
                "platform_circumvented" -> platformCircumvented = JsonParsingUtils.readNullableBoolean(reader)
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        JsonParsingUtils.requireField(action, "action", reader)
        JsonParsingUtils.requireField(originalText, "original_text", reader)

        return Moderation(
            action = ModerationAction.fromValue(action),
            originalText = originalText,
            textHarms = textHarms.orEmpty(),
            imageHarms = imageHarms.orEmpty(),
            blocklistMatched = blocklistMatched,
            semanticFilterMatched = semanticFilterMatched,
            platformCircumvented = platformCircumvented ?: false,
        )
    }

    override fun toJson(p0: JsonWriter, p1: Moderation?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
