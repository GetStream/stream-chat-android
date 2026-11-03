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

package io.getstream.chat.android.ai.a2ui.core

/**
 * JSON as plain Kotlin values: [Map] with [String] keys, [List], [String], [Long] or [Double],
 * [Boolean] and `null`. Numbers without a fraction or an exponent read as [Long].
 *
 * A2UI arrives while the agent writes it, so the parser is strict and small: it reads one
 * complete JSON text, and [A2UIStream] finds the complete messages in a reply first.
 */
@Suppress("TooManyFunctions")
internal object A2UIJson {

    /** The deepest a parsed value may nest, past which the text is rejected rather than read. */
    private const val MAX_PARSE_DEPTH = 512
    private const val UNICODE_ESCAPE_DIGITS = 4
    private const val HEX = 16

    // Whole numbers below this print without a fraction; larger ones may not be exact.
    private const val MAX_EXACT_WHOLE = 1e15
    private val WHITESPACE = setOf(' ', '\n', '\r', '\t')

    /** Parses one JSON text. */
    fun parse(text: String): Any? = Parser(text).parseDocument()

    /** Parses one JSON text, or returns `null` when it isn't JSON. */
    fun parseOrNull(text: String): Any? = try {
        parse(text)
    } catch (_: A2UIJsonException) {
        null
    }

    /** Writes [value] as compact JSON. Keys keep their order unless [sortKeys]. */
    fun write(
        value: Any?,
        sortKeys: Boolean = false,
    ): String = StringBuilder().also { write(value, it, sortKeys) }.toString()

    /** [value] as an object with string keys, or `null`. */
    @Suppress("UNCHECKED_CAST")
    fun map(value: Any?): Map<String, Any?>? =
        (value as? Map<*, *>)?.takeIf { map -> map.keys.all { it is String } } as Map<String, Any?>?

    /** [value] as an array, or `null`. */
    fun list(value: Any?): List<Any?>? = value as? List<Any?>

    /** Whether [value] is a JSON number. */
    fun isNumber(value: Any?): Boolean = value is Number && value !is Boolean

    /** [value] as text the way A2UI interpolates it: numbers without a needless `.0`. */
    fun stringify(value: Any?): String = when (value) {
        null -> ""
        is String -> value
        is Boolean -> value.toString()
        is Number -> numberText(value)
        else -> write(value)
    }

    /** [value] coerced to text, or `null` when it is absent. */
    fun string(value: Any?): String? = value?.let(::stringify)

    /** [value] coerced to a finite number: numbers, and strings that hold one. */
    fun double(value: Any?): Double? {
        val number = when (value) {
            is Boolean -> null
            is Number -> value.toDouble()
            is String -> value.trim().toDoubleOrNull()
            else -> null
        }
        return number?.takeIf { it.isFinite() }
    }

    /** [value] coerced to a whole number, rounding toward zero. */
    fun long(value: Any?): Long? = double(value)?.let { number ->
        val truncated = if (number < 0) kotlin.math.ceil(number) else kotlin.math.floor(number)
        truncated.takeIf { it >= Long.MIN_VALUE.toDouble() && it <= Long.MAX_VALUE.toDouble() }?.toLong()
    }

    /** [value] coerced to an [Int], or `null` when it doesn't fit. */
    fun int(value: Any?): Int? = long(value)?.takeIf { it in Int.MIN_VALUE..Int.MAX_VALUE }?.toInt()

    /** [value] coerced to a boolean: booleans, numbers (non-zero) and `"true"`/`"false"`. */
    fun bool(value: Any?): Boolean? = when (value) {
        is Boolean -> value
        is Number -> value.toDouble() != 0.0
        is String -> when (value.lowercase()) {
            "true" -> true
            "false" -> false
            else -> null
        }
        else -> null
    }

    /** [value] as a list of strings, coercing each item. */
    fun stringList(value: Any?): List<String>? = list(value)?.mapNotNull(::string)

