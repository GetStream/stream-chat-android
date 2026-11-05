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
 * The regular expressions of `pattern` rules, which the model writes.
 *
 * They run on the main thread, so they are matched with RE2J, in time linear in the input: the
 * platform matcher backtracks and can't be interrupted, so a pattern such as `x.*x.*y` would freeze
 * the UI. Compiling is bounded instead, as RE2 bounds it and RE2J doesn't. A pattern is rejected
 * when it is longer than [MAX_PATTERN_LENGTH], or when counts nested inside each other multiply to
 * more than [MAX_REPEAT], such as `(?:a{100}){11}`.
 */
internal object PatternSafety {

    /** The largest count a `{n,m}` quantifier may use, alone or multiplied by the counts around it. */
    const val MAX_REPEAT: Int = 1000

    /** The longest pattern compiled. */
    const val MAX_PATTERN_LENGTH: Int = 1000

    /** The longest input a `pattern` rule is matched against; longer input fails the rule. */
    const val MAX_INPUT_LENGTH: Int = 4096

    /**
     * [pattern] compiled, or null when it is rejected or RE2J can't compile it, such as one with
     * lookaround or a backreference.
     */
    fun compile(pattern: String): Pattern? {
        if (pattern.length > MAX_PATTERN_LENGTH || repeats(pattern) > MAX_REPEAT) return null
        return try {
            Pattern.compile(pattern)
        } catch (_: PatternSyntaxException) {
            null
        }
    }

    /**
     * The largest product of `{n,m}` counts nested inside each other in [pattern], such as 30 for
     * `(?:a{3}){10}`. `*`, `+` and `?` count once, as in RE2. Stops growing past [MAX_REPEAT].
     */
    @Suppress("CyclomaticComplexMethod")
    fun repeats(pattern: String): Int {
        // The largest product in each enclosing group, and in the group being read.
        val enclosing = ArrayDeque<Int>()
        var largest = 1
        // The product of the item a following count would repeat.
        var last = 1
        var index = 0
        while (index < pattern.length) {
            when (pattern[index]) {
                '\\' -> {
                    index = afterEscape(pattern, index)
                    last = 1
                }
                '[' -> {
                    index = afterClass(pattern, index)
                    last = 1
                }
                '(' -> {
                    enclosing.addLast(largest)
                    largest = 1
                    index++
                }
                ')' -> {
                    last = largest
                    largest = maxOf(enclosing.removeLastOrNull() ?: 1, last)
                    index++
                }
                '{' -> {
                    val count = REPEAT.matchAt(pattern, index)
                    if (count == null) {
                        last = 1
                        index++
                    } else {
                        val times = count.groupValues[2].ifEmpty { count.groupValues[1] }
                        last = minOf(maxOf(times.toIntOrNull() ?: Int.MAX_VALUE, 1).toLong() * last, MAX_REPEAT + 1L)
                            .toInt()
                        largest = maxOf(largest, last)
                        index = count.range.last + 1
                    }
                }
                '*', '+', '?' -> index++
                else -> {
                    last = 1
                    index++
                }
            }
        }
        return enclosing.fold(largest, ::maxOf)
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

    private val REPEAT = Regex("""\{(\d+)(?:,(\d*))?}""")
}
