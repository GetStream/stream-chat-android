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

package io.getstream.chat.android.ai.compose.ui.component.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import io.getstream.chat.android.ai.compose.R
import java.util.Locale
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToLong

/** One paragraph of reasoning, identified by its position. */
internal data class ReasoningParagraph(val id: Int, val text: String) {

    companion object {
        /**
         * Splits reasoning at blank lines. Reasoning only grows at its end, so a paragraph's
         * position is a stable identity.
         */
        fun split(text: String): List<ReasoningParagraph> = text.split("\n\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapIndexed { index, paragraph -> ReasoningParagraph(index, paragraph) }

        /**
         * Inline Markdown as reasoning models write it: bold, italics, code and links. A heading
         * reads as a bold line, since a paragraph of thinking has no document to structure.
         * Markers that never close are shown as written.
         */
        fun annotated(paragraph: String): AnnotatedString {
            val source = paragraph.split("\n").joinToString("\n") { line ->
                val trimmed = line.trimStart(' ')
                if (!trimmed.startsWith("#")) return@joinToString line
                val heading = trimmed.trimStart('#').trim()
                if (heading.isEmpty()) line else "**$heading**"
            }
            return buildAnnotatedString { InlineMarkdown(source, this).appendAll() }
        }
    }
}

/** A small inline Markdown reader: `**bold**`, `__bold__`, `*italic*`, `_italic_`, `code` and links. */
private class InlineMarkdown(private val text: String, private val out: AnnotatedString.Builder) {

    fun appendAll() = append(0, text.length)

    private fun append(start: Int, end: Int) {
        var index = start
        var plainStart = start
        while (index < end) {
            // Plain text so far goes out first, since a span writes straight to the output.
            if (index > plainStart && startsSpan(index)) {
                out.append(text, plainStart, index)
                plainStart = index
            }
            val consumed = span(index, end)
            if (consumed != null) {
                index = consumed
                plainStart = consumed
            } else {
                index++
            }
        }
        if (plainStart < end) out.append(text, plainStart, end)
    }

    private fun startsSpan(index: Int): Boolean = text[index] in "`[*_"

    /** Appends the span starting at [index], returning where it ends, or `null` if none starts there. */
    private fun span(index: Int, end: Int): Int? = when {
        text.startsWith("`", index) -> code(index, end)
        text.startsWith("[", index) -> link(index, end)
        text.startsWith("**", index) || text.startsWith("__", index) ->
            emphasis(index, end, text.substring(index, index + "**".length))
        text.startsWith("*", index) -> emphasis(index, end, "*")
        text.startsWith("_", index) && (index == 0 || !text[index - 1].isLetterOrDigit()) -> emphasis(index, end, "_")
        else -> null
    }

    private fun code(index: Int, end: Int): Int? {
        // An empty pair of backticks is not code.
        val close = text.indexOf('`', index + 1).takeIf { it > index + 1 && it < end } ?: return null
        out.withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) { append(text.substring(index + 1, close)) }
        return close + 1
    }

    private fun link(index: Int, end: Int): Int? {
        val middle = text.indexOf("](", index + 1).takeIf { it in (index + 1) until end } ?: return null
        val urlStart = middle + "](".length
        val close = text.indexOf(')', urlStart).takeIf { it > urlStart && it < end } ?: return null
        val url = text.substring(urlStart, close)
        if (url.any { it.isWhitespace() }) return null
        out.withLink(LinkAnnotation.Url(url, TextLinkStyles(SpanStyle(textDecoration = TextDecoration.Underline)))) {
            append(index + 1, middle)
        }
        return close + 1
    }

    private fun emphasis(index: Int, end: Int, marker: String): Int? {
        val contentStart = index + marker.length
        if (contentStart >= end || text[contentStart].isWhitespace()) return null
        val close = closingMarker(contentStart + 1, end, marker) ?: return null
        val style = if (marker.length == 1) {
            SpanStyle(fontStyle = FontStyle.Italic)
        } else {
            SpanStyle(fontWeight = FontWeight.SemiBold)
        }
        out.withStyle(style) { append(contentStart, close) }
        return close + marker.length
    }

    /** Where [marker] closes: after text that doesn't end in a space, and not as half of a double marker. */
    private fun closingMarker(from: Int, end: Int, marker: String): Int? {
        var search = from
        while (true) {
            val close = text.indexOf(marker, search).takeIf { it >= 0 && it + marker.length <= end } ?: return null
            if (!text[close - 1].isWhitespace() && !isHalfOfDouble(close, marker)) return close
            search = close + 1
        }
    }

    private fun isHalfOfDouble(index: Int, marker: String): Boolean =
        marker.length == 1 && (text.startsWith(marker + marker, index) || text.getOrNull(index - 1) == marker[0])
}