    /** Whether two JSON values are equal, with `1` equal to `1.0`. */
    fun isEqual(left: Any?, right: Any?): Boolean = when {
        left == null || right == null -> left == null && right == null
        isNumber(left) && isNumber(right) -> (left as Number).toDouble() == (right as Number).toDouble()
        left is Map<*, *> && right is Map<*, *> ->
            left.size == right.size && left.all { (key, value) -> right.containsKey(key) && isEqual(value, right[key]) }
        left is List<*> && right is List<*> ->
            left.size == right.size && left.indices.all { isEqual(left[it], right[it]) }
        else -> left == right
    }

    /** Whether [value] nests deeper than [limit] objects and arrays. */
    fun exceedsDepth(value: Any?, limit: Int): Boolean {
        val children = when (value) {
            is Map<*, *> -> value.values
            is List<*> -> value
            else -> return false
        }
        if (limit <= 0) return true
        return children.any { exceedsDepth(it, limit - 1) }
    }

    /** [value] with every number as a [Long] or a [Double], and every map keyed by strings. */
    fun normalize(value: Any?): Any? = when (value) {
        null, is String, is Boolean, is Long -> value
        is Int -> value.toLong()
        is Short -> value.toLong()
        is Byte -> value.toLong()
        is Double -> value
        is Number -> value.toDouble()
        is Map<*, *> -> LinkedHashMap<String, Any?>().also { map ->
            value.forEach { (key, item) -> if (key is String) map[key] = normalize(item) }
        }
        is List<*> -> value.map(::normalize)
        else -> value.toString()
    }

    private fun numberText(value: Number): String {
        if (value !is Double && value !is Float) return value.toString()
        val double = value.toDouble()
        if (!double.isFinite()) return "null"
        val isWhole = double == kotlin.math.floor(double) && kotlin.math.abs(double) < MAX_EXACT_WHOLE
        if (isWhole) return double.toLong().toString()
        return double.toString()
    }

    private fun write(value: Any?, out: StringBuilder, sortKeys: Boolean) {
        when (value) {
            null -> out.append("null")
            is String -> quote(value, out)
            is Boolean -> out.append(value)
            is Number -> out.append(numberText(value))
            is Map<*, *> -> {
                out.append('{')
                val entries = if (sortKeys) value.entries.sortedBy { it.key.toString() } else value.entries
                entries.forEachIndexed { index, (key, item) ->
                    if (index > 0) out.append(',')
                    quote(key.toString(), out)
                    out.append(':')
                    write(item, out, sortKeys)
                }
                out.append('}')
            }
            is List<*> -> {
                out.append('[')
                value.forEachIndexed { index, item ->
                    if (index > 0) out.append(',')
                    write(item, out, sortKeys)
                }
                out.append(']')
            }
            else -> quote(value.toString(), out)
        }
    }

    private fun quote(text: String, out: StringBuilder) {
        out.append('"')
        for (char in text) {
            when (char) {
                '"' -> out.append("\\\"")
                '\\' -> out.append("\\\\")
                '\n' -> out.append("\\n")
                '\r' -> out.append("\\r")
                '\t' -> out.append("\\t")
                '\b' -> out.append("\\b")
                '\u000C' -> out.append("\\f")
                else -> if (char < ' ') {
                    out.append("\\u").append(char.code.toString(HEX).padStart(UNICODE_ESCAPE_DIGITS, '0'))
                } else {
                    out.append(char)
                }
            }
        }
        out.append('"')
    }

    private class Parser(private val text: String) {
        private var position = 0

        fun parseDocument(): Any? {
            skipWhitespace()
            val value = parseValue(depth = 0)
            skipWhitespace()
            if (position != text.length) fail("Unexpected text after the JSON value")
            return value
        }

