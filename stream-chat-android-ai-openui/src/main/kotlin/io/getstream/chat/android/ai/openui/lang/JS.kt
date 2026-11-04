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

@file:Suppress("MagicNumber") // ECMAScript constants: code points, radix and precision limits.

package io.getstream.chat.android.ai.openui.lang

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.floor

/**
 * JavaScript semantics used by the OpenUI Lang reference implementation.
 *
 * The language spec is defined by a TypeScript runtime, so expression evaluation, string
 * conversion and comparisons follow JavaScript rules. These helpers reproduce the parts the
 * language relies on so that output matches the reference renderers. Kotlin strings are UTF-16
 * like JavaScript's, so lengths and indices line up without conversion.
 */
@Suppress("TooManyFunctions")
internal object JS {

    // Whitespace ---------------------------------------------------------------------------

    /** ECMAScript `WhiteSpace` + `LineTerminator` (used by `trim`, `Number`, `parseFloat`). */
    internal fun isWhitespace(c: Char): Boolean = when (c.code) {
        0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x20, 0xA0, 0x1680, 0x2028, 0x2029, 0x202F, 0x205F, 0x3000, 0xFEFF -> true
        in 0x2000..0x200A -> true
        else -> false
    }

    /** `String.prototype.trim`. */
    internal fun trim(string: String): String = trimEnd(trimStart(string))

    /** `String.prototype.trimStart`. */
    internal fun trimStart(string: String): String = string.trimStart(::isWhitespace)

    /** `String.prototype.trimEnd`. */
    internal fun trimEnd(string: String): String = string.trimEnd(::isWhitespace)

    // Numbers ------------------------------------------------------------------------------

    /** `Number.prototype.toString()` (ECMA-262 Number::toString with radix 10). */
    fun numberToString(value: Double): String {
        if (value.isNaN()) return "NaN"
        if (value == 0.0) return "0"
        if (value.isInfinite()) return if (value < 0) "-Infinity" else "Infinity"
        if (value < 0) return "-" + numberToString(-value)

        val (digits, n) = shortestDigits(value)
        val k = digits.length
        return when {
            n in k..21 -> digits + "0".repeat(n - k)
            n in 1..21 -> digits.substring(0, n) + "." + digits.substring(n)
            n in -5..0 -> "0." + "0".repeat(-n) + digits
            else -> {
                val exponent = n - 1
                val sign = if (exponent < 0) "-" else "+"
                val mantissa = if (k == 1) digits else digits.substring(0, 1) + "." + digits.substring(1)
                mantissa + "e" + sign + abs(exponent)
            }
        }
    }

    /**
     * Shortest round-trip decimal digits `s` and exponent `n` such that `value = 0.s × 10^n`.
     * Rounds the exact binary value to increasing precision until it reads back unchanged.
     */
    private fun shortestDigits(value: Double): Pair<String, Int> {
        val exact = BigDecimal(value)
        for (precision in 1..17) {
            val rounded = exact.round(MathContext(precision, RoundingMode.HALF_EVEN))
            if (rounded.toDouble() == value) {
                val stripped = rounded.stripTrailingZeros()
                val digits = stripped.unscaledValue().toString()
                return digits to digits.length - stripped.scale()
            }
        }
        val stripped = exact.round(MathContext(17, RoundingMode.HALF_EVEN)).stripTrailingZeros()
        val digits = stripped.unscaledValue().toString()
        return digits to digits.length - stripped.scale()
    }

    /** `Number(string)` — ECMAScript StringToNumber. */
    @Suppress("NestedBlockDepth")
    fun stringToNumber(string: String): Double {
        val trimmed = trim(string)
        if (trimmed.isEmpty()) return 0.0
        if (trimmed.length > 2 && trimmed[0] == '0') {
            val radix = when (trimmed[1]) {
                'x', 'X' -> 16
                'o', 'O' -> 8
                'b', 'B' -> 2
                else -> null
            }
            if (radix != null) {
                var result = 0.0
                for (c in trimmed.substring(2)) {
                    val digit = Character.digit(c, radix)
                    if (digit < 0 || !c.isAsciiLetterOrDigit()) return Double.NaN
                    result = result * radix + digit
                }
                return result
            }
        }
        val scanned = scanDecimalLiteral(trimmed) ?: return Double.NaN
        return if (scanned.second == trimmed.length) scanned.first else Double.NaN
    }

