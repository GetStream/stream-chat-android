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

package io.getstream.chat.android.ai.compose.parts

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

/**
 * Lenient reads from a JSON object: a field of the wrong type reads as missing, and so does an
 * empty string.
 */
internal class Fields(private val values: Map<String, Any?>) {

    fun string(key: String): String? = (values[key] as? String)?.takeIf { it.isNotEmpty() }

    /** A number, read as a whole number. Booleans are not numbers. */
    fun int(key: String): Int? = when (val value = values[key]) {
        is Boolean -> null
        is Number -> value.toInt()
        else -> null
    }

    fun nested(key: String): Fields? = (values[key] as? Map<*, *>)?.let { Fields(it.stringKeyed()) }

    /** An object or an array, as JSON text with sorted keys. Other values read as missing. */
    fun json(key: String): String? = when (val value = values[key]) {
        is Map<*, *>, is List<*> -> Json.write(value)
        else -> null
    }
}

/** JSON reading and writing on `org.json`, with plain Kotlin collections on the outside. */
internal object Json {

    /** Parses a JSON object into a map, or returns an empty map when [text] is not one. */
    fun parseObject(text: String): Map<String, Any?> = try {
        toKotlin(JSONObject(text)) as? Map<String, Any?> ?: emptyMap()
    } catch (_: JSONException) {
        emptyMap()
    }

    /** Writes [value] as JSON, with object keys sorted so equal values write equal text. */
    fun write(value: Any?): String = StringBuilder().also { append(it, value) }.toString()

    private fun toKotlin(value: Any?): Any? = when (value) {
        null, JSONObject.NULL -> null
        is JSONObject -> buildMap {
            val keys = value.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                put(key, toKotlin(value.opt(key)))
            }
        }
        is JSONArray -> List(value.length()) { toKotlin(value.opt(it)) }
        else -> value
    }

    private fun append(builder: StringBuilder, value: Any?) {
        when (value) {
            null, JSONObject.NULL -> builder.append("null")
            is String -> quote(builder, value)
            is Boolean -> builder.append(value)
            is Number -> builder.append(JSONObject.numberToString(value))
            is Map<*, *> -> appendObject(builder, value)
            is List<*> -> appendArray(builder, value)
            is JSONObject, is JSONArray -> append(builder, toKotlin(value))
            else -> quote(builder, value.toString())
        }
    }

    private fun appendObject(builder: StringBuilder, value: Map<*, *>) {
        builder.append('{')
        value.entries
            .map { it.key.toString() to it.value }
            .sortedBy { it.first }
            .forEachIndexed { index, (key, item) ->
                if (index > 0) builder.append(',')
                quote(builder, key)
                builder.append(':')
                append(builder, item)
            }
        builder.append('}')
    }

    private fun appendArray(builder: StringBuilder, value: List<*>) {
        builder.append('[')
        value.forEachIndexed { index, item ->
            if (index > 0) builder.append(',')
            append(builder, item)
        }
        builder.append(']')
    }

    /** Writes [value] as a JSON string. Only what JSON requires is escaped, so `/` stays as is. */
    private fun quote(builder: StringBuilder, value: String) {
        builder.append('"')
        for (char in value) {
            when (char) {
                '"' -> builder.append("\\\"")
                '\\' -> builder.append("\\\\")
                '\n' -> builder.append("\\n")
                '\r' -> builder.append("\\r")
                '\t' -> builder.append("\\t")
                '\b' -> builder.append("\\b")
                '\u000C' -> builder.append("\\f")
                else -> if (char < ' ') {
                    builder.append(String.format(Locale.ROOT, "\\u%04x", char.code))
                } else {
                    builder.append(char)
                }
            }
        }
        builder.append('"')
    }
}

internal fun Map<*, *>.stringKeyed(): Map<String, Any?> = entries.associate { it.key.toString() to it.value }
