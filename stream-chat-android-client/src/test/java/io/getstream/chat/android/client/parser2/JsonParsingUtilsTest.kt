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

package io.getstream.chat.android.client.parser2

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import io.getstream.chat.android.client.parser2.direct.JsonParsingUtils
import okio.Buffer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class JsonParsingUtilsTest {

    // region requireField
    // Note: requireField does NOT read from JSON — it validates an already-parsed value.
    // The reader is only used for reader.path in the error message.

    @Test
    fun `requireField returns value when non-null`() {
        val reader = readerFor("{}")
        reader.beginObject()
        reader.endObject()
        // Value was already parsed elsewhere; requireField just validates and smart-casts
        val result = JsonParsingUtils.requireField("hello", "name", reader)
        assertEquals("hello", result)
    }

    @Test
    fun `requireField throws JsonDataException when null`() {
        val reader = readerFor("{}")
        reader.beginObject()
        reader.endObject()
        val exception = assertThrows<JsonDataException> {
            JsonParsingUtils.requireField(null, "id", reader)
        }
        assertEquals("Required value 'id' missing at \$", exception.message)
    }

    // endregion

    // region readNullableString

    @Test
    fun `readNullableString reads string value from object field`() {
        val reader = readerFor("""{"name":"hello"}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.readNullableString(reader)
        assertEquals("hello", result)
    }

    @Test
    fun `readNullableString returns null for explicit null in object field`() {
        val reader = readerFor("""{"name":null}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.readNullableString(reader)
        assertNull(result)
    }

    // endregion

    // region readNullableInt

    @Test
    fun `readNullableInt reads int value from object field`() {
        val reader = readerFor("""{"count":42}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.readNullableInt(reader)
        assertEquals(42, result)
    }

    @Test
    fun `readNullableInt returns null for explicit null in object field`() {
        val reader = readerFor("""{"count":null}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.readNullableInt(reader)
        assertNull(result)
    }

    // endregion

    // region readNullableBoolean

    @Test
    fun `readNullableBoolean reads true from object field`() {
        val reader = readerFor("""{"flag":true}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.readNullableBoolean(reader)
        assertEquals(true, result)
    }

    @Test
    fun `readNullableBoolean reads false from object field`() {
        val reader = readerFor("""{"flag":false}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.readNullableBoolean(reader)
        assertEquals(false, result)
    }

    @Test
    fun `readNullableBoolean returns null for explicit null in object field`() {
        val reader = readerFor("""{"flag":null}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.readNullableBoolean(reader)
        assertNull(result)
    }

    // endregion

    // region readNullableLong

    @Test
    fun `readNullableLong reads long value from object field`() {
        val reader = readerFor("""{"ts":9999999999}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.readNullableLong(reader)
        assertEquals(9999999999L, result)
    }

    @Test
    fun `readNullableLong returns null for explicit null in object field`() {
        val reader = readerFor("""{"ts":null}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.readNullableLong(reader)
        assertNull(result)
    }

    // endregion

    // region accumulateExtraData

    @Test
    fun `accumulateExtraData creates map on first call`() {
        val reader = readerFor("""{"key":"value"}""")
        reader.beginObject()
        val key = reader.nextName()
        val result = JsonParsingUtils.accumulateExtraData(key, reader, null)
        assertEquals(mapOf("key" to "value"), result)
    }

    @Test
    fun `accumulateExtraData appends to existing map`() {
        val existing = mutableMapOf<String, Any>("a" to 1.0)
        val reader = readerFor("""{"b":"two"}""")
        reader.beginObject()
        val key = reader.nextName()
        val result = JsonParsingUtils.accumulateExtraData(key, reader, existing)
        assertEquals(mapOf("a" to 1.0, "b" to "two"), result)
    }

    @Test
    fun `accumulateExtraData returns original map when value is null`() {
        val existing = mutableMapOf<String, Any>("a" to 1.0)
        val reader = readerFor("""{"b":null}""")
        reader.beginObject()
        val key = reader.nextName()
        val result = JsonParsingUtils.accumulateExtraData(key, reader, existing)
        // null JSON values are skipped — original map returned unchanged
        assertEquals(existing, result)
    }

    @Test
    fun `accumulateExtraData returns null when value is null and no existing map`() {
        val reader = readerFor("""{"b":null}""")
        reader.beginObject()
        val key = reader.nextName()
        val result = JsonParsingUtils.accumulateExtraData(key, reader, null)
        assertNull(result)
    }

    // endregion

    // region parseStringList

    @Test
    fun `parseStringList parses array of strings`() {
        val reader = readerFor("""{"items":["a","b","c"]}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.parseStringList(reader)
        assertEquals(listOf("a", "b", "c"), result)
    }

    @Test
    fun `parseStringList returns null for JSON null`() {
        val reader = readerFor("""{"items":null}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.parseStringList(reader)
        assertNull(result)
    }

    @Test
    fun `parseStringList returns null for non-array token`() {
        val reader = readerFor("""{"items":"not_an_array"}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.parseStringList(reader)
        assertNull(result)
    }

    @Test
    fun `parseStringList returns empty list for empty array`() {
        val reader = readerFor("""{"items":[]}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.parseStringList(reader)
        assertEquals(emptyList<String>(), result)
    }

    // endregion

    // region parseList

    @Test
    fun `parseList parses array of objects`() {
        val adapter = object : JsonAdapter<String>() {
            override fun fromJson(reader: JsonReader): String = reader.nextString()
            override fun toJson(writer: JsonWriter, value: String?) = error("unused")
        }
        val reader = readerFor("""{"items":["x","y"]}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.parseList(reader, adapter)
        assertEquals(listOf("x", "y"), result)
    }

    @Test
    fun `parseList returns null for JSON null`() {
        val adapter = object : JsonAdapter<String>() {
            override fun fromJson(reader: JsonReader): String = reader.nextString()
            override fun toJson(writer: JsonWriter, value: String?) = error("unused")
        }
        val reader = readerFor("""{"items":null}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.parseList(reader, adapter)
        assertNull(result)
    }

    @Test
    fun `parseList skips null elements`() {
        val adapter = object : JsonAdapter<String>() {
            override fun fromJson(reader: JsonReader): String? {
                return if (reader.peek() == JsonReader.Token.NULL) reader.nextNull() else reader.nextString()
            }
            override fun toJson(writer: JsonWriter, value: String?) = error("unused")
        }
        val reader = readerFor("""{"items":["a",null,"b"]}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.parseList(reader, adapter)
        assertEquals(listOf("a", "b"), result)
    }

    @Test
    fun `parseList returns null for non-array token`() {
        val adapter = object : JsonAdapter<String>() {
            override fun fromJson(reader: JsonReader): String = reader.nextString()
            override fun toJson(writer: JsonWriter, value: String?) = error("unused")
        }
        val reader = readerFor("""{"items":42}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.parseList(reader, adapter)
        assertNull(result)
    }

    // endregion

    // region parseStringMap

    @Test
    fun `parseStringMap parses object`() {
        val reader = readerFor("""{"map":{"a":"1","b":"2"}}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.parseStringMap(reader)
        assertEquals(mapOf("a" to "1", "b" to "2"), result)
    }

    @Test
    fun `parseStringMap returns null for JSON null`() {
        val reader = readerFor("""{"map":null}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.parseStringMap(reader)
        assertNull(result)
    }

    @Test
    fun `parseStringMap returns null for non-object token`() {
        val reader = readerFor("""{"map":[1,2]}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.parseStringMap(reader)
        assertNull(result)
    }

    @Test
    fun `parseStringMap returns empty map for empty object`() {
        val reader = readerFor("""{"map":{}}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.parseStringMap(reader)
        assertEquals(emptyMap<String, String>(), result)
    }

    // endregion

    // region parseIntMap

    @Test
    fun `parseIntMap parses object`() {
        val reader = readerFor("""{"map":{"a":1,"b":2}}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.parseIntMap(reader)
        assertEquals(mapOf("a" to 1, "b" to 2), result)
    }

    @Test
    fun `parseIntMap returns null for JSON null`() {
        val reader = readerFor("""{"map":null}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.parseIntMap(reader)
        assertNull(result)
    }

    @Test
    fun `parseIntMap returns null for non-object token`() {
        val reader = readerFor("""{"map":"not_an_object"}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.parseIntMap(reader)
        assertNull(result)
    }

    @Test
    fun `parseIntMap returns empty map for empty object`() {
        val reader = readerFor("""{"map":{}}""")
        reader.beginObject()
        reader.nextName()
        val result = JsonParsingUtils.parseIntMap(reader)
        assertEquals(emptyMap<String, Int>(), result)
    }

    // endregion

    private fun readerFor(json: String): JsonReader =
        JsonReader.of(Buffer().writeUtf8(json))
}
