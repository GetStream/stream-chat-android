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

/** One `${...}` expression of a `formatString` template. */
internal sealed interface Expression {
    data class Path(val path: DataPath, val source: String) : Expression
    data class Call(val name: String, val arguments: Map<String, Expression>) : Expression
    data class Literal(val value: Any?) : Expression
}

/** A piece of a parsed template: text as it is, or an expression to interpolate. */
internal sealed interface TemplateSegment {
    data class Text(val text: String) : TemplateSegment
    data class Interpolation(val expression: Expression) : TemplateSegment
}

/** A `formatString` template that doesn't parse. */
internal class ExpressionException(message: String) : Exception(message)

/**
 * Parses `formatString` templates, as the A2UI 1.0 basic catalog defines them:
 *
 * - `${/absolute/path}` and `${relative/path}` read the data model;
 * - `${name(arg: value, other: ${/path})}` calls a function, with named arguments that are
 *   quoted strings, numbers, `true`, `false`, `null`, paths, calls or nested `${...}`;
 * - `\${` is a literal `${`.
 */
@Suppress("TooManyFunctions")
internal object ExpressionParser {
    const val MAX_DEPTH: Int = 100
    const val MAX_TEMPLATE_LENGTH: Int = 10_000
    const val MAX_PARTS: Int = 1_000
    private const val ESCAPED_OPEN = "\\\${"

    /** Splits [template] into text and expressions. */
    fun parseTemplate(template: String): List<TemplateSegment> = parseTemplate(template, depth = 0)

    @Suppress("NestedBlockDepth")
    private fun parseTemplate(template: String, depth: Int): List<TemplateSegment> {
        if (template.length > MAX_TEMPLATE_LENGTH) {
            throw ExpressionException(
                "Expression template length (${template.length}) exceeds maximum limit ($MAX_TEMPLATE_LENGTH)",
            )
        }
        val segments = mutableListOf<TemplateSegment>()
        val text = StringBuilder()
        val scanner = Scanner(template)
        fun flush() {
            if (text.isNotEmpty()) {
                segments.add(TemplateSegment.Text(text.toString()))
                text.clear()
            }
        }
        while (!scanner.isAtEnd) {
            if (segments.size >= MAX_PARTS) {
                throw ExpressionException("Expression parts count exceeds maximum limit ($MAX_PARTS)")
            }
            when {
                scanner.matches("\${") -> {
                    scanner.advance(2)
                    val content = extractInterpolation(scanner)
                    when (val expression = parseExpression(content, depth + 1)) {
                        null -> Unit
                        is Expression.Literal -> if (expression.value is String) {
                            text.append(expression.value)
                        } else {
                            flush()
                            segments.add(TemplateSegment.Interpolation(expression))
                        }
                        else -> {
                            flush()
                            segments.add(TemplateSegment.Interpolation(expression))
                        }
                    }
                }
                scanner.matches(ESCAPED_OPEN) -> {
                    scanner.advance(ESCAPED_OPEN.length)
                    text.append("\${")
                }
                else -> text.appendCodePoint(scanner.advance())
            }
        }
        flush()
        return segments
    }

    private fun parseExpression(source: String, depth: Int): Expression? {
        if (depth > MAX_DEPTH) throw maxDepth()
        val scanner = Scanner(source.trim())
        if (scanner.isAtEnd) return null
        val expression = parseValue(scanner, depth)
        scanner.skipWhitespace()
        if (!scanner.isAtEnd) throw ExpressionException("Unexpected characters at end of expression: '${scanner.rest}'")
        return expression
    }

