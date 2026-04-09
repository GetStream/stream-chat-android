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
import io.getstream.chat.android.models.Location
import java.util.Date

internal class LocationAdapter(
    private val dateAdapter: JsonAdapter<Date>,
) : JsonAdapter<Location>() {
    override fun fromJson(reader: JsonReader): Location? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

        reader.beginObject()
        var channelCid: String? = null
        var messageId: String? = null
        var userId: String? = null
        var latitude: Double? = null
        var longitude: Double? = null
        var createdByDeviceId: String? = null
        var endAt: Date? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "channel_cid" -> channelCid = reader.nextString()
                "message_id" -> messageId = reader.nextString()
                "user_id" -> userId = reader.nextString()
                "latitude" -> latitude = reader.nextDouble()
                "longitude" -> longitude = reader.nextDouble()
                "created_by_device_id" -> createdByDeviceId = reader.nextString()
                "end_at" -> endAt = dateAdapter.fromJson(reader)
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        JsonParsingUtils.requireField(channelCid, "channel_cid", reader)
        JsonParsingUtils.requireField(messageId, "message_id", reader)
        JsonParsingUtils.requireField(userId, "user_id", reader)
        JsonParsingUtils.requireField(latitude, "latitude", reader)
        JsonParsingUtils.requireField(longitude, "longitude", reader)
        JsonParsingUtils.requireField(createdByDeviceId, "created_by_device_id", reader)

        return Location(
            cid = channelCid,
            messageId = messageId,
            userId = userId,
            latitude = latitude,
            longitude = longitude,
            deviceId = createdByDeviceId,
            endAt = endAt,
        )
    }

    override fun toJson(p0: JsonWriter, p1: Location?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