    /** `parseFloat(string)`. */
    internal fun parseFloat(string: String): Double = scanDecimalLiteral(trimStart(string))?.first ?: Double.NaN

    private fun Char.isAsciiLetterOrDigit() = this in '0'..'9' || this in 'a'..'z' || this in 'A'..'Z'

    /** Scans the longest ECMAScript `StrDecimalLiteral` prefix (including `Infinity`). */
    private fun scanDecimalLiteral(s: String): Pair<Double, Int>? {
        var i = 0
        var negative = false
        if (i < s.length && (s[i] == '+' || s[i] == '-')) {
            negative = s[i] == '-'
            i++
        }
        if (s.startsWith("Infinity", i)) {
            return (if (negative) Double.NEGATIVE_INFINITY else Double.POSITIVE_INFINITY) to i + 8
        }
        val digitsStart = i
        var integerDigits = 0
        while (i < s.length && s[i] in '0'..'9') {
            i++
            integerDigits++
        }
        var fractionDigits = 0
        if (i < s.length && s[i] == '.') {
            var j = i + 1
            while (j < s.length && s[j] in '0'..'9') {
                j++
                fractionDigits++
            }
            if (integerDigits > 0 || fractionDigits > 0) i = j
        }
        if (integerDigits == 0 && fractionDigits == 0) return null
        var end = i
        if (i < s.length && (s[i] == 'e' || s[i] == 'E')) {
            var j = i + 1
            if (j < s.length && (s[j] == '+' || s[j] == '-')) j++
            val exponentStart = j
            while (j < s.length && s[j] in '0'..'9') j++
            if (j > exponentStart) end = j
        }
        var literal = s.substring(digitsStart, end)
        if (literal.startsWith(".")) literal = "0$literal"
        val magnitude = literal.toDoubleOrNull() ?: return null
        return (if (negative) -magnitude else magnitude) to end
    }

    /**
     * The language's lenient numeric coercion (builtins `toNumber`): numbers pass through,
     * numeric strings convert (invalid ones become `0`), booleans become `1`/`0`, anything else `0`.
     */
    fun toNumber(value: OpenUIValue?): Double = when (value) {
        is OpenUIValue.Num -> value.value
        is OpenUIValue.Str -> stringToNumber(value.value).let { if (it.isNaN()) 0.0 else it }
        is OpenUIValue.Bool -> if (value.value) 1.0 else 0.0
        else -> 0.0
    }

    /** `Math.round` — rounds half up towards +∞. */
    internal fun round(value: Double): Double {
        if (!value.isFinite()) return value
        val floor = floor(value)
        return if (value - floor >= 0.5) floor + 1 else floor
    }

    /** `Math.min` over two values (propagates NaN). */
    internal fun min(lhs: Double, rhs: Double): Double =
        if (lhs.isNaN() || rhs.isNaN()) Double.NaN else kotlin.math.min(lhs, rhs)

    /** `Math.max` over two values (propagates NaN). */
    internal fun max(lhs: Double, rhs: Double): Double =
        if (lhs.isNaN() || rhs.isNaN()) Double.NaN else kotlin.math.max(lhs, rhs)

    /** The `%` operator: the remainder takes the sign of the dividend (like Kotlin's `rem`). */
    internal fun remainder(lhs: Double, rhs: Double): Double = lhs % rhs

    // Conversions --------------------------------------------------------------------------

