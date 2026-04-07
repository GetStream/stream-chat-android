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
import io.getstream.chat.android.DeliveryReceipts
import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.ReadReceipts
import io.getstream.chat.android.TypingIndicators

internal class PrivacySettingsAdapter : JsonAdapter<PrivacySettings>() {
    override fun fromJson(reader: JsonReader): PrivacySettings? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

        reader.beginObject()
        var typingIndicators: TypingIndicators? = null
        var deliveryReceipts: DeliveryReceipts? = null
        var readReceipts: ReadReceipts? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "typing_indicators" -> typingIndicators = parseTypingIndicators(reader)
                "delivery_receipts" -> deliveryReceipts = parseDeliveryReceipts(reader)
                "read_receipts" -> readReceipts = parseReadReceipts(reader)
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return PrivacySettings(
            typingIndicators = typingIndicators,
            deliveryReceipts = deliveryReceipts,
            readReceipts = readReceipts,
        )
    }

    private fun parseTypingIndicators(reader: JsonReader): TypingIndicators? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
        reader.beginObject()
        var enabled: Boolean? = null
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "enabled" -> enabled = reader.nextBoolean()
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        if (enabled == null) {
            throw JsonDataException("Required value 'enabled' missing at ${reader.path}")
        }
        return TypingIndicators(enabled = enabled)
    }

    private fun parseDeliveryReceipts(reader: JsonReader): DeliveryReceipts? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
        reader.beginObject()
        var enabled: Boolean? = null
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "enabled" -> enabled = reader.nextBoolean()
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        if (enabled == null) {
            throw JsonDataException("Required value 'enabled' missing at ${reader.path}")
        }
        return DeliveryReceipts(enabled = enabled)
    }

    private fun parseReadReceipts(reader: JsonReader): ReadReceipts? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
        reader.beginObject()
        var enabled: Boolean? = null
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "enabled" -> enabled = reader.nextBoolean()
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        if (enabled == null) {
            throw JsonDataException("Required value 'enabled' missing at ${reader.path}")
        }
        return ReadReceipts(enabled = enabled)
    }

    override fun toJson(p0: JsonWriter, p1: PrivacySettings?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
