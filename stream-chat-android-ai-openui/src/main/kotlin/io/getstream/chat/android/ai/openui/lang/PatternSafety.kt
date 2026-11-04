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
 * exponentially (`(a+)+`, `(a?){25}a{25}`, `(a|aa)*`) or polynomially to a high degree
 * (`[a-z]*[a-z]*[a-z]*[a-z]*1`) would freeze the UI. A match can't be interrupted on Android (the
 * platform matcher copies the input and matches in native code), so instead of timing the match,
 * patterns are inspected before they are compiled. A pattern is unsafe when it has:
 *
 * - a quantifier that can repeat more than once (`*`, `+`, `{n}` with n > 1, `{n,}`, `{n,m}` with m > 1)
 *   applied to a group that contains, at any depth, a variable quantifier (`*`, `+`, `?`, `{n,}`, or
 *   `{n,m}` with n != m) or an alternation;
 * - two unbounded quantifiers (`*`, `+`, `{n,}`) over overlapping characters with only items that can
 *   match nothing between them, such as `\d+\s*\d+`, `[a-z]*[a-z]*` or `a+a+`. A group whose only
 *   content is one quantified item counts as that item;
 * - a backreference (`\1`, `\k<name>`);
 * - a `{n,m}` count above [MAX_REPEAT];
 * - the comments flag (`(?x)`), which changes what the pattern's characters mean.
 *
 * A group repeated at most once (`?`, `{0,1}`, `{1}`) is always allowed, so optional parts such as
 * `(?:-[0-9]{4})?` and `(Mr|Mrs)?` pass. An exact count `{n}` never counts as variable, so `(ab){3}` passes.
 * Characters are compared generously: a property such as `\p{L}` counts as any character, and with the
 * case-insensitive flag a letter overlaps its other case and any non-ASCII character.
 *
 * What remains is at most polynomial of a low degree in the input length, which [MAX_INPUT_LENGTH] bounds.
 */
internal object PatternSafety {

    /** The largest count a `{n,m}` quantifier may use. */
    const val MAX_REPEAT: Int = 1000

    /** The longest input a `pattern` rule is matched against; longer input fails the rule. */
    const val MAX_INPUT_LENGTH: Int = 4096

    /** Whether [pattern] passes the static check described on [PatternSafety]. */
    fun isSafe(pattern: String): Boolean {
        // Flags turned on anywhere count everywhere, which only makes the check stricter.
        val flags = INLINE_FLAGS.findAll(pattern).joinToString("") { it.groupValues[1] }
        if ('x' in flags) return false
        return Scanner(pattern, CharReader(pattern, foldCase = 'i' in flags, unicodeClasses = 'U' in flags)).scan()
    }

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

    /**
     * One item of a sequence: the characters it can consume, whether it can match nothing, and
     * whether it is an unbounded repeat (or a group whose only content is one).
     */
    private class Item(val chars: CharSet, val canMatchEmpty: Boolean, val unbounded: Boolean = false)

    /** What a group (or the whole pattern) contains so far. */
    private class Frame(val isLookaround: Boolean = false) {
        var variable = false
        var alternation = false

        /** The items of the branch being read. */
        val branch = mutableListOf<Item>()

        /** What the finished branches consume. */
        var chars = CharSet.EMPTY

        /** Whether a finished branch can match nothing. */
        var canMatchEmpty = false

        /** The finished branches. */
        var branches = 0

        /** The only item of the first branch, if it has exactly one. */
        var only: Item? = null
    }

