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

import java.util.regex.PatternSyntaxException

/**
 * The A2UI regex dialect: a portable subset of ECMAScript patterns in Unicode mode. A pattern is
 * parsed, checked against static safety rules, and then written out as an equivalent Java pattern
 * with ECMAScript meanings for `.`, `$`, `\d`, `\w`, `\s` and `\b`.
 *
 * The static check is the only guard against catastrophic backtracking: the platform matcher
 * can't be interrupted, so the shapes that backtrack catastrophically are rejected before any
 * matching. The check rejects:
 * - a quantifier (`*`, `+`, `?` or `{n,m}`) applied to a group that contains a quantifier or an
 *   alternation, such as `(a?){25}`, `(a+)+`, `(a|aa)*` or `(?:a*)*`;
 * - two unbounded quantifiers over overlapping characters with only empty-matching items between
 *   them, such as `\d+\s*\d+`;
 * - backreferences;
 * - a `{n,m}` count above [MAX_REPEAT_COUNT];
 * - a pattern longer than [MAX_PATTERN_LENGTH] code points.
 *
 * A value longer than [MAX_VALUE_LENGTH] code points is not matched at all.
 */
@Suppress("TooManyFunctions")
internal object A2UIRegex {

    /** The longest pattern accepted, in code points. */
    const val MAX_PATTERN_LENGTH: Int = 256

    /** The longest value matched, in code points; a longer one doesn't match. */
    const val MAX_VALUE_LENGTH: Int = 4096

    /** The largest count a `{n,m}` quantifier may have. */
    const val MAX_REPEAT_COUNT: Int = 1000

    /**
     * [pattern] as a [Regex] with the dialect's meaning.
     *
     * @throws FunctionException With `EXPRESSION_ERROR` when the pattern is too long, isn't in
     * the dialect, or fails the static safety check.
     */
    fun compile(pattern: String): Regex {
        if (pattern.codePointCount(0, pattern.length) > MAX_PATTERN_LENGTH) {
            reject("The pattern is longer than $MAX_PATTERN_LENGTH code points.")
        }
        val tree = Parser(pattern).parse()
        Safety.check(tree)
        return try {
            Regex(Writer.write(tree))
        } catch (_: PatternSyntaxException) {
            reject("The pattern is not valid.")
        }
    }

    /**
     * Whether [regex], compiled by [compile], finds a match in [value]. A value longer than
     * [MAX_VALUE_LENGTH] code points doesn't match.
     */
    fun containsMatch(regex: Regex, value: String): Boolean =
        value.codePointCount(0, value.length) <= MAX_VALUE_LENGTH && regex.containsMatchIn(value)

    private fun reject(reason: String): Nothing =
        throw FunctionException(RendererError.EXPRESSION_ERROR, "'regex' rejects the pattern: $reason")

    internal sealed interface Node
    internal class Alternation(val branches: List<Sequence>) : Node
    internal class Sequence(val items: List<Node>) : Node
    internal class Characters(val set: CharSet) : Node
    internal class Group(val body: Alternation, val kind: GroupKind) : Node
    internal class Assertion(val java: String) : Node
    internal class Repeat(val node: Node, val min: Int, val max: Int?, val lazy: Boolean) : Node

    internal enum class GroupKind(val java: String, val isLookahead: Boolean) {
        CAPTURING("(", false),
        NON_CAPTURING("(?:", false),
        LOOKAHEAD("(?=", true),
        NEGATIVE_LOOKAHEAD("(?!", true),
    }

    /** A set of code points, as sorted, disjoint, non-adjacent ranges. */
    internal class CharSet private constructor(val ranges: List<IntRange>) {

        fun union(other: CharSet): CharSet = of(ranges + other.ranges)

        fun complement(): CharSet {
            val result = mutableListOf<IntRange>()
            var next = 0
            for (range in ranges) {
                if (range.first > next) result.add(next until range.first)
                next = range.last + 1
            }
            if (next <= MAX_CODE_POINT) result.add(next..MAX_CODE_POINT)
            return CharSet(result)
        }

