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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

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

    /** The offset the next emitted character will land on. */
    val length: Int get() = text.length

    fun append(value: CharSequence) {
        text.append(value)
    }

    /**
     * Appends a line break for a break inside a block, then re-opens the line with [linePrefix] so
     * a construct that marks every one of its lines keeps doing so. Consecutive breaks collapse,
     * because a hard break is spelled as a marker plus the newline it sits on and both reach the
     * walker.
     */
    fun appendLineBreak() {
        if (endsWithOpenLine()) return
        openLine()
    }

    /** The prefix every line currently opens with, so nested constructs can build on it. */
    val currentLinePrefix: String get() = linePrefix

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

    /** True when the text already ends on a freshly opened line, prefix included. */
    private fun endsWithOpenLine(): Boolean = text.endsWith("\n$linePrefix")

    fun addSpan(style: SpanStyle, start: Int, end: Int = length) {
        if (end > start) spanStyles += AnnotatedString.Range(style, start, end)
    }

    fun addAnnotation(tag: String, value: String, start: Int, end: Int = length) {
        if (end > start) annotations += AnnotatedString.Range(value, start, end, tag)
    }

    /**
     * Separates the block just emitted from the next one with [newlines] line breaks, counting the
     * ones already present so blocks never stack up extra blank lines. Does nothing while the
     * output is still empty, so the result never starts with a blank line.
     */
    fun endBlock(newlines: Int) {
        if (text.isEmpty()) return
        var present = 0
        var end = text.length
        val opening = "\n$linePrefix"
        while (end >= opening.length && text.substring(end - opening.length, end) == opening) {
            present++
            end -= opening.length
        }
        repeat((newlines - present).coerceAtLeast(0)) { openLine() }
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
        clampRangesToText()
    }

    /** Keeps recorded ranges inside the text after characters have been removed from the end. */
    private fun clampRangesToText() {
        clamp(spanStyles)
        clamp(annotations)
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
        spanStyles.forEach { addStyle(it.item, it.start, it.end) }
        annotations.forEach { addStringAnnotation(it.tag, it.item, it.start, it.end) }
    }
}
