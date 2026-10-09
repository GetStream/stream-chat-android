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
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.PushProvider

/**
 * Reads an event user's device the way the generated path reads it from custom data: an entry that isn't an
 * object or has no string `id` is dropped (null), and a missing `push_provider` maps to an unknown provider.
 */
internal class DeviceAdapter : JsonAdapter<Device>() {
    override fun fromJson(reader: JsonReader): Device? {
        if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) {
            reader.skipValue()
            return null
        }

        reader.beginObject()
        var id: String? = null
        var pushProvider: String? = null
        var pushProviderName: String? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> id = readStringOrNull(reader)
                "push_provider" -> pushProvider = readStringOrNull(reader)
                "push_provider_name" -> pushProviderName = readStringOrNull(reader)
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return id?.let {
            Device(
                token = it,
                pushProvider = PushProvider.fromKey(pushProvider.orEmpty()),
                providerName = pushProviderName,
            )
        }
    }

    private fun readStringOrNull(reader: JsonReader): String? =
        if (reader.peek() == JsonReader.Token.STRING) {
            reader.nextString()
        } else {
            reader.skipValue()
            null
        }

    override fun toJson(p0: JsonWriter, p1: Device?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
