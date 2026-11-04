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
 * [Preprocessor.preprocess] of text that only grows at the end. Each [append] reads the
 * characters added since the last one and redoes only the last line, which later characters can
 * still change; completed lines are never processed again.
 */
internal class IncrementalPreprocessor {
    private val output = StringBuilder()

    /**
     * The preprocessed text is `text[0, length)`. Everything up to its last line break stays the
     * same as text is appended, unless [append] reports that it started over.
     */
    val text: CharSequence get() = output

    /** Where the preprocessed text ends: trailing whitespace past it is not part of it. */
    var length = 0
        private set

    /** How many characters all appends have read, which grows with the text, not with its square. */
    var charsRead = 0
        private set

    private val fences = FenceStripper()
    private var comments = CommentStripper()

    // How many characters of `output` won't change, and where the last non-whitespace among them ends.
    private var committed = 0
    private var committedContentEnd = 0
    private var keptContent = false

    /**
     * Appends `raw[start, end)`. Returns `true` when the text so far reads differently, which a
     * fence after unfenced text causes: only what is inside fences is kept from then on.
     */
    fun append(raw: CharSequence, start: Int = 0, end: Int = raw.length): Boolean {
        val stripped = fences.feed(raw, start, end)
        val startedOver = fences.startedOver
        if (startedOver) {
            comments = CommentStripper()
            output.setLength(0)
            committed = 0
            committedContentEnd = 0
            keptContent = false
        }
        output.setLength(committed)
        commit(comments.feed(stripped))
        val lastLine = trimmedStart(comments.lastLine(followedBy = fences.undecided))
        output.append(lastLine)
        length = contentEnd(lastLine)?.let { committed + it } ?: committedContentEnd
        charsRead += fences.read + comments.read
        return startedOver
    }

    private fun commit(lines: String) {
        val kept = trimmedStart(lines)
        if (kept.isEmpty()) return
        keptContent = true
        contentEnd(kept)?.let { committedContentEnd = committed + it }
        output.append(kept)
        committed += kept.length
    }

    // Leading whitespace is trimmed until the first character of content is kept.
    private fun trimmedStart(text: String): String = if (keptContent) text else JS.trimStart(text)

    // Where the last character that isn't whitespace ends, or null when there is none.
    private fun contentEnd(text: String): Int? {
        var end = text.length
        while (end > 0 && JS.isWhitespace(text[end - 1])) end--
        return if (end == 0) null else end
    }
}

/**
 * [Preprocessor.stripFences], one character at a time: before any fence the text is kept as it
 * is, and once one is found only the text inside fences is.
 */
private class FenceStripper {
    private enum class Place { Outside, Language, Content }

    private enum class Quote { None, Open, Escaped }

    private var fenced = false
    private var place = Place.Outside
    private var quote = Quote.None
    private var blocks = 0

    // One or two backticks at the end, which a fence needs the next characters to tell apart.
    private var pending = ""

    /** Whether the last [feed] found the first fence, which drops everything kept before it. */
    var startedOver = false
        private set

    /** How many characters the last [feed] read. */
    var read = 0
        private set

    /**
     * What the undecided backticks read as if the text ended here: text, unless they are outside
     * fences, which drop text.
     */
    val undecided: String get() = if (!fenced || place == Place.Content) pending else ""

    @Suppress("LoopWithTooManyJumpStatements", "NestedBlockDepth")
    fun feed(raw: CharSequence, start: Int, end: Int): String {
        val input = StringBuilder(pending.length + end - start).append(pending).append(raw, start, end)
        pending = ""
        startedOver = false
        read = input.length
        val result = StringBuilder()
        var index = 0
        while (index < input.length) {
            val c = input[index]
            if (place == Place.Language) {
                if (c == '\n') {
                    place = Place.Content
                    quote = Quote.None
                }
                index++
                continue
            }
            if (quote == Quote.None && c == '`') {
                val isFence = fenceAt(input, index)
                if (isFence == null) {
                    pending = input.substring(index)
                    break
                }
                if (isFence) {
                    if (!fenced) {
                        fenced = true
                        startedOver = true
                        result.setLength(0)
                    }
                    if (place == Place.Content) {
                        place = Place.Outside
                    } else {
                        blocks++
                        if (blocks > 1) result.append('\n')
                        place = Place.Language
                    }
                    index += FENCE_LENGTH
                    continue
                }
            }
            quote = when (quote) {
                Quote.Escaped -> Quote.Open
                Quote.Open -> when (c) {
                    '\\' -> Quote.Escaped
                    '"' -> Quote.None
                    else -> Quote.Open
                }
                Quote.None -> if (c == '"') Quote.Open else Quote.None
            }
            if (!fenced || place == Place.Content) result.append(c)
            index++
        }
        return result.toString()
    }

    private companion object {
        const val FENCE_LENGTH = 3

        // Whether three backticks start at `index`, or null when the text ends before that's known.
        fun fenceAt(input: CharSequence, index: Int): Boolean? {
            if (index + 1 < input.length && input[index + 1] != '`') return false
            if (index + 2 >= input.length) return null
            return input[index + 2] == '`'
        }
    }
}

/** [Preprocessor.stripComments], a line at a time. */
private class CommentStripper {
    // The string open where the last line starts, and that line so far.
    private var quote: Char? = null
    private val line = StringBuilder()

    /** How many characters were stripped since the last [feed]. */
    var read = 0
        private set

    /** The lines [input] completes, each with its line break. */
    fun feed(input: String): String {
        read = 0
        val result = StringBuilder()
        var start = 0
        for (index in input.indices) {
            if (input[index] != '\n') continue
            line.append(input, start, index)
            result.append(strip(line)).append('\n')
            line.setLength(0)
            start = index + 1
        }
        line.append(input, start, input.length)
        return result.toString()
    }

    /** The last line, which no line break ends yet, as it reads if [followedBy] ends the text. */
    fun lastLine(followedBy: String): String {
        val open = quote
        val stripped = strip(if (followedBy.isEmpty()) line else StringBuilder(line).append(followedBy))
        quote = open
        return stripped
    }

    @Suppress("LoopWithTooManyJumpStatements")
    private fun strip(line: CharSequence): String {
        read += line.length
        var index = 0
        while (index < line.length) {
            val c = line[index]
            val open = quote
            if (open != null) {
                if (c == '\\' && index + 1 < line.length) {
                    index += 2
                    continue
                }
                if (c == open) quote = null
                index++
                continue
            }
            if (c == '"' || c == '\'') {
                quote = c
            } else if (c == '#' || (c == '/' && line.getOrNull(index + 1) == '/')) {
                return JS.trimEnd(line.substring(0, index))
            }
            index++
        }
        return line.toString()
    }
}
