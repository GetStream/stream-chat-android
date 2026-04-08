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
import io.getstream.chat.android.models.Option

internal class OptionAdapter : JsonAdapter<Option>() {
    override fun fromJson(reader: JsonReader): Option? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

        reader.beginObject()

        var id: String? = null
        var text: String? = null
        var extraData: MutableMap<String, Any>? = null

        while (reader.hasNext()) {
            val key = reader.nextName()
            when (key) {
                "id" -> id = reader.nextString()
                "text" -> text = reader.nextString()
                else -> extraData = JsonParsingUtils.accumulateExtraData(key, reader, extraData)
            }
        }
        reader.endObject()

        JsonParsingUtils.requireField(id, "id", reader)
        JsonParsingUtils.requireField(text, "text", reader)

        return Option(
            id = id,
            text = text,
            extraData = extraData?.toMap() ?: emptyMap(),
        )
    }

    override fun toJson(p0: JsonWriter, p1: Option?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