        fun overlaps(other: CharSet): Boolean = ranges.any { range ->
            other.ranges.any { it.first <= range.last && range.first <= it.last }
        }

        companion object {
            const val MAX_CODE_POINT: Int = 0x10FFFF
            val EMPTY: CharSet = CharSet(emptyList())

            fun of(vararg ranges: IntRange): CharSet = of(ranges.toList())

            fun single(codePoint: Int): CharSet = CharSet(listOf(codePoint..codePoint))

            fun of(ranges: List<IntRange>): CharSet {
                val merged = mutableListOf<IntRange>()
                for (range in ranges.filter { !it.isEmpty() }.sortedBy { it.first }) {
                    val last = merged.lastOrNull()
                    if (last != null && range.first <= last.last + 1) {
                        merged[merged.lastIndex] = last.first..maxOf(last.last, range.last)
                    } else {
                        merged.add(range)
                    }
                }
                return CharSet(merged)
            }
        }
    }

    private const val HEX_ESCAPE_DIGITS = 2
    private const val UNICODE_ESCAPE_DIGITS = 4
    private const val MAX_BRACED_UNICODE_DIGITS = 6

    private val DIGIT = CharSet.of('0'.code..'9'.code)
    private val WORD = CharSet.of('0'.code..'9'.code, 'A'.code..'Z'.code, 'a'.code..'z'.code, '_'.code..'_'.code)

    @Suppress("MagicNumber")
    private val SPACE = CharSet.of(
        0x09..0x0D, 0x20..0x20, 0xA0..0xA0, 0x1680..0x1680, 0x2000..0x200A,
        0x2028..0x2029, 0x202F..0x202F, 0x205F..0x205F, 0x3000..0x3000, 0xFEFF..0xFEFF,
    )

    @Suppress("MagicNumber")
    private val DOT = CharSet.of(0x0A..0x0A, 0x0D..0x0D, 0x2028..0x2029).complement()
    private const val SYNTAX_CHARACTERS = "^$\\.*+?()[]{}|/"
    private const val HEX_RADIX = 16
    private const val BACKSPACE = 0x08
    private const val VERTICAL_TAB = 0x0B
    private const val FORM_FEED = 0x0C

    private class Parser(pattern: String) {
        private val points = IntArray(pattern.codePointCount(0, pattern.length)).also { points ->
            var offset = 0
            for (i in points.indices) {
                points[i] = pattern.codePointAt(offset)
                offset += Character.charCount(points[i])
            }
        }
        private var index = 0

        fun parse(): Alternation {
            val tree = alternation()
            if (index < points.size) fail("Unmatched ')'.")
            return tree
        }

        private fun peek(offset: Int = 0): Int = points.getOrElse(index + offset) { -1 }

        private fun take(): Int = points[index++]

        private fun fail(message: String): Nothing = reject(message)

        private fun alternation(): Alternation {
            val branches = mutableListOf(sequence())
            while (peek() == '|'.code) {
                index++
                branches.add(sequence())
            }
            return Alternation(branches)
        }

        private fun sequence(): Sequence {
            val items = mutableListOf<Node>()
            while (index < points.size && peek() != '|'.code && peek() != ')'.code) {
                items.add(quantified(term()))
            }
            return Sequence(items)
        }

        private fun quantified(node: Node): Node {
            val (min, max) = when (peek()) {
                '*'.code -> step(0, null)
                '+'.code -> step(1, null)
                '?'.code -> step(0, 1)
                '{'.code -> braces()
                else -> return node
            }
            if (node is Assertion || (node is Group && node.kind.isLookahead)) fail("An assertion can't be repeated.")
            val lazy = peek() == '?'.code
            if (lazy) index++
            return Repeat(node, min, max, lazy)
        }

        // A one-character quantifier, stepped over.
        private fun step(min: Int, max: Int?): Pair<Int, Int?> {
            index++
            return min to max
        }