        private fun parseValue(depth: Int): Any? {
            if (depth > MAX_PARSE_DEPTH) fail("The JSON nests too deeply")
            skipWhitespace()
            if (position >= text.length) fail("Unexpected end of JSON")
            return when (val char = text[position]) {
                '{' -> parseObject(depth)
                '[' -> parseArray(depth)
                '"' -> parseString()
                't' -> literal("true", true)
                'f' -> literal("false", false)
                'n' -> literal("null", null)
                else -> if (char == '-' || char.isDigit()) parseNumber() else fail("Unexpected character '$char'")
            }
        }

        private fun parseObject(depth: Int): Map<String, Any?> {
            position++
            val map = LinkedHashMap<String, Any?>()
            skipWhitespace()
            if (peek() == '}') {
                position++
                return map
            }
            while (true) {
                skipWhitespace()
                if (peek() != '"') fail("Expected a key")
                val key = parseString()
                skipWhitespace()
                expect(':')
                map[key] = parseValue(depth + 1)
                skipWhitespace()
                when (peek()) {
                    ',' -> position++
                    '}' -> {
                        position++
                        return map
                    }
                    else -> fail("Expected ',' or '}'")
                }
            }
        }

        private fun parseArray(depth: Int): List<Any?> {
            position++
            val list = ArrayList<Any?>()
            skipWhitespace()
            if (peek() == ']') {
                position++
                return list
            }
            while (true) {
                list.add(parseValue(depth + 1))
                skipWhitespace()
                when (peek()) {
                    ',' -> position++
                    ']' -> {
                        position++
                        return list
                    }
                    else -> fail("Expected ',' or ']'")
                }
            }
        }

        @Suppress("NestedBlockDepth")
        private fun parseString(): String {
            position++
            val out = StringBuilder()
            while (true) {
                if (position >= text.length) fail("Unterminated string")
                when (val char = text[position++]) {
                    '"' -> return out.toString()
                    '\\' -> {
                        if (position >= text.length) fail("Unterminated string")
                        when (val escaped = text[position++]) {
                            '"', '\\', '/' -> out.append(escaped)
                            'b' -> out.append('\b')
                            'f' -> out.append('\u000C')
                            'n' -> out.append('\n')
                            'r' -> out.append('\r')
                            't' -> out.append('\t')
                            'u' -> {
                                if (position + UNICODE_ESCAPE_DIGITS > text.length) fail("Invalid unicode escape")
                                val digits = text.substring(position, position + UNICODE_ESCAPE_DIGITS)
                                val code = digits.toIntOrNull(HEX) ?: fail("Invalid unicode escape")
                                out.append(code.toChar())
                                position += UNICODE_ESCAPE_DIGITS
                            }
                            else -> fail("Invalid escape '\\$escaped'")
                        }
                    }
                    else -> if (char < ' ') fail("Control character in string") else out.append(char)
                }
            }
        }

        private fun parseNumber(): Any {
            val start = position
            if (peek() == '-') position++
            digits()
            var isInteger = true
            if (peek() == '.') {
                isInteger = false
                position++
                digits()
            }
            if (peek() == 'e' || peek() == 'E') {
                isInteger = false
                position++
                if (peek() == '+' || peek() == '-') position++
                digits()
            }
            val literal = text.substring(start, position)
            if (isInteger) literal.toLongOrNull()?.let { return it }
            val number = literal.toDoubleOrNull() ?: fail("Invalid number '$literal'")
            return number
        }

        private fun digits() {
            val start = position
            while (position < text.length && text[position].isDigit()) position++
            if (position == start) fail("Expected a digit")
        }

        private fun literal(word: String, value: Any?): Any? {
            if (!text.startsWith(word, position)) fail("Unexpected token")
            position += word.length
            return value
        }

        private fun expect(char: Char) {
            if (peek() != char) fail("Expected '$char'")
            position++
        }

        private fun peek(): Char = if (position < text.length) text[position] else '\u0000'

        private fun skipWhitespace() {
            while (position < text.length && text[position] in WHITESPACE) {
                position++
            }
        }

        private fun fail(message: String): Nothing = throw A2UIJsonException("$message at $position")
    }
}

/** Text that isn't JSON. */
internal class A2UIJsonException(message: String) : Exception(message)
