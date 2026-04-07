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
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import io.getstream.chat.android.models.Command

internal class CommandAdapter : JsonAdapter<Command>() {
    override fun fromJson(reader: JsonReader): Command? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

        reader.beginObject()
        var name: String? = null
        var description: String? = null
        var args: String? = null
        var set: String? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "name" -> name = reader.nextString()
                "description" -> description = reader.nextString()
                "args" -> args = reader.nextString()
                "set" -> set = reader.nextString()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        if (name == null) {
            throw JsonDataException("Required value 'name' missing at ${reader.path}")
        }
        if (description == null) {
            throw JsonDataException("Required value 'description' missing at ${reader.path}")
        }
        if (args == null) {
            throw JsonDataException("Required value 'args' missing at ${reader.path}")
        }
        if (set == null) {
            throw JsonDataException("Required value 'set_' (JSON name 'set') missing at ${reader.path}")
        }

        return Command(
            name = name,
            description = description,
            args = args,
            set = set,
        )
    }

    override fun toJson(p0: JsonWriter, p1: Command?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