        // `{n}`, `{n,}` or `{n,m}`; leaves the index after the closing brace.
        private fun braces(): Pair<Int, Int?> {
            index++
            val min = number() ?: fail("Incomplete quantifier.")
            var max: Int? = min
            if (peek() == ','.code) {
                index++
                max = if (peek() == '}'.code) null else number() ?: fail("Incomplete quantifier.")
            }
            if (peek() != '}'.code) fail("Incomplete quantifier.")
            if (max != null && max < min) fail("Numbers out of order in quantifier.")
            index++
            return min to max
        }

        private fun number(): Int? {
            val start = index
            while (peek() in '0'.code..'9'.code) index++
            if (index == start) return null
            val value = String(points, start, index - start).toIntOrNull()
            if (value == null || value > MAX_REPEAT_COUNT) fail("A quantifier's count is above $MAX_REPEAT_COUNT.")
            return value
        }

        private fun term(): Node {
            val point = take()
            return when (point) {
                '^'.code -> Assertion("^")
                '$'.code -> Assertion("\\z")
                '.'.code -> Characters(DOT)
                '('.code -> group()
                '['.code -> Characters(characterClass())
                '\\'.code -> atomEscape()
                '*'.code, '+'.code, '?'.code, '{'.code -> fail("Nothing to repeat.")
                '}'.code, ']'.code -> fail("Lone '${String(Character.toChars(point))}'.")
                else -> Characters(CharSet.single(point))
            }
        }

        private fun group(): Node {
            val kind = if (peek() == '?'.code) {
                index++
                when (if (index < points.size) take() else -1) {
                    ':'.code -> GroupKind.NON_CAPTURING
                    '='.code -> GroupKind.LOOKAHEAD
                    '!'.code -> GroupKind.NEGATIVE_LOOKAHEAD
                    else -> fail("Lookbehind, named groups and inline flags are not allowed.")
                }
            } else {
                GroupKind.CAPTURING
            }
            val body = alternation()
            if (peek() != ')'.code) fail("Unterminated group.")
            index++
            return Group(body, kind)
        }

        private fun atomEscape(): Node {
            if (index >= points.size) fail("'\\' at end of pattern.")
            return when (peek()) {
                'b'.code -> {
                    index++
                    Assertion(WORD_BOUNDARY)
                }
                'B'.code -> {
                    index++
                    Assertion(NOT_WORD_BOUNDARY)
                }
                else -> Characters(escape(inClass = false))
            }
        }

        // The set an escape stands for, the backslash already read.
        @Suppress("CyclomaticComplexMethod")
        private fun escape(inClass: Boolean): CharSet {
            val point = take()
            return when (point) {
                'd'.code -> DIGIT
                'D'.code -> DIGIT.complement()
                'w'.code -> WORD
                'W'.code -> WORD.complement()
                's'.code -> SPACE
                'S'.code -> SPACE.complement()
                't'.code -> CharSet.single('\t'.code)
                'n'.code -> CharSet.single('\n'.code)
                'v'.code -> CharSet.single(VERTICAL_TAB)
                'f'.code -> CharSet.single(FORM_FEED)
                'r'.code -> CharSet.single('\r'.code)
                'b'.code -> if (inClass) CharSet.single(BACKSPACE) else fail("Unexpected '\\b'.")
                '-'.code -> if (inClass) CharSet.single('-'.code) else fail("Invalid escape '\\-'.")
                '0'.code -> if (peek() in '0'.code..'9'.code) {
                    fail("Octal escapes are not allowed.")
                } else {
                    CharSet.single(0)
                }
                in '1'.code..'9'.code -> fail("Backreferences are not allowed.")
                'x'.code -> CharSet.single(hex(HEX_ESCAPE_DIGITS))
                'u'.code -> CharSet.single(unicodeEscape())
                else -> if (point in SYNTAX_CHARACTERS.map { it.code }) {
                    CharSet.single(point)
                } else {
                    fail("The escape '\\${String(Character.toChars(point))}' is not allowed.")
                }
            }
        }

