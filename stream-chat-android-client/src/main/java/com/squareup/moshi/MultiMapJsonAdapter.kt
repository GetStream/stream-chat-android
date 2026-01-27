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

package com.squareup.moshi

import com.squareup.moshi.JsonAdapter.Factory
import io.getstream.log.StreamLog
import java.io.IOException
import java.lang.reflect.Type

/**
 * Converts maps with string keys to JSON objects.
 *
 * Custom implementation of [com.squareup.moshi.MapJsonAdapter] which handles duplicate entries.
 */
internal class MultiMapJsonAdapter<K, V>(
    moshi: Moshi,
    keyType: Type,
    valueType: Type,
) : JsonAdapter<Map<K?, V?>>() {

    private val keyAdapter: JsonAdapter<K?> = moshi.adapter(keyType)
    private val valueAdapter: JsonAdapter<V?> = moshi.adapter(valueType)

    @Throws(IOException::class)
    override fun toJson(writer: JsonWriter, map: Map<K?, V?>?) {
        map ?: return
        val keys = hashSetOf<K?>()
        writer.beginObject()
        for ((key, value) in map) {
            if (key == null) {
                throw JsonDataException("Map key is null at ${writer.path}")
            }
            if (keys.contains(key)) {
                val exception = MultiMapJsonDataException(
                    "Map key '$key' has multiple values at path ${writer.path};\nmap: $map",
                )
                StreamLog.e(TAG, exception) { "[toJson] failed: $exception" }
                continue
            }
            writer.promoteValueToName()
            keyAdapter.toJson(writer, key)
            valueAdapter.toJson(writer, value)
            keys.add(key)
        }
        writer.endObject()
    }

    @Throws(IOException::class)
    override fun fromJson(reader: JsonReader): Map<K?, V?> {
        val result = linkedMapOf<K?, V?>()
        reader.beginObject()
        while (reader.hasNext()) {
            reader.promoteNameToValue()
            val key = keyAdapter.fromJson(reader) ?: continue
            val value = valueAdapter.fromJson(reader)
            val replaced = result.put(key, value)
            if (replaced != null) {
                val exception = MultiMapJsonDataException(
                    "Map key '$key' has multiple values at path ${reader.path}: $replaced and $value",
                )
                StreamLog.e(TAG, exception) { "[fromJson] failed: $exception" }
            }
        }
        reader.endObject()
        return result
    }

    override fun toString(): String {
        return "CustomMapJsonAdapter($keyAdapter=$valueAdapter)"
    }

    companion object {
        private const val TAG = "CustomMapJsonAdapter"

        val FACTORY = Factory { type, annotations, moshi ->
            if (annotations.isNotEmpty()) return@Factory null
            val rawType = Types.getRawType(type)
            if (rawType != MutableMap::class.java) return@Factory null
            val keyAndValue = Types.mapKeyAndValueTypes(type, rawType) ?: return@Factory null
            MultiMapJsonAdapter<Any?, Any>(moshi, keyAndValue[0], keyAndValue[1]).nullSafe()
        }
    }
}

/**
 * An exception to highlight multiple key-value entries detection.
 */
private class MultiMapJsonDataException(message: String) : Exception(message)