/**
 * Reveals new text a little at a time, so thoughts that arrive in bursts read as a steady stream.
 * Text that does not carry on from what is shown replaces it at once.
 */
internal class TextReveal(initial: String = "") {

    /** The text shown so far. */
    var shown: String by mutableStateOf(initial)
        private set

    /** Whether text is waiting to be revealed; call [tick] about every [TICK_MILLIS] meanwhile. */
    var revealing: Boolean by mutableStateOf(false)
        private set

    private var target = initial
    private val pending = StringBuilder()

    fun update(text: String, animated: Boolean) {
        if (!animated || text.length <= target.length || !text.startsWith(target)) {
            if (text != shown || pending.isNotEmpty()) show(text)
            return
        }
        pending.append(text, target.length, text.length)
        target = text
        revealing = true
    }

    /** Shows a sixth of the backlog at a time: new thoughts arrive about that often. */
    fun tick() {
        if (pending.isEmpty()) {
            revealing = false
            return
        }
        var count = max(1, pending.length / BACKLOG_SHARE)
        // Never split a character made of two UTF-16 units.
        if (count < pending.length && Character.isHighSurrogate(pending[count - 1])) count++
        shown += pending.substring(0, count)
        pending.delete(0, count)
    }

    private fun show(text: String) {
        pending.setLength(0)
        target = text
        shown = text
        revealing = false
    }

    companion object {
        /** How often new text is revealed: 30 times a second. */
        const val TICK_MILLIS: Long = 33
        private const val BACKLOG_SHARE = 6
    }
}

/** The words of the reasoning header, read from resources so they can be translated. */
internal data class ReasoningStrings(
    val thinking: String,
    /** The format of "Thinking… 7s", with the duration as its argument. */
    val thinkingFor: String,
    val thought: String,
    /** The format of "Thought for 12s", with the duration as its argument. */
    val thoughtFor: String,
    val duration: DurationStrings,
)

/** The units of a short duration, read from resources so they can be translated. */
internal data class DurationStrings(
    /** The format of "7s", with the whole seconds as its argument. */
    val seconds: String,
    /** The format of "2m", with the whole minutes as its argument. */
    val minutes: String,
    /** The format of "1m 5s", with the minutes and the seconds as its arguments. */
    val minutesSeconds: String,
    /** The format of "0.4s", with the seconds, already formatted, as its argument. */
    val fraction: String,
    /** Formats the numbers, including the decimal separator. */
    val locale: Locale,
)

/** The duration units of the current resources and locale. */
@Composable
internal fun rememberDurationStrings(): DurationStrings {
    val seconds = stringResource(R.string.stream_ai_compose_duration_seconds)
    val minutes = stringResource(R.string.stream_ai_compose_duration_minutes)
    val minutesSeconds = stringResource(R.string.stream_ai_compose_duration_minutes_seconds)
    val fraction = stringResource(R.string.stream_ai_compose_duration_fraction)
    val locale = Locale.getDefault()
    return remember(seconds, minutes, minutesSeconds, fraction, locale) {
        DurationStrings(seconds, minutes, minutesSeconds, fraction, locale)
    }
}

/** "Thinking…" while the model thinks, then how long it thought. */
internal fun reasoningTitle(isThinking: Boolean, durationSeconds: Double?, strings: ReasoningStrings): String = when {
    isThinking -> strings.thinking
    durationSeconds == null -> strings.thought
    else -> strings.thoughtFor.format(formatSeconds(floor(max(1.0, durationSeconds)).toLong(), strings.duration))
}

/** "Thinking…", then "Thinking… 7s" once a second has passed. */
internal fun thinkingTitle(elapsedSeconds: Double, strings: ReasoningStrings): String =
    if (elapsedSeconds < 1) {
        strings.thinking
    } else {
        strings.thinkingFor.format(formatSeconds(floor(elapsedSeconds).toLong(), strings.duration))
    }

/** A tool call's duration: "0.4s" under a second, then "12s" or "1m 5s". */
internal fun formatToolDuration(seconds: Double, strings: DurationStrings): String =
    if (seconds < 1) {
        String.format(strings.locale, strings.fraction, String.format(strings.locale, "%.1f", seconds))
    } else {
        formatSeconds(seconds.roundToLong(), strings)
    }

/** Whole seconds in the narrow form: "7s", "1m 5s", "2m". */
internal fun formatSeconds(seconds: Long, strings: DurationStrings): String {
    val minutes = seconds / SecondsPerMinute
    val rest = seconds % SecondsPerMinute
    return when {
        minutes == 0L -> String.format(strings.locale, strings.seconds, rest)
        rest == 0L -> String.format(strings.locale, strings.minutes, minutes)
        else -> String.format(strings.locale, strings.minutesSeconds, minutes, rest)
    }
}

private const val SecondsPerMinute = 60L