    /** `String(value)`. */
    fun toString(value: OpenUIValue?): String = when (value) {
        null -> "undefined"
        OpenUIValue.Null -> "null"
        is OpenUIValue.Bool -> value.value.toString()
        is OpenUIValue.Num -> numberToString(value.value)
        is OpenUIValue.Str -> value.value
        is OpenUIValue.Arr -> value.items.joinToString(",") { if (it.isNull) "" else toString(it) }
        is OpenUIValue.Obj, is OpenUIValue.Element, is OpenUIValue.Expression -> "[object Object]"
    }

    /** `String(value ?? "")`. */
    fun toStringOrEmpty(value: OpenUIValue?): String = if (isNullish(value)) "" else toString(value)

    /** JavaScript truthiness. */
    fun isTruthy(value: OpenUIValue?): Boolean = when (value) {
        null, OpenUIValue.Null -> false
        is OpenUIValue.Bool -> value.value
        is OpenUIValue.Num -> value.value != 0.0 && !value.value.isNaN()
        is OpenUIValue.Str -> value.value.isNotEmpty()
        is OpenUIValue.Arr, is OpenUIValue.Obj, is OpenUIValue.Element, is OpenUIValue.Expression -> true
    }

    /** `typeof value`. */
    internal fun typeOf(value: OpenUIValue?): String = when (value) {
        null -> "undefined"
        is OpenUIValue.Str -> "string"
        is OpenUIValue.Num -> "number"
        is OpenUIValue.Bool -> "boolean"
        else -> "object"
    }

    /** `Array.isArray(value) ? "array" : typeof value`. */
    internal fun jsType(value: OpenUIValue?): String = if (value is OpenUIValue.Arr) "array" else typeOf(value)

    // Equality -----------------------------------------------------------------------------

    /**
     * Abstract (loose) equality `==`.
     *
     * Arrays and objects are compared structurally; the reference implementation compares them
     * by identity, which has no value-semantics equivalent.
     */
    internal fun looseEquals(lhs: OpenUIValue?, rhs: OpenUIValue?): Boolean {
        val leftNullish = isNullish(lhs)
        val rightNullish = isNullish(rhs)
        if (leftNullish || rightNullish) return leftNullish && rightNullish
        lhs!!
        rhs!!
        return when {
            lhs is OpenUIValue.Num && rhs is OpenUIValue.Num -> lhs.value == rhs.value
            lhs is OpenUIValue.Str && rhs is OpenUIValue.Str -> lhs.value == rhs.value
            lhs is OpenUIValue.Bool && rhs is OpenUIValue.Bool -> lhs.value == rhs.value
            lhs is OpenUIValue.Num && rhs is OpenUIValue.Str -> lhs.value == stringToNumber(rhs.value)
            lhs is OpenUIValue.Str && rhs is OpenUIValue.Num -> stringToNumber(lhs.value) == rhs.value
            lhs is OpenUIValue.Bool -> looseEquals(OpenUIValue.Num(if (lhs.value) 1.0 else 0.0), rhs)
            rhs is OpenUIValue.Bool -> looseEquals(lhs, OpenUIValue.Num(if (rhs.value) 1.0 else 0.0))
            lhs is OpenUIValue.Num || lhs is OpenUIValue.Str -> looseEquals(lhs, OpenUIValue.Str(toString(rhs)))
            rhs is OpenUIValue.Num || rhs is OpenUIValue.Str -> looseEquals(OpenUIValue.Str(toString(lhs)), rhs)
            else -> lhs == rhs
        }
    }

    /** `value == null` (true for `null` and `undefined`). */
    fun isNullish(value: OpenUIValue?): Boolean = value == null || value is OpenUIValue.Null

    // Property access ----------------------------------------------------------------------

    /** `value[key]` for own properties (no prototype lookups). */
    @Suppress("NestedBlockDepth")
    internal fun property(value: OpenUIValue?, key: String): OpenUIValue? = when (value) {
        is OpenUIValue.Obj -> value.fields[key]
        is OpenUIValue.Arr -> when {
            key == "length" -> OpenUIValue.Num(value.items.size.toDouble())
            else -> arrayIndex(key)?.let { value.items.getOrNull(it) }
        }
        is OpenUIValue.Str -> when {
            key == "length" -> OpenUIValue.Num(value.value.length.toDouble())
            else -> arrayIndex(
                key,
            )?.let { index -> value.value.getOrNull(index)?.let { OpenUIValue.Str(it.toString()) } }
        }
        is OpenUIValue.Element -> value.node.jsonField(key)
        else -> null
    }

