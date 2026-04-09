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
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.PushProvider

internal class DeviceAdapter : JsonAdapter<Device>() {
    override fun fromJson(reader: JsonReader): Device? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

        reader.beginObject()
        var id: String? = null
        var pushProvider: String? = null
        var pushProviderName: String? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> id = reader.nextString()
                "push_provider" -> pushProvider = reader.nextString()
                "push_provider_name" -> pushProviderName = JsonParsingUtils.readNullableString(reader)
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        JsonParsingUtils.requireField(id, "id", reader)
        JsonParsingUtils.requireField(pushProvider, "push_provider", reader)

        return Device(
            token = id,
            pushProvider = PushProvider.fromKey(pushProvider),
            providerName = pushProviderName,
        )
    }

    override fun toJson(p0: JsonWriter, p1: Device?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
