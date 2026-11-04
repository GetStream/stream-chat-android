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

/**
 * Static guard for the regular expressions of `pattern` rules.
 *
 * The model writes those patterns and they run on the main thread, so a pattern that can backtrack
 * exponentially (`(a+)+`, `(a?){25}a{25}`, `(a|aa)*`) would freeze the UI. A match can't be interrupted
 * on Android (the platform matcher copies the input and matches in native code), so instead of timing
 * the match, patterns are inspected before they are compiled. A pattern is unsafe when it has:
 *
 * - a quantifier that can repeat more than once (`*`, `+`, `{n}` with n > 1, `{n,}`, `{n,m}` with m > 1)
 *   applied to a group that contains, at any depth, a variable quantifier (`*`, `+`, `?`, `{n,}`, or
 *   `{n,m}` with n != m) or an alternation;
 * - a backreference (`\1`, `\k<name>`);
 * - a `{n,m}` count above [MAX_REPEAT].
 *
 * A group repeated at most once (`?`, `{0,1}`, `{1}`) is always allowed, so optional parts such as
 * `(?:-[0-9]{4})?` and `(Mr|Mrs)?` pass. An exact count `{n}` never counts as variable, so `(ab){3}` passes.
 *
 * What remains is at most polynomial in the input length, which [MAX_INPUT_LENGTH] bounds.
 */
internal object PatternSafety {

    /** The largest count a `{n,m}` quantifier may use. */
    const val MAX_REPEAT: Int = 1000

    /** The longest input a `pattern` rule is matched against; longer input fails the rule. */
    const val MAX_INPUT_LENGTH: Int = 10_000

    /** Whether [pattern] passes the static check described on [PatternSafety]. */
    fun isSafe(pattern: String): Boolean = Scanner(pattern).scan()

    /** What precedes the scanner's position, which decides how a following quantifier is read. */
    private enum class Atom {
        /** Nothing quantifiable: start of the pattern or a group, or after `|`. */
        NONE,

        /** A character, class, escape or group without quantifiers or alternations. */
        SIMPLE,

        /** A group that contains a variable quantifier or an alternation. */
        COMPLEX,

        /** A quantifier; a `?` or `+` right after it makes it lazy or possessive. */
        QUANTIFIER,
    }

    /** What a group (or the whole pattern) contains so far. */
    private class Frame {
        var variable = false
        var alternation = false
    }

    private class Scanner(private val pattern: String) {
        private var index = 0
        private var safe = true
        private var last = Atom.NONE
        private val frames = ArrayDeque<Frame>().apply { addLast(Frame()) }

        fun scan(): Boolean {
            while (safe && index < pattern.length) {
                when (pattern[index]) {
                    '\\' -> escape()
                    '[' -> characterClass()
                    '(' -> openGroup()
                    ')' -> closeGroup()
                    '|' -> alternation()
                    '*', '+' -> quantifier(length = 1, maxRepeat = UNBOUNDED, variable = true)
                    '?' -> quantifier(length = 1, maxRepeat = 1, variable = true)
                    '{' -> braces()
                    else -> literal(length = 1)
                }
            }
            return safe
        }

        private fun literal(length: Int) {
            index += length
            last = Atom.SIMPLE
        }

        private fun alternation() {
            frames.last().alternation = true
            last = Atom.NONE
            index++
        }

        private fun escape() {
            when (pattern.getOrNull(index + 1)) {
                in '1'..'9', 'k' -> safe = false
                else -> {
                    index = skipEscape(index)
                    last = Atom.SIMPLE
                }
            }
        }

        /** The index after the escape sequence starting at [start], which holds a `\`. */
        private fun skipEscape(start: Int): Int {
            val next = pattern.getOrNull(start + 1)
            return when {
                next == 'Q' -> pattern.indexOf("\\E", start + 2).let { if (it < 0) pattern.length else it + 2 }
                next == 'c' -> start + CONTROL_ESCAPE_LENGTH
                next in BRACED_ESCAPES && pattern.getOrNull(start + 2) == '{' -> after('}', start + 2)
                else -> start + 2
            }
        }