    // `null` reads as an empty string, which interpolates as nothing.
    private fun parseValue(scanner: Scanner, depth: Int): Expression? {
        if (depth > MAX_DEPTH) throw maxDepth()
        scanner.skipWhitespace()
        if (scanner.isAtEnd) return null
        if (scanner.matches("\${")) {
            scanner.advance(2)
            return parseExpression(extractInterpolation(scanner), depth + 1)
        }
        val first = scanner.peek()
        return when {
            first == '\''.code || first == '"'.code -> Expression.Literal(parseString(scanner))
            isNumberStart(scanner) -> Expression.Literal(parseNumber(scanner))
            scanner.matchKeyword("true") -> Expression.Literal(true)
            scanner.matchKeyword("false") -> Expression.Literal(false)
            scanner.matchKeyword("null") -> Expression.Literal("")
            else -> {
                val token = scanPathOrIdentifier(scanner)
                scanner.skipWhitespace()
                when {
                    scanner.peek() == '('.code -> parseCall(token, scanner, depth)
                    token.startsWith("@") -> throw ExpressionException("Expected '(' after function name '$token'")
                    token.isEmpty() -> null
                    !DataPath.isValidSyntax(token) -> invalidPath(token)
                    else -> Expression.Path(DataPath.parse(token), token)
                }
            }
        }
    }

    private fun parseCall(name: String, scanner: Scanner, depth: Int): Expression {
        scanner.advance()
        scanner.skipWhitespace()
        val arguments = LinkedHashMap<String, Expression>()
        while (!scanner.isAtEnd && scanner.peek() != ')'.code) {
            val argument = scanner.scan(::isIdentifierCharacter)
            scanner.skipWhitespace()
            if (scanner.peek() != ':'.code) {
                throw ExpressionException("Expected ':' after argument name '$argument' in function '$name'")
            }
            scanner.advance()
            arguments[argument] = parseValue(scanner, depth + 1) ?: Expression.Literal("")
            scanner.skipWhitespace()
            if (scanner.peek() == ','.code) {
                scanner.advance()
                scanner.skipWhitespace()
            }
        }
        if (scanner.peek() != ')'.code) throw ExpressionException("Expected ')' after function arguments for '$name'")
        scanner.advance()
        return Expression.Call(name, arguments)
    }

    // The content of an interpolation, from after its `${` to before its matching `}`.
    @Suppress("NestedBlockDepth")
    private fun extractInterpolation(scanner: Scanner): String {
        val content = StringBuilder()
        var depth = 1
        while (!scanner.isAtEnd) {
            val char = scanner.advance()
            when (char) {
                '{'.code -> depth++
                '}'.code -> {
                    depth--
                    if (depth == 0) return content.toString()
                }
                '\''.code, '"'.code -> {
                    content.appendCodePoint(char)
                    while (!scanner.isAtEnd) {
                        val quoted = scanner.advance()
                        content.appendCodePoint(quoted)
                        if (quoted == '\\'.code && !scanner.isAtEnd) {
                            content.appendCodePoint(scanner.advance())
                        } else if (quoted == char) {
                            break
                        }
                    }
                    continue
                }
            }
            content.appendCodePoint(char)
        }
        throw ExpressionException("Unclosed interpolation: missing '}'")
    }

    @Suppress("LoopWithTooManyJumpStatements")
    private fun parseString(scanner: Scanner): String {
        val quote = scanner.advance()
        val result = StringBuilder()
        while (!scanner.isAtEnd) {
            val char = scanner.advance()
            when {
                char == '\\'.code -> {
                    if (scanner.isAtEnd) break
                    when (val escaped = scanner.advance()) {
                        'n'.code -> result.append('\n')
                        't'.code -> result.append('\t')
                        'r'.code -> result.append('\r')
                        else -> result.appendCodePoint(escaped)
                    }
                }
                char == quote -> break
                else -> result.appendCodePoint(char)
            }
        }
        return result.toString()
    }

    // A number starts at a digit, or a `.` before a digit, optionally signed. A `-` or `.` inside
    // a path such as `a-1` never gets here: the path scanner takes the whole token.
    private fun isNumberStart(scanner: Scanner): Boolean {
        val offset = if (scanner.peek() == '-'.code || scanner.peek() == '+'.code) 1 else 0
        if (scanner.peek(offset).isAsciiDigit()) return true
        return scanner.peek(offset) == '.'.code && scanner.peek(offset + 1).isAsciiDigit()
    }