    private class Scanner(private val pattern: String, private val reader: CharReader) {
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
                    '*', '+' -> quantifier(length = 1, minRepeat = if (pattern[index] == '*') 0 else 1, UNBOUNDED)
                    '?' -> quantifier(length = 1, minRepeat = 0, maxRepeat = 1)
                    '{' -> braces()
                    '^', '$' -> push(length = 1, Item(CharSet.EMPTY, canMatchEmpty = true))
                    '.' -> push(length = 1, Item(reader.fold(CharSet.ALL), canMatchEmpty = false))
                    else -> {
                        val codePoint = pattern.codePointAt(index)
                        push(Character.charCount(codePoint), reader.item(CharSet.single(codePoint)))
                    }
                }
            }
            if (safe) endBranch(frames.last())
            return safe
        }

        /** Adds [item] of [length] characters to the branch being read. */
        private fun push(length: Int, item: Item) {
            frames.last().branch += item
            index += length
            last = Atom.SIMPLE
        }

        private fun alternation() {
            val frame = frames.last()
            frame.alternation = true
            endBranch(frame)
            last = Atom.NONE
            index++
        }

        /** Checks the branch being read of [frame] and adds it to what the frame holds. */
        private fun endBranch(frame: Frame) {
            val items = frame.branch
            if (hasAdjacentRepeats(items)) safe = false
            for (item in items) frame.chars = frame.chars.union(item.chars)
            frame.canMatchEmpty = frame.canMatchEmpty || items.all { it.canMatchEmpty }
            if (frame.branches == 0) frame.only = items.singleOrNull()
            frame.branches++
            items.clear()
        }

        private fun escape() {
            when (pattern.getOrNull(index + 1)) {
                in '1'..'9', 'k' -> safe = false
                'Q' -> {
                    val quote = reader.escape(index)
                    // An empty quote is nothing: a quantifier after it applies to what precedes it.
                    if (quote.quoted.isNotEmpty()) {
                        quote.quoted.forEach { frames.last().branch += reader.item(CharSet.single(it)) }
                        last = Atom.SIMPLE
                    }
                    index = quote.end
                }
                else -> {
                    val escape = reader.escape(index)
                    val item = if (escape.isAssertion) {
                        Item(CharSet.EMPTY, canMatchEmpty = true)
                    } else {
                        reader.item(escape.chars)
                    }
                    push(escape.end - index, item)
                }
            }
        }

        private fun characterClass() {
            val (chars, end) = reader.characterClass(index)
            push(end - index, reader.item(chars))
        }

        private fun openGroup() {
            index++
            last = Atom.NONE
            if (pattern.getOrNull(index) != '?') {
                frames.addLast(Frame())
                return
            }
            index++
            var isLookaround = false
            when (pattern.getOrNull(index)) {
                ':', '>' -> index++
                '=', '!' -> {
                    isLookaround = true
                    index++
                }
                '<' -> if (pattern.getOrNull(index + 1) in LOOKBEHINDS) {
                    isLookaround = true
                    index += 2
                } else {
                    index = after('>', index)
                }
                else -> {
                    // Inline flags: `(?i)` sets them and isn't a group, `(?i:...)` is one.
                    while (index < pattern.length && pattern[index] != ')' && pattern[index] != ':') index++
                    val isGroup = pattern.getOrNull(index) == ':'
                    index++
                    if (!isGroup) return
                }
            }
            frames.addLast(Frame(isLookaround))
        }

        private fun closeGroup() {
            index++
            if (frames.size < 2) {
                safe = false
                return
            }
            val group = frames.removeLast()
            endBranch(group)
            if (!safe) return
            val parent = frames.last()
            parent.variable = parent.variable || group.variable
            parent.alternation = parent.alternation || group.alternation
            parent.branch += if (group.isLookaround) {
                Item(CharSet.EMPTY, canMatchEmpty = true)
            } else {
                Item(group.chars, group.canMatchEmpty, unbounded = group.branches == 1 && group.only?.unbounded == true)
            }
            last = if (group.variable || group.alternation) Atom.COMPLEX else Atom.SIMPLE
        }

        private fun braces() {
            val match = REPEAT.matchAt(pattern, index)
            if (match == null) {
                push(length = 1, reader.item(CharSet.single('{'.code)))
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
            quantifier(match.value.length, minRepeat = min, maxRepeat = max)
        }

        /**
         * Applies a quantifier of [length] characters that repeats at least [minRepeat] and at most
         * [maxRepeat] times ([UNBOUNDED] for no limit).
         */
        private fun quantifier(length: Int, minRepeat: Int, maxRepeat: Int) {
            val modifier = pattern[index] == '?' || pattern[index] == '+'
            when {
                last == Atom.QUANTIFIER && modifier -> last = Atom.NONE
                last == Atom.COMPLEX && (maxRepeat == UNBOUNDED || maxRepeat > 1) -> safe = false
                else -> {
                    if (maxRepeat != minRepeat) frames.last().variable = true
                    val branch = frames.last().branch
                    if (last != Atom.NONE && branch.isNotEmpty()) {
                        val item = branch.removeAt(branch.lastIndex)
                        branch += Item(item.chars, minRepeat == 0 || item.canMatchEmpty, maxRepeat == UNBOUNDED)
                    }
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

    /**
     * Whether [items], a sequence, has two unbounded repeats over overlapping characters with only
     * items that can match nothing between them.
     */
    private fun hasAdjacentRepeats(items: List<Item>): Boolean {
        for (i in items.indices) {
            if (!items[i].unbounded) continue
            for (j in i + 1 until items.size) {
                if (items[j].unbounded && items[i].chars.overlaps(items[j].chars)) return true
                if (!items[j].canMatchEmpty) break
            }
        }
        return false
    }

    /** An escape sequence: where it ends, what it consumes, and the text of a `\Q...\E` quote. */
    private class Escape(
        val end: Int,
        val chars: CharSet,
        val exact: Boolean = true,
        val isAssertion: Boolean = false,
        val quoted: List<Int> = emptyList(),
    ) {
        /** The one code point the escape stands for, which can bound a range in a class. */
        val single: Int? get() = chars.ranges.singleOrNull()?.takeIf { exact && it.first == it.last }?.first
    }

    /** Reads what escapes and character classes consume, under the pattern's flags. */
    private class CharReader(
        private val pattern: String,
        private val foldCase: Boolean,
        private val unicodeClasses: Boolean,
    ) {

        /** An item that consumes one of [chars], with the case-insensitive flag applied. */
        fun item(chars: CharSet): Item = Item(fold(chars), canMatchEmpty = false)

        /** [chars] with what the case-insensitive flag adds: other cases and, generously, non-ASCII. */
        fun fold(chars: CharSet): CharSet {
            if (!foldCase) return chars
            var result = chars
            for (upper in 'A'.code..'Z'.code) {
                val lower = upper + ('a' - 'A')
                if (chars.contains(upper) || chars.contains(lower)) {
                    result = result.union(CharSet.single(upper)).union(CharSet.single(lower))
                }
            }
            val hasNonAscii = chars.overlaps(NON_ASCII)
            if (hasNonAscii || chars.overlaps(ASCII_LETTERS)) result = result.union(NON_ASCII)
            if (hasNonAscii) result = result.union(ASCII_LETTERS)
            return result
        }

        /** The escape whose `\` is at [start]. */
        @Suppress("CyclomaticComplexMethod")
        fun escape(start: Int): Escape {
            val next = pattern.getOrNull(start + 1) ?: return Escape(start + 1, CharSet.ALL, exact = false)
            return when (next) {
                'd' -> predefined(start, DIGIT, positive = true)
                'D' -> predefined(start, DIGIT, positive = false)
                'w' -> predefined(start, WORD, positive = true)
                'W' -> predefined(start, WORD, positive = false)
                's' -> predefined(start, SPACE, positive = true)
                'S' -> predefined(start, SPACE, positive = false)
                'h' -> Escape(start + 2, HORIZONTAL_SPACE)
                'H' -> Escape(start + 2, HORIZONTAL_SPACE.complement())
                'v', 'R' -> Escape(start + 2, VERTICAL_SPACE)
                'V' -> Escape(start + 2, VERTICAL_SPACE.complement())
                'b', 'B', 'A', 'z', 'Z', 'G' -> Escape(start + 2, CharSet.EMPTY, isAssertion = true)
                't' -> single(start + 2, '\t'.code)
                'n' -> single(start + 2, '\n'.code)
                'r' -> single(start + 2, '\r'.code)
                'f' -> single(start + 2, FORM_FEED)
                'a' -> single(start + 2, BELL)
                'e' -> single(start + 2, ESCAPE)
                'c' -> pattern.getOrNull(start + 2)
                    ?.let { single(start + CONTROL_ESCAPE_LENGTH, it.code xor CONTROL_BIT) }
                    ?: Escape(pattern.length, CharSet.ALL, exact = false)
                'x', 'u', '0' -> numeric(start, next)
                'Q' -> quote(start)
                'p', 'P', 'N' -> Escape(skipBraced(start), CharSet.ALL, exact = false)
                else -> if (next.isLetterOrDigit()) {
                    Escape(start + 2, CharSet.ALL, exact = false)
                } else {
                    val codePoint = pattern.codePointAt(start + 1)
                    single(start + 1 + Character.charCount(codePoint), codePoint)
                }
            }
        }

        private fun single(end: Int, codePoint: Int) = Escape(end, CharSet.single(codePoint))

        // `\d`, `\w` and `\s` (or their complements): with the Unicode flag they reach beyond ASCII.
        private fun predefined(start: Int, ascii: CharSet, positive: Boolean): Escape = Escape(
            start + 2,
            when {
                !positive -> ascii.complement()
                unicodeClasses -> ascii.union(NON_ASCII)
                else -> ascii
            },
        )

        // `\xhh`, `\x{h...}`, `\uhhhh` and `\0ooo`. A surrogate half counts as any character.
        private fun numeric(start: Int, kind: Char): Escape {
            val digits = when {
                kind == 'x' && pattern.getOrNull(start + 2) == '{' -> {
                    val end = skipBraced(start)
                    val digitsStart = start + BRACED_HEX_PREFIX_LENGTH
                    return pattern.substring(digitsStart, maxOf(digitsStart, end - 1)).toIntOrNull(HEX_RADIX)
                        ?.takeIf { it <= CharSet.MAX_CODE_POINT && !isSurrogate(it) }
                        ?.let { single(end, it) }
                        ?: Escape(end, CharSet.ALL, exact = false)
                }
                kind == 'x' -> pattern.substring(start + 2, minOf(pattern.length, start + 2 + HEX_DIGITS))
                kind == 'u' -> pattern.substring(start + 2, minOf(pattern.length, start + 2 + UNICODE_DIGITS))
                else -> pattern.drop(start + 2).take(OCTAL_DIGITS).takeWhile { it in '0'..'7' }
            }
            val end = start + 2 + digits.length
            val value = if (kind == '0') digits.toIntOrNull(OCTAL_RADIX) else digits.toIntOrNull(HEX_RADIX)
            return value?.takeIf { !isSurrogate(it) }?.let { single(end, it) }
                ?: Escape(end, CharSet.ALL, exact = false)
        }

        private fun isSurrogate(codePoint: Int) = codePoint in Char.MIN_SURROGATE.code..Char.MAX_SURROGATE.code

        // `\Q...\E`: its characters are literals.
        private fun quote(start: Int): Escape {
            val close = pattern.indexOf("\\E", start + 2)
            val text = pattern.substring(start + 2, if (close < 0) pattern.length else close)
            val points = buildList {
                var offset = 0
                while (offset < text.length) {
                    val point = text.codePointAt(offset)
                    add(point)
                    offset += Character.charCount(point)
                }
            }
            val chars = points.fold(CharSet.EMPTY) { set, point -> set.union(CharSet.single(point)) }
            return Escape(if (close < 0) pattern.length else close + 2, chars, quoted = points)
        }

        // The index after an escape such as `\p{L}`, `\pL`, `\N{name}` or `\x{1F600}`.
        private fun skipBraced(start: Int): Int = if (pattern.getOrNull(start + 2) == '{') {
            pattern.indexOf('}', start + 2).let { if (it < 0) pattern.length else it + 1 }
        } else {
            minOf(pattern.length, start + CONTROL_ESCAPE_LENGTH)
        }

        /** What the class whose `[` is at [start] consumes, and the index after it. */
        fun characterClass(start: Int): Pair<CharSet, Int> {
            val (chars, end) = classAt(start)
            return chars.set to end
        }

        private class Chars(val set: CharSet, val exact: Boolean)

        // A class, nested ones and `&&` intersections included, before the case-insensitive flag
        // applies. An inexact part makes a complement count as any character, so the result never
        // misses a character the class can consume.
        private fun classAt(start: Int): Pair<Chars, Int> {
            var index = start + 1
            val negated = pattern.getOrNull(index) == '^'
            if (negated) index++
            val union = classUnion(index)
            var chars = union.chars
            var compound = union.nested
            index = union.end
            while (pattern.startsWith("&&", index)) {
                val other = classUnion(index + 2)
                chars = Chars(fold(chars.set).intersect(fold(other.chars.set)), chars.exact && other.chars.exact)
                compound = true
                index = other.end
            }
            if (index >= pattern.length) return Chars(CharSet.ALL, exact = false) to pattern.length
            return when {
                !negated -> chars
                compound || !chars.exact -> Chars(CharSet.ALL, exact = false)
                else -> Chars(chars.set.complement(), exact = true)
            } to index + 1
        }

        private class Union(val chars: Chars, val nested: Boolean, val end: Int)

        // Class members up to its `]` or a `&&`.
        private fun classUnion(from: Int): Union {
            var index = from
            var set = CharSet.EMPTY
            var exact = true
            var nested = false
            // A `]` right after the opening `[` or `[^` is a literal.
            var first = pattern.getOrNull(index - 1) == '[' || pattern.getOrNull(index - 1) == '^'
            while (index < pattern.length && (first || !endsUnion(index))) {
                first = false
                if (pattern[index] == '[') {
                    val (inner, end) = classAt(index)
                    set = set.union(inner.set)
                    exact = exact && inner.exact
                    nested = true
                    index = end
                    continue
                }
                val (member, end) = classMember(index)
                set = set.union(member.set)
                exact = exact && member.exact
                index = end
            }
            return Union(Chars(set, exact), nested, index)
        }

        private fun endsUnion(index: Int): Boolean = pattern[index] == ']' || pattern.startsWith("&&", index)

        // A literal, escape or range inside a class.
        private fun classMember(start: Int): Pair<Chars, Int> {
            val (from, afterFrom) = classAtom(start)
            val dash = pattern.getOrNull(afterFrom) == '-'
            val bound = pattern.getOrNull(afterFrom + 1)
            if (!dash || bound in NOT_RANGE_BOUNDS) return Chars(from.chars, from.exact) to afterFrom
            val (to, afterTo) = classAtom(afterFrom + 1)
            val low = from.single
            val high = to.single
            return if (low != null && high != null && low <= high) {
                Chars(CharSet.of(low..high), exact = true) to afterTo
            } else {
                Chars(CharSet.ALL, exact = false) to afterTo
            }
        }

        private fun classAtom(start: Int): Pair<Escape, Int> {
            if (pattern[start] != '\\') {
                val codePoint = pattern.codePointAt(start)
                val end = start + Character.charCount(codePoint)
                return Escape(end, CharSet.single(codePoint)) to end
            }
            val escape = escape(start)
            val chars = if (escape.isAssertion) Escape(escape.end, CharSet.ALL, exact = false) else escape
            return chars to escape.end
        }
    }

    /** A set of code points, as sorted, disjoint, non-adjacent ranges. */
    private class CharSet private constructor(val ranges: List<IntRange>) {

        fun union(other: CharSet): CharSet = of(ranges + other.ranges)

        fun intersect(other: CharSet): CharSet = complement().union(other.complement()).complement()

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

        fun contains(codePoint: Int): Boolean = ranges.any { codePoint in it }

        fun overlaps(other: CharSet): Boolean = ranges.any { range ->
            other.ranges.any { it.first <= range.last && range.first <= it.last }
        }

        companion object {
            const val MAX_CODE_POINT: Int = 0x10FFFF
            val EMPTY: CharSet = CharSet(emptyList())
            val ALL: CharSet = CharSet(listOf(0..MAX_CODE_POINT))

            fun single(codePoint: Int): CharSet = CharSet(listOf(codePoint..codePoint))

            fun of(vararg ranges: IntRange): CharSet = of(ranges.toList())

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

    private val DIGIT = CharSet.of('0'.code..'9'.code)
    private val ASCII_LETTERS = CharSet.of('A'.code..'Z'.code, 'a'.code..'z'.code)
    private val WORD = DIGIT.union(ASCII_LETTERS).union(CharSet.single('_'.code))

    @Suppress("MagicNumber")
    private val SPACE = CharSet.of(0x09..0x0D, 0x20..0x20)

    @Suppress("MagicNumber")
    private val HORIZONTAL_SPACE = CharSet.of(
        0x09..0x09, 0x20..0x20, 0xA0..0xA0, 0x1680..0x1680, 0x180E..0x180E,
        0x2000..0x200A, 0x202F..0x202F, 0x205F..0x205F, 0x3000..0x3000,
    )

    @Suppress("MagicNumber")
    private val VERTICAL_SPACE = CharSet.of(0x0A..0x0D, 0x85..0x85, 0x2028..0x2029)

    @Suppress("MagicNumber")
    private val NON_ASCII = CharSet.of(0x80..CharSet.MAX_CODE_POINT)

    private const val FORM_FEED = 0x0C
    private const val BELL = 0x07
    private const val ESCAPE = 0x1B
    private const val CONTROL_BIT = 0x40
    private const val HEX_RADIX = 16
    private const val OCTAL_RADIX = 8
    private const val HEX_DIGITS = 2
    private const val UNICODE_DIGITS = 4
    private const val OCTAL_DIGITS = 3

    /** The length of a control-character escape such as `\cM`, and of a one-letter property such as `\pL`. */
    private const val CONTROL_ESCAPE_LENGTH = 3

    private val LOOKBEHINDS = setOf('=', '!')

    /** What can't follow the `-` of a range in a class, which makes the `-` a literal. */
    private val NOT_RANGE_BOUNDS = setOf(null, ']', '[')

    /** The length of `\x{`. */
    private const val BRACED_HEX_PREFIX_LENGTH = 3

    /** The repeat limit of `*`, `+` and `{n,}`. */
    private const val UNBOUNDED = -1

    private val REPEAT = Regex("""\{(\d+)(?:,(\d*))?}""")

    /** Inline flags, as in `(?i)` or `(?iu:...)`; group 1 holds those turned on. */
    private val INLINE_FLAGS = Regex("""\(\?([a-zA-Z]*)(?:-[a-zA-Z]*)?[:)]""")
}
