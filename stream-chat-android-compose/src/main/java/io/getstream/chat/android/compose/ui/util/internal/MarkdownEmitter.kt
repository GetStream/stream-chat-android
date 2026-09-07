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

package io.getstream.chat.android.compose.ui.util.internal

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.TextUnit

/**
 * Collects text, styles and annotations while the markdown tree is walked, then assembles them into
 * an [AnnotatedString].
 *
 * Styles are recorded against offsets rather than pushed and popped, because block constructs are
 * styled only once their whole content has been emitted.
 */
internal class MarkdownEmitter {

    private val text = StringBuilder()
    private var linePrefix = ""
    private val spanStyles = mutableListOf<AnnotatedString.Range<SpanStyle>>()
    private val annotations = mutableListOf<AnnotatedString.Range<String>>()
    private val paragraphBreaks = mutableListOf<Int>()
    private val indentMarks = mutableListOf(0 to TextUnit.Unspecified)

    val length: Int get() = text.length

    fun append(value: CharSequence) {
        text.append(value)
    }

    /**
     * Ends the current line and opens the next with [linePrefix], so a construct marking every one
     * of its lines keeps doing so. Every call breaks, so two breaks in the source stay two.
     */
    fun appendLineBreak() {
        openLine()
    }

    val currentLinePrefix: String get() = linePrefix

    /**
     * Whether lines currently open with a prefix. While they do, blocks stay separated by
     * characters, so the prefix reaches the blank line between two of them; a paragraph break
     * would leave that line bare and cut the marker in two.
     */
    private val linesArePrefixed: Boolean get() = linePrefix.isNotEmpty()

    /**
     * Hangs every wrapped line [block] emits by [indent], so a line too long for the width keeps
     * clear of the marker its first line carries.
     */
    fun withHangingIndent(indent: TextUnit, block: () -> Unit) {
        val previous = indentMarks.last().second
        indentMarks += length to indent
        try {
            block()
        } finally {
            indentMarks += length to previous
        }
    }

    /** Marks every line [block] emits with [prefix], as a block quote marks its whole span. */
    fun withLinePrefix(prefix: String, block: () -> Unit) {
        val previous = linePrefix
        linePrefix = prefix
        try {
            block()
        } finally {
            linePrefix = previous
        }
    }

    private fun openLine() {
        text.append('\n').append(linePrefix)
    }

    fun addSpan(style: SpanStyle, start: Int, end: Int = length) {
        if (end > start) spanStyles += AnnotatedString.Range(style, start, end)
    }

    fun addAnnotation(tag: String, value: String, start: Int, end: Int = length) {
        if (end > start) annotations += AnnotatedString.Range(value, start, end, tag)
    }

    /**
     * Separates the block just emitted from the next with [newlines] breaks, counting those already
     * present. Does nothing while the output is empty, so it never starts with a blank line.
     *
     * A paragraph break ends its paragraph on a line feed, which renders as a blank line, so it
     * stands in for one of the breaks rather than being added on top of them.
     */
    fun endBlock(newlines: Int) {
        if (text.isEmpty()) return
        val breaksParagraph = newlines >= ParagraphSeparation && !linesArePrefixed
        val literal = if (breaksParagraph) newlines - 1 else newlines
        var present = 0
        var end = text.length
        val opening = "\n$linePrefix"
        while (end >= opening.length && text.substring(end - opening.length, end) == opening) {
            present++
            end -= opening.length
        }
        repeat((literal - present).coerceAtLeast(0)) { openLine() }
        // Before the prefix, so the prefix opens the next paragraph instead of closing the last.
        if (breaksParagraph) paragraphBreaks += text.length - linePrefix.length
    }

    /** Drops trailing blank lines, so a trailing block separator does not pad the bubble. */
    fun trimTrailingNewlines() {
        val opening = "\n$linePrefix"
        while (text.isNotEmpty()) {
            when {
                text.endsWith(opening) -> text.setLength(text.length - opening.length)
                text.last() == '\n' -> text.setLength(text.length - 1)
                else -> break
            }
        }
        clamp(spanStyles)
        clamp(annotations)
        paragraphBreaks.retainAll { it in 1 until text.length }
    }

    private fun <T> clamp(ranges: MutableList<AnnotatedString.Range<T>>) {
        val limit = text.length
        for (index in ranges.indices.reversed()) {
            val range = ranges[index]
            when {
                range.start >= limit -> ranges.removeAt(index)
                range.end > limit -> ranges[index] =
                    AnnotatedString.Range(range.item, range.start, limit, range.tag)
            }
        }
    }

    fun build(): AnnotatedString = buildAnnotatedString {
        append(text.toString())
        paragraphStyles().forEach { addStyle(it.item, it.start, it.end) }
        spanStyles.forEach { addStyle(it.item, it.start, it.end) }
        annotations.forEach { addStringAnnotation(it.tag, it.item, it.start, it.end) }
    }

    /**
     * One style per paragraph, covering the whole output, carrying the indent in force where the
     * paragraph starts. The ranges have to be contiguous and complete, because their edges are the
     * paragraph breaks that [endBlock] counted on for separation.
     */
    private fun paragraphStyles(): List<AnnotatedString.Range<ParagraphStyle>> {
        if (text.isEmpty()) return emptyList()
        val bounds = (listOf(0) + paragraphBreaks.sorted().distinct() + text.length)
        return bounds.zipWithNext { start, end ->
            AnnotatedString.Range(ParagraphStyle(textIndent = indentAt(start)), start, end)
        }
    }

    private fun indentAt(offset: Int): TextIndent? = indentMarks
        .lastOrNull { it.first <= offset }
        ?.second
        ?.takeIf { it != TextUnit.Unspecified }
        ?.let { TextIndent(restLine = it) }
}

/** Two breaks in the source is the author starting a new paragraph. */
private const val ParagraphSeparation = 2
