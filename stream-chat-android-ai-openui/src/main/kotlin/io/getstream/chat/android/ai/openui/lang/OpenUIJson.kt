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

package io.getstream.chat.android.ai.openui.lang

/** Thrown when text is not valid JSON. */
internal class OpenUIJsonException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** JSON reading and writing for [OpenUIValue]s (`JSON.parse` / `JSON.stringify`). */
internal object OpenUIJson {

    /**
     * Parses JSON text with `JSON.parse` semantics: object key order is preserved, the last
     * duplicate key wins, and numbers are doubles. Throws [OpenUIJsonException] for invalid input.
     */
    fun parse(text: String): OpenUIValue = JsonReader(text).readDocument()

    /** Parses JSON text, returning `null` when it is invalid. */
    fun parseOrNull(text: String): OpenUIValue? = try {
        parse(text)
    } catch (_: OpenUIJsonException) {
        null
    }

    /** `JSON.stringify(value)`. */
    fun stringify(value: OpenUIValue): String = JS.jsonStringify(value)!!
}

/** A single-pass recursive-descent reader for strict JSON (RFC 8259, as `JSON.parse` accepts it). */
private class JsonReader(private val text: String) {

    private var pos = 0
    private var depth = 0

    fun readDocument(): OpenUIValue {
        skipWhitespace()
        val value = readValue()
        skipWhitespace()
        if (pos < text.length) fail("Unexpected ${describe(pos)}")
        return value
    }

    private fun readValue(): OpenUIValue {
        if (pos >= text.length) fail("Unexpected end of JSON input")
        return when (val c = text[pos]) {
            '{' -> nested { readObject() }
            '[' -> nested { readArray() }
            '"' -> OpenUIValue.Str(readString())
            't' -> readKeyword("true", OpenUIValue.True)
            'f' -> readKeyword("false", OpenUIValue.False)
            'n' -> readKeyword("null", OpenUIValue.Null)
            else -> if (c == '-' || c in '0'..'9') readNumber() else fail("Unexpected ${describe(pos)}")
        }
    }

    private inline fun nested(read: () -> OpenUIValue): OpenUIValue {
        if (++depth > MAX_DEPTH) fail("Nesting deeper than $MAX_DEPTH levels")
        return read().also { depth-- }
    }

    private fun readObject(): OpenUIValue {
        pos++ // {
        val fields = OpenUIObject.Builder()
        skipWhitespace()
        if (peek() == '}') {
            pos++
            return OpenUIValue.Obj(OpenUIObject.Empty)
        }
        while (true) {
            skipWhitespace()
            if (peek() != '"') fail("Expected a property name, found ${describe(pos)}")
            val key = readString()
            skipWhitespace()
            expect(':')
            skipWhitespace()
            // JSON.parse keeps the first position of a repeated key and the last value.
            fields[key] = readValue()
            skipWhitespace()
            when (peek()) {
                ',' -> pos++
                '}' -> {
                    pos++
                    return OpenUIValue.Obj(fields.build())
                }
                else -> fail("Expected ',' or '}' after a property value, found ${describe(pos)}")
            }
        }
    }

    private fun readArray(): OpenUIValue {
        pos++ // [
        val items = ArrayList<OpenUIValue>()
        skipWhitespace()
        if (peek() == ']') {
            pos++
            return OpenUIValue.Arr(items)
        }
        while (true) {
            skipWhitespace()
            items += readValue()
            skipWhitespace()
            when (peek()) {
                ',' -> pos++
                ']' -> {
                    pos++
                    return OpenUIValue.Arr(items)
                }
                else -> fail("Expected ',' or ']' after an array element, found ${describe(pos)}")
            }
        }
    }

    private fun readString(): String {
        pos++ // opening quote
        val out = StringBuilder()
        while (true) {
            if (pos >= text.length) fail("Unterminated string")
            val c = text[pos]
            when {
                c == '"' -> {
                    pos++
                    return out.toString()
                }
                c == '\\' -> readEscape(out)
                c.code < CONTROL_LIMIT -> fail("Bad control character in string literal")
                else -> {
                    out.append(c)
                    pos++
                }
            }
        }
    }

    private fun readEscape(out: StringBuilder) {
        if (pos + 1 >= text.length) fail("Unterminated string")
        when (val e = text[pos + 1]) {
            '"', '\\', '/' -> out.append(e)
            'b' -> out.append('\b')
            'f' -> out.append('\u000C')
            'n' -> out.append('\n')
            'r' -> out.append('\r')
            't' -> out.append('\t')
            'u' -> {
                var code = 0
                for (i in pos + 2 until pos + UNICODE_ESCAPE_LENGTH) {
                    val digit = text.getOrNull(i)?.digitToIntOrNull(HEX_RADIX)
                        ?: fail("Bad Unicode escape", i.coerceAtMost(text.length))
                    code = code * HEX_RADIX + digit
                }
                // Surrogate pairs (`😀`) arrive as two escapes and combine in the UTF-16 string.
                out.append(code.toChar())
                pos += UNICODE_ESCAPE_LENGTH
                return
            }
            else -> fail("Bad escaped character", pos + 1)
        }
        pos += 2
    }

    private fun readNumber(): OpenUIValue {
        val start = pos
        if (peek() == '-') pos++
        when (peek()) {
            '0' -> {
                pos++
                if (peek()?.isAsciiDigit() == true) fail("Leading zeros are not allowed", start)
            }
            in '1'..'9' -> skipDigits()
            else -> fail("No number after minus sign")
        }
        if (peek() == '.') {
            pos++
            if (peek()?.isAsciiDigit() != true) fail("Unterminated fractional number")
            skipDigits()
        }
        if (peek() == 'e' || peek() == 'E') {
            pos++
            if (peek() == '+' || peek() == '-') pos++
            if (peek()?.isAsciiDigit() != true) fail("Exponent part is missing a number")
            skipDigits()
        }
        // The grammar above is a subset of what toDouble accepts, and both round like JavaScript.
        return OpenUIValue.Num(text.substring(start, pos).toDouble())
    }

    private fun readKeyword(keyword: String, value: OpenUIValue): OpenUIValue {
        if (!text.startsWith(keyword, pos)) fail("Unexpected ${describe(pos)}")
        pos += keyword.length
        return value
    }

    private fun skipDigits() {
        while (peek()?.isAsciiDigit() == true) pos++
    }

    private fun skipWhitespace() {
        while (pos < text.length) {
            when (text[pos]) {
                ' ', '\t', '\n', '\r' -> pos++
                else -> return
            }
        }
    }

    private fun expect(c: Char) {
        if (peek() != c) fail("Expected '$c', found ${describe(pos)}")
        pos++
    }

    private fun peek(): Char? = text.getOrNull(pos)

    private fun Char.isAsciiDigit(): Boolean = this in '0'..'9'

    private fun describe(at: Int): String =
        if (at >= text.length) "end of JSON input" else "token '${text[at]}'"

    private fun fail(message: String, at: Int = pos): Nothing =
        throw OpenUIJsonException("$message at position $at")

    private companion object {
        const val MAX_DEPTH = 512
        const val CONTROL_LIMIT = 0x20
        const val HEX_RADIX = 16
        const val UNICODE_ESCAPE_LENGTH = 6
    }
}