    /** Canonical array index (`"0"`, `"12"`; not `"01"` or `"-1"`). */
    internal fun arrayIndex(key: String): Int? {
        if (key.isEmpty() || !key.all { it in '0'..'9' }) return null
        if (key.length > 1 && key.startsWith("0")) return null
        return key.toIntOrNull()
    }

    // JSON ---------------------------------------------------------------------------------

    /** `JSON.stringify(value)`. Returns `null` for `undefined`. */
    internal fun jsonStringify(value: OpenUIValue?): String? {
        if (value == null) return null
        return buildString { writeJson(value, this) }
    }

    private fun writeJson(value: OpenUIValue, out: StringBuilder) {
        when (value) {
            OpenUIValue.Null -> out.append("null")
            is OpenUIValue.Bool -> out.append(value.value)
            is OpenUIValue.Num -> out.append(if (value.value.isFinite()) numberToString(value.value) else "null")
            is OpenUIValue.Str -> writeJsonString(value.value, out)
            is OpenUIValue.Arr -> {
                out.append('[')
                value.items.forEachIndexed { index, item ->
                    if (index > 0) out.append(',')
                    writeJson(item, out)
                }
                out.append(']')
            }
            is OpenUIValue.Obj -> {
                out.append('{')
                var first = true
                for ((key, item) in value.fields) {
                    if (!first) out.append(',')
                    first = false
                    writeJsonString(key, out)
                    out.append(':')
                    writeJson(item, out)
                }
                out.append('}')
            }
            is OpenUIValue.Element -> writeJson(value.node.jsonValue, out)
            is OpenUIValue.Expression -> writeJson(value.node.jsonValue, out)
        }
    }

    /** `JSON.stringify` string quoting (ECMA-262 QuoteJSONString). */
    internal fun writeJsonString(string: String, out: StringBuilder) {
        out.append('"')
        for (c in string) {
            when (c) {
                '"' -> out.append("\\\"")
                '\\' -> out.append("\\\\")
                '\b' -> out.append("\\b")
                '\u000C' -> out.append("\\f")
                '\n' -> out.append("\\n")
                '\r' -> out.append("\\r")
                '\t' -> out.append("\\t")
                else -> if (c.code < 0x20) out.append("\\u%04x".format(c.code)) else out.append(c)
            }
        }
        out.append('"')
    }

    /**
     * `JSON.parse` of a single double-quoted string literal (quotes included).
     * Returns `null` when JavaScript would throw a `SyntaxError`.
     */
    @Suppress("NestedBlockDepth")
    internal fun jsonParseString(literal: String): String? {
        if (literal.length < 2 || literal.first() != '"' || literal.last() != '"') return null
        val out = StringBuilder(literal.length)
        var i = 1
        val end = literal.length - 1
        while (i < end) {
            val c = literal[i]
            when {
                c == '"' -> return null
                c.code < 0x20 -> return null
                c != '\\' -> out.append(c)
                else -> {
                    if (i + 1 >= end) return null
                    when (literal[i + 1]) {
                        '"' -> out.append('"')
                        '\\' -> out.append('\\')
                        '/' -> out.append('/')
                        'b' -> out.append('\b')
                        'f' -> out.append('\u000C')
                        'n' -> out.append('\n')
                        'r' -> out.append('\r')
                        't' -> out.append('\t')
                        'u' -> {
                            if (i + 6 > end) return null
                            val hex = literal.substring(i + 2, i + 6)
                            if (!hex.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) return null
                            out.append(hex.toInt(16).toChar())
                            i += 4
                        }
                        else -> return null
                    }
                    i++
                }
            }
            i++
        }
        return out.toString()
    }
}