        private fun unicodeEscape(): Int {
            if (peek() == '{'.code) {
                index++
                val start = index
                while (peek() != '}'.code && index < points.size) index++
                if (peek() != '}'.code || index == start) fail("Invalid Unicode escape.")
                val text = String(points, start, index - start)
                index++
                val value = text.takeIf { it.length <= MAX_BRACED_UNICODE_DIGITS }?.toIntOrNull(HEX_RADIX)
                if (value == null || value > CharSet.MAX_CODE_POINT) fail("Invalid Unicode escape.")
                return value
            }
            val high = hex(UNICODE_ESCAPE_DIGITS)
            if (Character.isHighSurrogate(high.toChar()) && peek() == '\\'.code && peek(1) == 'u'.code) {
                val mark = index
                index += 2
                val low = runCatching { hex(UNICODE_ESCAPE_DIGITS) }.getOrNull()
                if (low != null && Character.isLowSurrogate(low.toChar())) {
                    return Character.toCodePoint(high.toChar(), low.toChar())
                }
                index = mark
            }
            return high
        }

        private fun hex(digits: Int): Int {
            if (index + digits > points.size) fail("Invalid hexadecimal escape.")
            val text = String(points, index, digits)
            val value = text.takeIf { it.all { char -> char.isLetterOrDigit() } }?.toIntOrNull(HEX_RADIX)
                ?: fail("Invalid hexadecimal escape.")
            index += digits
            return value
        }

        private fun characterClass(): CharSet {
            val negated = peek() == '^'.code
            if (negated) index++
            var set = CharSet.EMPTY
            while (peek() != ']'.code) {
                if (index >= points.size) fail("Unterminated character class.")
                val (from, fromIsSingle) = classAtom()
                if (peek() == '-'.code && peek(1) != ']'.code && peek(1) != -1) {
                    index++
                    val (to, toIsSingle) = classAtom()
                    if (!fromIsSingle || !toIsSingle) fail("A class escape can't bound a range.")
                    val first = from.ranges.single().first
                    val last = to.ranges.single().first
                    if (last < first) fail("Range out of order in character class.")
                    set = set.union(CharSet.of(first..last))
                } else {
                    set = set.union(from)
                }
            }
            index++
            return if (negated) set.complement() else set
        }

        // A class member and whether it is one code point, which a range may use.
        private fun classAtom(): Pair<CharSet, Boolean> {
            val point = take()
            if (point != '\\'.code) return CharSet.single(point) to true
            if (index >= points.size) fail("'\\' at end of pattern.")
            val escaped = peek()
            val set = escape(inClass = true)
            val isClassEscape = escaped in "dDwWsS".map { it.code }
            return set to !isClassEscape
        }
    }

    private const val WORD_CLASS = "[0-9A-Za-z_]"
    private const val WORD_BOUNDARY = "(?:(?<=$WORD_CLASS)(?!$WORD_CLASS)|(?<!$WORD_CLASS)(?=$WORD_CLASS))"
    private const val NOT_WORD_BOUNDARY = "(?:(?<=$WORD_CLASS)(?=$WORD_CLASS)|(?<!$WORD_CLASS)(?!$WORD_CLASS))"

    /** The protocol's static safety check, which runs before any matching. */
    private object Safety {

        fun check(node: Node) {
            when (node) {
                is Alternation -> node.branches.forEach(::check)
                is Sequence -> {
                    adjacentRepeats(node)
                    node.items.forEach(::check)
                }
                is Group -> check(node.body)
                is Repeat -> {
                    if (node.node is Group && containsQuantifierOrAlternation(node.node.body)) {
                        fail("A quantifier applies to a group that has a quantifier or an alternation.")
                    }
                    check(node.node)
                }
                is Characters, is Assertion -> Unit
            }
        }

