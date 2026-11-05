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

import com.google.re2j.Pattern
import com.google.re2j.PatternSyntaxException

/**
 * The regular expressions of `pattern` rules, which the model writes as JavaScript does.
 *
 * They run on the main thread, so they are matched with RE2J, in time linear in the input: the
 * platform matcher backtracks and can't be interrupted, so a pattern such as `x.*x.*y` would freeze
 * the UI. Matching costs about the input's length times the size of the compiled program, so a
 * pattern is rejected when it is longer than [MAX_PATTERN_LENGTH], or when it expands past
 * [MAX_PROGRAM_SIZE] items once its counts are written out, such as `(?:a{100}){11}` or
 * `a{0,600}b{0,600}`.
 *
 * Written for RE2J, `\uXXXX` and `\u{X}` become `\x{X}`, and `.` matches no line terminator, as
 * in JavaScript. RE2J has no lookahead, so lookaheads right after a leading `^`, such as
 * `^(?=.*\d).{8,}$`, are each matched on their own from the input's start.
 */
internal object PatternSafety {

    /** The most items a pattern may expand to once its counts are written out, as RE2 compiles it. */
    const val MAX_PROGRAM_SIZE: Int = 1000

    /** The longest pattern compiled. */
    const val MAX_PATTERN_LENGTH: Int = 1000

    /** The longest input a `pattern` rule is matched against; longer input fails the rule. */
    const val MAX_INPUT_LENGTH: Int = 4096

    /**
     * A pattern, compiled: [pattern] without its leading lookaheads, the ones that must match at the
     * input's start, and the ones that must not.
     */
    class Compiled internal constructor(
        private val pattern: Pattern,
        private val required: List<Pattern>,
        private val forbidden: List<Pattern>,
    ) {

        /** Whether the pattern finds a match in [text]. */
        fun containsMatchIn(text: String): Boolean =
            required.all { it.matcher(text).lookingAt() } &&
                forbidden.none { it.matcher(text).lookingAt() } &&
                pattern.matcher(text).find()
    }

    /**
     * [pattern] compiled, or null when it is rejected or RE2J can't compile it, such as one with
     * lookbehind, a backreference, or lookahead anywhere but right after a leading `^`.
     */
    fun compile(pattern: String): Compiled? {
        if (pattern.length > MAX_PATTERN_LENGTH || size(pattern) > MAX_PROGRAM_SIZE) return null
        val lookaheads = leadingLookaheads(pattern)
        val rest = lookaheads.lastOrNull()?.let { "^" + pattern.substring(it.end) } ?: pattern
        val (required, forbidden) = lookaheads.partition { !it.negative }
        return try {
            Compiled(
                pattern = Pattern.compile(translate(rest)),
                required = required.map { Pattern.compile(translate(it.body)) },
                forbidden = forbidden.map { Pattern.compile(translate(it.body)) },
            )
        } catch (_: PatternSyntaxException) {
            null
        }
    }

    /**
     * How many items [pattern] expands to once its counts are written out, as RE2 compiles it: 4 for
     * `a{0,3}b`, 6 for `(?:ab){3}`, and 2 for `a+`, which is `aa*`. Stops growing past
     * [MAX_PROGRAM_SIZE].
     */
    @Suppress("CyclomaticComplexMethod", "LongMethod")
    fun size(pattern: String): Int {
        // The size of each enclosing group read so far, and of the group being read.
        val enclosing = ArrayDeque<Int>()
        var total = 0
        // The size of the item a following count would repeat.
        var last = 0
        var index = 0
        while (index < pattern.length) {
            val count = if (pattern[index] == '{') REPEAT.matchAt(pattern, index) else null
            when {
                count != null -> {
                    val (min, range, max) = count.destructured
                    val times = when {
                        range.isEmpty() -> min.toCount()
                        max.isEmpty() -> min.toCount() + 1
                        else -> max.toCount()
                    }.coerceAtLeast(1)
                    total = bounded(total + last * (times - 1))
                    last = bounded(last * times)
                    index = count.range.last + 1
                }
                // A flag group or an empty quote adds nothing, so a following count repeats the item
                // before it.
                pattern.startsWith("\\Q", index) -> {
                    val end = afterEscape(pattern, index)
                    val quoted = pattern.substring(index, end).removePrefix("\\Q").removeSuffix("\\E").length
                    if (quoted > 0) {
                        total = bounded(total + quoted)
                        last = 1
                    }
                    index = end
                }
                FLAGS.matchAt(pattern, index) != null -> index = pattern.indexOf(')', index) + 1
                pattern[index] == '(' -> {
                    enclosing.addLast(total)
                    total = 0
                    last = 0
                    index = GROUP_OPENING.matchAt(pattern, index)?.let { it.range.last + 1 } ?: (index + 1)
                }
                pattern[index] == ')' -> {
                    last = total
                    total = bounded((enclosing.removeLastOrNull() ?: 0) + total)
                    index++
                }
                pattern[index] == '|' -> {
                    last = 0
                    index++
                }
                pattern[index] == '+' -> {
                    total = bounded(total + last)
                    last = bounded(last * 2)
                    index++
                }
                pattern[index] == '*' || pattern[index] == '?' -> index++
                else -> {
                    total = bounded(total + 1)
                    last = 1
                    index = when (pattern[index]) {
                        '\\' -> afterEscape(pattern, index)
                        '[' -> afterClass(pattern, index)
                        else -> index + 1
                    }
                }
            }
        }
        return enclosing.fold(total) { sum, outer -> bounded(sum + outer) }
    }

