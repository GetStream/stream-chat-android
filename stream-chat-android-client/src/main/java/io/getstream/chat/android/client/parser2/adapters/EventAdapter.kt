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

package io.getstream.chat.android.client.parser2.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.rawType
import io.getstream.chat.android.client.api2.model.dto.ChatEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectedEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectionErrorEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationMessageNewEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationThreadMessageNewEventDto
import io.getstream.chat.android.models.EventType
import java.lang.reflect.Type

internal class EventAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        return when (type.rawType) {
            ChatEventDto::class.java -> EventDtoAdapter(moshi)
            else -> null
        }
    }
}

internal class EventDtoAdapter(
    private val moshi: Moshi,
) : JsonAdapter<ChatEventDto>() {

    private val mapAdapter: JsonAdapter<MutableMap<String, Any?>> =
        moshi.adapter(Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java))

    private val connectedEventAdapter = moshi.adapter(ConnectedEventDto::class.java)
    private val connectionErrorEventAdapter = moshi.adapter(ConnectionErrorEventDto::class.java)
    private val notificationMessageNewEventAdapter = moshi.adapter(NotificationMessageNewEventDto::class.java)
    private val notificationThreadMessageNewEventAdapter =
        moshi.adapter(NotificationThreadMessageNewEventDto::class.java)

    @Suppress("LongMethod", "ComplexMethod", "ReturnCount")
    override fun fromJson(reader: JsonReader): ChatEventDto? {
        if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull<Nothing?>()
            return null
        }

        val map: Map<String, Any?> = mapAdapter.fromJson(reader)!!.filterValues { it != null }

        val adapter = when (val type = map["type"] as? String) {
            EventType.HEALTH_CHECK -> when {
                map.containsKey("me") -> connectedEventAdapter
                else -> return null
            }
            EventType.CONNECTION_ERROR -> connectionErrorEventAdapter
            EventType.NOTIFICATION_MESSAGE_NEW -> notificationMessageNewEventAdapter
            EventType.NOTIFICATION_THREAD_MESSAGE_NEW -> notificationThreadMessageNewEventAdapter
            else -> return null
        }

        return adapter.fromJsonValue(map)
    }

    override fun toJson(writer: JsonWriter, value: ChatEventDto?) {
        error("Can't convert this event to Json $value")
    }
}