        // Two unbounded atoms that overlap, with only empty-matching items between them.
        private fun adjacentRepeats(sequence: Sequence) {
            val items = sequence.items
            for (i in items.indices) {
                val left = unwrap(items[i])
                if (left !is Repeat || left.max != null) continue
                for (j in i + 1 until items.size) {
                    val right = unwrap(items[j])
                    if (right is Repeat && right.max == null && characters(left).overlaps(characters(right))) {
                        fail("Two unbounded quantifiers over overlapping characters are adjacent.")
                    }
                    if (!canMatchEmpty(items[j])) break
                }
            }
        }

        private fun fail(message: String): Nothing = reject(message)

        // A group whose only content is one quantified atom counts as that atom.
        private fun unwrap(node: Node): Node {
            if (node !is Group || node.kind.isLookahead) return node
            val only = node.body.branches.singleOrNull()?.items?.singleOrNull() ?: return node
            return if (only is Repeat) only else unwrap(only).takeIf { it is Repeat } ?: node
        }

        private fun containsQuantifierOrAlternation(node: Node): Boolean = when (node) {
            is Repeat -> true
            is Group -> containsQuantifierOrAlternation(node.body)
            is Alternation -> node.branches.size > 1 || node.branches.any(::containsQuantifierOrAlternation)
            is Sequence -> node.items.any(::containsQuantifierOrAlternation)
            is Characters, is Assertion -> false
        }

        private fun canMatchEmpty(node: Node): Boolean = when (node) {
            is Characters -> false
            is Assertion -> true
            is Group -> node.kind.isLookahead || canMatchEmpty(node.body)
            is Alternation -> node.branches.any(::canMatchEmpty)
            is Sequence -> node.items.all(::canMatchEmpty)
            is Repeat -> node.min == 0 || canMatchEmpty(node.node)
        }

        // Every code point [node] can consume.
        private fun characters(node: Node): CharSet = when (node) {
            is Characters -> node.set
            is Assertion -> CharSet.EMPTY
            is Group -> if (node.kind.isLookahead) CharSet.EMPTY else characters(node.body)
            is Alternation -> node.branches.fold(CharSet.EMPTY) { set, branch -> set.union(characters(branch)) }
            is Sequence -> node.items.fold(CharSet.EMPTY) { set, item -> set.union(characters(item)) }
            is Repeat -> characters(node.node)
        }
    }

    /** Writes a checked tree as a Java pattern. */
    private object Writer {

        fun write(node: Node): String = StringBuilder().also { write(node, it) }.toString()

        private fun write(node: Node, out: StringBuilder) {
            when (node) {
                is Alternation -> node.branches.forEachIndexed { index, branch ->
                    if (index > 0) out.append('|')
                    write(branch, out)
                }
                is Sequence -> node.items.forEach { write(it, out) }
                is Characters -> characters(node.set, out)
                is Assertion -> out.append(node.java)
                is Group -> {
                    out.append(node.kind.java)
                    write(node.body, out)
                    out.append(')')
                }
                is Repeat -> {
                    out.append("(?:")
                    write(node.node, out)
                    out.append(')')
                    out.append(
                        when {
                            node.min == 0 && node.max == null -> "*"
                            node.min == 1 && node.max == null -> "+"
                            node.min == 0 && node.max == 1 -> "?"
                            node.max == null -> "{${node.min},}"
                            node.max == node.min -> "{${node.min}}"
                            else -> "{${node.min},${node.max}}"
                        },
                    )
                    if (node.lazy) out.append('?')
                }
            }
        }

        private fun characters(set: CharSet, out: StringBuilder) {
            if (set.ranges.isEmpty()) {
                out.append("[^\\x{0}-\\x{10FFFF}]")
                return
            }
            out.append('[')
            for (range in set.ranges) {
                out.append("\\x{").append(Integer.toHexString(range.first)).append('}')
                if (range.last != range.first) {
                    out.append("-\\x{").append(Integer.toHexString(range.last)).append('}')
                }
            }
            out.append(']')
        }
    }
}