    private fun parseNumber(scanner: Scanner): Any {
        val literal = StringBuilder()
        if (scanner.peek() == '-'.code || scanner.peek() == '+'.code) literal.appendCodePoint(scanner.advance())
        literal.append(scanner.scan { it.isAsciiDigit() || it == '.'.code })
        if (scanner.peek() == 'e'.code || scanner.peek() == 'E'.code) {
            literal.appendCodePoint(scanner.advance())
            if (scanner.peek() == '+'.code || scanner.peek() == '-'.code) literal.appendCodePoint(scanner.advance())
            literal.append(scanner.scan { it.isAsciiDigit() })
        }
        val text = literal.toString()
        if (!NUMBER.matches(text)) throw ExpressionException("Invalid number literal: '$text'")
        val unsigned = text.removePrefix("+")
        val isInteger = text.none { it == '.' || it == 'e' || it == 'E' }
        if (isInteger) unsigned.toLongOrNull()?.let { return it }
        val number = unsigned.toDoubleOrNull()?.takeIf { it.isFinite() }
            ?: throw ExpressionException("Number literal is out of range: '$text'")
        // Adding zero turns -0 into 0.
        return number + 0.0
    }

    // `@index` style system names are ASCII: `@[A-Za-z_][A-Za-z0-9_]*`.
    private fun scanPathOrIdentifier(scanner: Scanner): String {
        if (scanner.peek() == '@'.code) {
            val first = scanner.peek(1)
            if (!(first.isAsciiLetter() || first == '_'.code)) {
                throw ExpressionException("Invalid identifier starting with '@' in expression")
            }
            scanner.advance()
            return "@" + scanner.scan { it.isAsciiLetter() || it.isAsciiDigit() || it == '_'.code }
        }
        // `~` starts the JSON Pointer escapes `~0` and `~1`.
        return scanner.scan {
            isIdentifierCharacter(it) || it == '/'.code || it == '.'.code || it == '-'.code || it == '~'.code
        }
    }

    private fun isIdentifierCharacter(codePoint: Int): Boolean = A2UIIdentifier.isContinue(codePoint)

    private fun Int.isAsciiDigit(): Boolean = this in '0'.code..'9'.code

    private fun Int.isAsciiLetter(): Boolean = this in 'a'.code..'z'.code || this in 'A'.code..'Z'.code

    private fun invalidPath(path: String): Nothing = throw ExpressionException("Invalid path syntax: '$path'")

    private fun maxDepth() = ExpressionException("Max recursion depth reached in parse")

    private val NUMBER = Regex("^[+-]?([0-9]+\\.?[0-9]*|\\.[0-9]+)([eE][+-]?[0-9]+)?$")

    /** Reads a string by code point, so characters outside the BMP count as one. */
    private class Scanner(source: String) {
        private val codePoints: IntArray = IntArray(source.codePointCount(0, source.length)).also { points ->
            var index = 0
            var offset = 0
            while (offset < source.length) {
                val codePoint = source.codePointAt(offset)
                points[index++] = codePoint
                offset += Character.charCount(codePoint)
            }
        }
        private var position = 0

        val isAtEnd: Boolean get() = position >= codePoints.size

        val rest: String get() = String(codePoints, position, codePoints.size - position)

        fun peek(offset: Int = 0): Int = codePoints.getOrElse(position + offset) { 0 }

        fun matches(expected: String): Boolean = expected.indices.all { peek(it) == expected[it].code }

        fun advance(): Int = peek().also { position++ }

        fun advance(count: Int) {
            position += count
        }

        fun scan(predicate: (Int) -> Boolean): String {
            val result = StringBuilder()
            while (!isAtEnd && predicate(peek())) result.appendCodePoint(advance())
            return result.toString()
        }

        fun skipWhitespace() {
            scan { Character.isWhitespace(it) }
        }

        // A keyword doesn't run on into an identifier: `trueish` is a path.
        fun matchKeyword(keyword: String): Boolean {
            if (!matches(keyword)) return false
            val next = peek(keyword.length)
            if (next != 0 && A2UIIdentifier.isContinue(next)) return false
            advance(keyword.length)
            return true
        }
    }
}