    private fun bounded(size: Int): Int = minOf(size, MAX_PROGRAM_SIZE + 1)

    private fun String.toCount(): Int = toIntOrNull()?.coerceAtMost(MAX_PROGRAM_SIZE + 1) ?: (MAX_PROGRAM_SIZE + 1)

    private class Lookahead(val body: String, val negative: Boolean, val end: Int)

    // The lookaheads right after a leading `^`, each up to the index after it.
    private fun leadingLookaheads(pattern: String): List<Lookahead> {
        if (!pattern.startsWith('^')) return emptyList()
        val lookaheads = mutableListOf<Lookahead>()
        var index = 1
        while (true) {
            val opening = LOOKAHEAD.matchAt(pattern, index)
            val close = opening?.let { closing(pattern, index) } ?: return lookaheads
            val body = pattern.substring(opening.range.last + 1, close)
            lookaheads.add(Lookahead(body, negative = opening.value.endsWith('!'), end = close + 1))
            index = close + 1
        }
    }

    // The index of the `)` closing the group that opens at [start], or null when it isn't closed.
    private fun closing(pattern: String, start: Int): Int? {
        var depth = 0
        var index = start
        while (index < pattern.length) {
            when (pattern[index]) {
                '\\' -> index = afterEscape(pattern, index) - 1
                '[' -> index = afterClass(pattern, index) - 1
                '(' -> depth++
                ')' -> if (--depth == 0) return index
            }
            index++
        }
        return null
    }

    // [pattern] with JavaScript's meanings in RE2's syntax: `\uXXXX` and `\u{X}` as `\x{X}`, and `.`
    // outside a class as any character but a line terminator.
    private fun translate(pattern: String): String = buildString {
        // The index after the class being read, or -1 outside one.
        var classEnd = -1
        var index = 0
        while (index < pattern.length) {
            if (index >= classEnd) classEnd = -1
            val unicode = UNICODE_ESCAPE.matchAt(pattern, index)
            when {
                unicode != null -> {
                    append("\\x{").append(unicode.groupValues[1].ifEmpty { unicode.groupValues[2] }).append('}')
                    index = unicode.range.last + 1
                }
                pattern[index] == '\\' -> {
                    val end = afterEscape(pattern, index)
                    append(pattern, index, end)
                    index = end
                }
                classEnd < 0 && pattern[index] == '[' -> {
                    classEnd = afterClass(pattern, index)
                    append('[')
                    index++
                }
                classEnd < 0 && pattern[index] == '.' -> {
                    append(ANY_BUT_LINE_TERMINATOR)
                    index++
                }
                else -> {
                    append(pattern[index])
                    index++
                }
            }
        }
    }

    // The index after the escape at [start]: `\Q...\E` to its end, `\x{...}`, `\p{...}` and `\P{...}`
    // to their closing brace, and any other escape after its character.
    private fun afterEscape(pattern: String, start: Int): Int {
        val next = pattern.getOrNull(start + 1) ?: return start + 1
        return when {
            next == 'Q' -> after(pattern, "\\E", start + 2)
            next in "xpP" && pattern.getOrNull(start + 2) == '{' -> after(pattern, "}", start + 2)
            else -> start + 2
        }
    }

    // The index after the character class at [start], whose braces and parentheses are literal.
    private fun afterClass(pattern: String, start: Int): Int {
        var index = start + 1
        if (pattern.getOrNull(index) == '^') index++
        // A `]` first in the class is one of its characters.
        if (pattern.getOrNull(index) == ']') index++
        while (index < pattern.length && pattern[index] != ']') {
            index = when {
                pattern[index] == '\\' -> afterEscape(pattern, index)
                pattern.startsWith("[:", index) -> after(pattern, ":]", index + 2)
                else -> index + 1
            }
        }
        return index + 1
    }

    // The index after the first [end] from [from], or the pattern's length when there is none.
    private fun after(pattern: String, end: String, from: Int): Int =
        pattern.indexOf(end, from).let { if (it < 0) pattern.length else it + end.length }

    private const val ANY_BUT_LINE_TERMINATOR = """[^\n\r\x{2028}\x{2029}]"""
    private val REPEAT = Regex("""\{(\d+)(,(\d*))?}""")
    private val FLAGS = Regex("""\(\?[a-zA-Z-]*\)""")

    // What opens a group before its body: `(?:`, `(?i:`, `(?=`, `(?!`, `(?P<name>` or `(?<name>`.
    private val GROUP_OPENING = Regex("""\(\?(?:[a-zA-Z-]*:|[=!]|P?<[^>]*>)""")
    private val LOOKAHEAD = Regex("""\(\?[=!]""")
    private val UNICODE_ESCAPE = Regex("""\\u(?:\{([0-9A-Fa-f]{1,6})}|([0-9A-Fa-f]{4}))""")
}