        private fun characterClass() {
            var depth = 0
            do {
                when (pattern[index]) {
                    '\\' -> index = skipEscape(index)
                    '[' -> {
                        depth++
                        index++
                        // A `]` right after the opening `[` or `[^` is a literal.
                        if (pattern.getOrNull(index) == '^') index++
                        if (pattern.getOrNull(index) == ']') index++
                    }
                    ']' -> {
                        depth--
                        index++
                    }
                    else -> index++
                }
            } while (depth > 0 && index < pattern.length)
            last = Atom.SIMPLE
        }

        private fun openGroup() {
            index++
            last = Atom.NONE
            if (pattern.getOrNull(index) != '?') {
                frames.addLast(Frame())
                return
            }
            index++
            when (pattern.getOrNull(index)) {
                ':', '=', '!', '>' -> index++
                '<' -> index = if (pattern.getOrNull(index + 1) in LOOKBEHINDS) index + 2 else after('>', index)
                else -> {
                    // Inline flags: `(?i)` sets them and isn't a group, `(?i:...)` is one.
                    while (index < pattern.length && pattern[index] != ')' && pattern[index] != ':') index++
                    val isGroup = pattern.getOrNull(index) == ':'
                    index++
                    if (!isGroup) return
                }
            }
            frames.addLast(Frame())
        }

        private fun closeGroup() {
            index++
            if (frames.size < 2) {
                safe = false
                return
            }
            val group = frames.removeLast()
            val parent = frames.last()
            parent.variable = parent.variable || group.variable
            parent.alternation = parent.alternation || group.alternation
            last = if (group.variable || group.alternation) Atom.COMPLEX else Atom.SIMPLE
        }

        private fun braces() {
            val match = REPEAT.matchAt(pattern, index)
            if (match == null) {
                literal(length = 1)
                return
            }
            val min = match.groupValues[1].toIntOrNull() ?: Int.MAX_VALUE
            val upper = match.groups[2]?.value
            val max = when {
                upper == null -> min
                upper.isEmpty() -> UNBOUNDED
                else -> upper.toIntOrNull() ?: Int.MAX_VALUE
            }
            if (min > MAX_REPEAT || (max != UNBOUNDED && max > MAX_REPEAT)) {
                safe = false
                return
            }
            quantifier(match.value.length, maxRepeat = max, variable = max != min)
        }

        /**
         * Applies a quantifier of [length] characters that repeats at most [maxRepeat] times
         * ([UNBOUNDED] for no limit) and, when [variable], matches a varying number of times.
         */
        private fun quantifier(length: Int, maxRepeat: Int, variable: Boolean) {
            val modifier = pattern[index] == '?' || pattern[index] == '+'
            when {
                last == Atom.QUANTIFIER && modifier -> last = Atom.NONE
                last == Atom.COMPLEX && (maxRepeat == UNBOUNDED || maxRepeat > 1) -> safe = false
                else -> {
                    if (variable) frames.last().variable = true
                    last = Atom.QUANTIFIER
                }
            }
            index += length
        }

        /** The index after the first [char] at or after [from], or the end of the pattern. */
        private fun after(char: Char, from: Int): Int = pattern.indexOf(char, from).let {
            if (it < 0) pattern.length else it + 1
        }
    }

    /** Escapes that take a `{...}` argument, e.g. `\p{L}` and `\x{1F600}`. */
    private val BRACED_ESCAPES = setOf('p', 'P', 'x', 'N')

    /** The length of a control-character escape such as `\cM`. */
    private const val CONTROL_ESCAPE_LENGTH = 3

    private val LOOKBEHINDS = setOf('=', '!')

    /** The repeat limit of `*`, `+` and `{n,}`. */
    private const val UNBOUNDED = -1

    private val REPEAT = Regex("""\{(\d+)(?:,(\d*))?}""")
}
