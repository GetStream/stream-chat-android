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
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Utility functions for parsing JSON with consistent null-handling.
 */
internal object JsonParsingUtils {

    /**
     * Throws [JsonDataException] if [value] is null. Enables smart-cast via contract.
     * The error format matches Moshi codegen's `Util.missingProperty` when invoked through `fromJsonValue`,
     * ensuring parity between the DTO and direct parsing paths.
     */
    @OptIn(ExperimentalContracts::class)
    fun <T> requireField(value: T?, fieldName: String, reader: JsonReader): T {
        contract { returns() implies (value != null) }
        return value ?: throw JsonDataException(
            "Required value '$fieldName' missing at ${reader.path}",
        )
    }

    /** Reads a nullable Int (returns null if JSON value is null). */
    fun readNullableInt(reader: JsonReader): Int? {
        return if (reader.peek() == JsonReader.Token.NULL) reader.nextNull() else reader.nextInt()
    }

    /** Reads a nullable String (returns null if JSON value is null). */
    fun readNullableString(reader: JsonReader): String? {
        return if (reader.peek() == JsonReader.Token.NULL) reader.nextNull() else reader.nextString()
    }

    /** Reads a nullable Boolean (returns null if JSON value is null). */
    fun readNullableBoolean(reader: JsonReader): Boolean? {
        return if (reader.peek() == JsonReader.Token.NULL) reader.nextNull() else reader.nextBoolean()
    }

    /** Reads a nullable Long (returns null if JSON value is null). */
    fun readNullableLong(reader: JsonReader): Long? {
        return if (reader.peek() == JsonReader.Token.NULL) reader.nextNull() else reader.nextLong()
    }

    /** Accumulates a key-value pair into an extra data map, lazily creating it if needed. */
    fun accumulateExtraData(
        key: String,
        reader: JsonReader,
        extraData: MutableMap<String, Any>?,
    ): MutableMap<String, Any>? {
        val value = reader.readJsonValue() ?: return extraData
        val map = extraData ?: mutableMapOf()
        map[key] = value
        return map
    }

    /**
     * Parses a JSON array of strings.
     *
     * @param reader The JsonReader positioned at the array field.
     * @return A list of strings, or null if the JSON value is null, missing, or not an array.
     */
    fun parseStringList(reader: JsonReader): List<String>? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
        if (reader.peek() != JsonReader.Token.BEGIN_ARRAY) {
            reader.skipValue()
            return null
        }
        reader.beginArray()
        val list = mutableListOf<String>()
        while (reader.hasNext()) {
            list.add(reader.nextString())
        }
        reader.endArray()
        return list
    }

    /**
     * Parses a JSON array of objects using the provided adapter.
     *
     * @param reader The JsonReader positioned at the array field.
     * @param adapter The JsonAdapter to parse individual array elements.
     * @return A list of parsed objects, or null if the JSON value is null, missing, or not an array.
     */
    fun <T> parseList(reader: JsonReader, adapter: JsonAdapter<T>): List<T>? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
        if (reader.peek() != JsonReader.Token.BEGIN_ARRAY) {
            reader.skipValue()
            return null
        }
        reader.beginArray()
        val list = mutableListOf<T>()
        while (reader.hasNext()) {
            adapter.fromJson(reader)?.let { list.add(it) }
        }
        reader.endArray()
        return list
    }

    /**
     * Parses a JSON object into a Map<String, String>.
     *
     * @param reader The JsonReader positioned at the object field.
     * @return A map of string key-value pairs, or null if the JSON value is null, missing, or not an object.
     */
    fun parseStringMap(reader: JsonReader): Map<String, String>? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
        if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) {
            reader.skipValue()
            return null
        }
        reader.beginObject()
        val map = mutableMapOf<String, String>()
        while (reader.hasNext()) {
            val key = reader.nextName()
            map[key] = reader.nextString()
        }
        reader.endObject()
        return map
    }

    /**
     * Parses a JSON object into a Map<String, Int>.
     *
     * @param reader The JsonReader positioned at the object field.
     * @return A map of string keys to integer values, or null if the JSON value is null, missing, or not an object.
     */
    fun parseIntMap(reader: JsonReader): Map<String, Int>? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
        if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) {
            reader.skipValue()
            return null
        }
        reader.beginObject()
        val map = mutableMapOf<String, Int>()
        while (reader.hasNext()) {
            val key = reader.nextName()
            map[key] = reader.nextInt()
        }
        reader.endObject()
        return map
    }
}
