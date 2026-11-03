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

/** Token kinds of OpenUI Lang (mirrors `T` in `tokens.ts`). */
internal enum class TokenType {
    Newline,
    LParen,
    RParen,
    LBrack,
    RBrack,
    LBrace,
    RBrace,
    Comma,
    Colon,
    Equals,
    True,
    False,
    Null,
    EOF,

    /** Carries a string value. */
    Str,

    /** Carries a numeric value. */
    Num,

    /** Lowercase identifier — becomes a reference. */
    Ident,

    /** PascalCase identifier — a component name or reference. */
    Type,

    /** `$identifier` — reactive state reference. */
    StateVar,
    Dot,
    Plus,
    Minus,
    Star,
    Slash,
    Percent,
    EqEq,
    NotEq,
    Greater,
    Less,
    GreaterEq,
    LessEq,
    And,
    Or,
    Not,
    Question,

    /** `@identifier` — builtin function call. */
    BuiltinCall,
}

internal data class Token(val type: TokenType, val text: String? = null, val number: Double = 0.0) {
    /** `String(token.v)` for tokens that carry a value. */
    val valueString: String get() = text ?: JS.numberToString(number)
}

/** Tokenizes OpenUI Lang source into a flat token list (port of `lexer.ts`). */
internal object Lexer {

    private val simple = mapOf(
        '(' to TokenType.LParen, ')' to TokenType.RParen,
        '[' to TokenType.LBrack, ']' to TokenType.RBrack,
        '{' to TokenType.LBrace, '}' to TokenType.RBrace,
        ',' to TokenType.Comma, ':' to TokenType.Colon,
        '.' to TokenType.Dot, '?' to TokenType.Question,
        '+' to TokenType.Plus, '*' to TokenType.Star, '/' to TokenType.Slash, '%' to TokenType.Percent,
    )

    /** Token kinds after which `-` is a binary operator rather than a negative number's sign. */
    private val valueEnders = setOf(
        TokenType.Num, TokenType.Str, TokenType.Ident, TokenType.Type, TokenType.RParen, TokenType.RBrack,
        TokenType.True, TokenType.False, TokenType.Null, TokenType.StateVar, TokenType.BuiltinCall,
    )

    private fun isIdentStart(c: Char) = c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    private fun isIdentPart(c: Char) = isIdentStart(c) || c in '0'..'9'
    private fun isDigit(c: Char) = c in '0'..'9'

    @Suppress("ComplexCondition", "LoopWithTooManyJumpStatements", "NestedBlockDepth")
    fun tokenize(src: String): List<Token> {
        val tokens = ArrayList<Token>()
        var i = 0
        val n = src.length

        fun twoChar(second: Char, double: TokenType, single: TokenType) {
            if (i + 1 < n && src[i + 1] == second) {
                tokens += Token(double)
                i += 2
            } else {
                tokens += Token(single)
                i++
            }
        }

        while (i < n) {
            // Horizontal whitespace is skipped; newlines are significant.
            while (i < n && (src[i] == ' ' || src[i] == '\t' || src[i] == '\r')) i++
            if (i >= n) break
            val c = src[i]

            simple[c]?.let {
                tokens += Token(it)
                i++
                continue
            }
            when (c) {
                '\n' -> {
                    tokens += Token(TokenType.Newline)
                    i++
                    continue
                }
                '=' -> {
                    twoChar('=', TokenType.EqEq, TokenType.Equals)
                    continue
                }
                '!' -> {
                    twoChar('=', TokenType.NotEq, TokenType.Not)
                    continue
                }
                '>' -> {
                    twoChar('=', TokenType.GreaterEq, TokenType.Greater)
                    continue
                }
                '<' -> {
                    twoChar('=', TokenType.LessEq, TokenType.Less)
                    continue
                }
                // A single `&` or `|` is read as `&&` / `||`.
                '&' -> {
                    twoChar('&', TokenType.And, TokenType.And)
                    continue
                }
                '|' -> {
                    twoChar('|', TokenType.Or, TokenType.Or)
                    continue
                }
                '"' -> {
                    i = readDoubleQuoted(src, i, tokens)
                    continue
                }
                '\'' -> {
                    i = readSingleQuoted(src, i, tokens)
                    continue
                }
            }

            // Minus: a negative number literal, or the subtraction / negation operator.
            if (c == '-') {
                val afterValue = tokens.lastOrNull()?.type in valueEnders
                if (afterValue || !(i + 1 < n && isDigit(src[i + 1]))) {
                    tokens += Token(TokenType.Minus)
                    i++
                    continue
                }
            }

            // Number literal: 42, -3, 1.5, 1e10
            if (isDigit(c) || (c == '-' && i + 1 < n && isDigit(src[i + 1]))) {
                val start = i
                if (src[i] == '-') i++
                while (i < n && isDigit(src[i])) i++
                if (i < n && src[i] == '.' && i + 1 < n && isDigit(src[i + 1])) {
                    i++
                    while (i < n && isDigit(src[i])) i++
                }
                if (i < n && (src[i] == 'e' || src[i] == 'E')) {
                    i++
                    if (i < n && (src[i] == '+' || src[i] == '-')) i++
                    while (i < n && isDigit(src[i])) i++
                }
                // `+text` in JavaScript: a dangling exponent ("1e") is NaN.
                tokens += Token(TokenType.Num, number = JS.stringToNumber(src.substring(start, i)))
                continue
            }

            // State variable: $identifier
            if (c == '$' && i + 1 < n && isIdentStart(src[i + 1])) {
                val start = i
                i++
                while (i < n && isIdentPart(src[i])) i++
                tokens += Token(TokenType.StateVar, src.substring(start, i))
                continue
            }

            // Keyword or identifier
            if (isIdentStart(c)) {
                val start = i
                while (i < n && isIdentPart(src[i])) i++
                when (val word = src.substring(start, i)) {
                    "true" -> tokens += Token(TokenType.True)
                    "false" -> tokens += Token(TokenType.False)
                    "null" -> tokens += Token(TokenType.Null)
                    // PascalCase → component type name; lowercase → variable reference.
                    else -> tokens += Token(if (c in 'A'..'Z') TokenType.Type else TokenType.Ident, word)
                }
                continue
            }

            // Builtin call: @identifier
            if (c == '@' && i + 1 < n && isIdentStart(src[i + 1])) {
                i++
                val start = i
                while (i < n && isIdentPart(src[i])) i++
                tokens += Token(TokenType.BuiltinCall, src.substring(start, i))
                continue
            }

            i++ // skip any other character (e.g. #, emojis)
        }

        tokens += Token(TokenType.EOF)
        return tokens
    }

    /** Reads a `"..."` literal and decodes it with `JSON.parse` rules. Returns the index after it. */
    private fun readDoubleQuoted(src: String, start: Int, tokens: MutableList<Token>): Int {
        val n = src.length
        var i = start + 1
        var closed = false
        while (i < n) {
            when (src[i]) {
                '\\' -> i += 2
                '"' -> {
                    i++
                    closed = true
                    break
                }
                else -> i++
            }
        }
        val raw = src.substring(start, minOf(i, n))
        // An unclosed string (streaming) is parsed as if it were closed.
        val decoded = JS.jsonParseString(if (closed) raw else "$raw\"")
            // JSON.parse failed (e.g. a raw newline or a partial \u escape): keep the raw text.
            ?: raw.removePrefix("\"").removeSuffix("\"")
        tokens += Token(TokenType.Str, decoded)
        return minOf(i, n)
    }

    /** Reads a `'...'` literal with its small set of escapes. Returns the index after it. */
    private fun readSingleQuoted(src: String, start: Int, tokens: MutableList<Token>): Int {
        val n = src.length
        var i = start + 1
        val result = StringBuilder()
        while (i < n) {
            val c = src[i]
            if (c == '\\') {
                i++
                if (i < n) {
                    result.append(
                        when (val escaped = src[i]) {
                            'n' -> '\n'
                            't' -> '\t'
                            else -> escaped
                        },
                    )
                    i++
                }
            } else if (c == '\'') {
                i++
                break
            } else {
                result.append(c)
                i++
            }
        }
        tokens += Token(TokenType.Str, result.toString())
        return i
    }
}
