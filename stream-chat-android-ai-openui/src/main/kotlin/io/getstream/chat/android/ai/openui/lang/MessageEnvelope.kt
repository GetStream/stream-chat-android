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

import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Splits a message stored in the OpenUI message protocol into its content, context, attributes and
 * end flag (port of `parseMessage`). While [isStreaming], a last line that may be a marker cut
 * mid-stream is held back.
 *
 * ```text
 * ]]>openui:content?library=chat
 * <openui-lang or text>
 * ]]>openui:context
 * [<json>]
 * ]]>openui:end
 * ```
 *
 * A regenerated response writes a new content line after the cut-off one; the last content wins.
 * Messages in the older `<content>`/`<context>` XML envelope still read.
 */
public fun parseMessage(raw: String, isStreaming: Boolean = false): ParsedMessage =
    MessageEnvelope.parse(raw, isStreaming)

/**
 * Writes a message in the OpenUI message protocol, with its marker lines (port of `buildMessage`).
 * The [context] is written as is when it is a string, else as JSON, and left out when `null`.
 */
public fun buildMessage(
    content: String,
    context: OpenUIValue? = null,
    attributes: Map<String, String> = emptyMap(),
    end: Boolean = false,
): String = MessageEnvelope.build(content, context, attributes, end)

/** The parts of a stored message (port of `ParsedMessage`). */
public data class ParsedMessage internal constructor(
    /** The body of the last `content` section, or the whole text when there is none. */
    val content: String,
    /** The body of the `context` section: parsed JSON, else the raw text. `null` when absent. */
    val context: OpenUIValue? = null,
    /** The string attributes of the winning `content` marker. */
    val attributes: Map<String, String> = emptyMap(),
    /** Whether an `end` line follows the winning `content` section. */
    val isEnded: Boolean = false,
)

/** The OpenUI message protocol (port of `message.ts`): marker lines around a stored response. */
internal object MessageEnvelope {
    private const val PREFIX = "]]>openui:"

    /** Starts the content section. */
    const val CONTENT_MARKER: String = PREFIX + "content"

    /** Starts the context section. */
    const val CONTEXT_MARKER: String = PREFIX + "context"

    /** Marks the end of a complete message. */
    const val END_MARKER: String = PREFIX + "end"

    /**
     * Splits a stored message into its content, context, attributes and end flag. While
     * [isStreaming], a last line that may be a marker cut mid-stream is held back.
     */
    fun parse(raw: String, isStreaming: Boolean = false): ParsedMessage {
        val (message, hasMarkers) = parseMarkers(raw, isStreaming)
        if (hasMarkers) return message
        return parseLegacyXml(raw) ?: message
    }

    /**
     * Writes a stored message with its marker lines. The [context] is written as is when it is a
     * string, else as JSON, and left out when `null`.
     */
    fun build(
        content: String,
        context: OpenUIValue? = null,
        attributes: Map<String, String> = emptyMap(),
        end: Boolean = false,
    ): String {
        val query = attributes.entries.joinToString("&") { "${encode(it.key)}=${encode(it.value)}" }
        val message = StringBuilder(CONTENT_MARKER)
        if (query.isNotEmpty()) message.append('?').append(query)
        message.append('\n').append(content)
        if (context != null && context != OpenUIValue.Null) {
            message.append('\n').append(CONTEXT_MARKER).append('\n')
                .append((context as? OpenUIValue.Str)?.value ?: OpenUIJson.stringify(context))
        }
        if (end) message.append('\n').append(END_MARKER)
        return message.toString()
    }

    /** Whether the text contains recognizable OpenUI Lang (a fenced block or a top-level `root =`). */
    fun hasLangSyntax(content: String?): Boolean {
        if (content.isNullOrEmpty()) return false
        if ("```openui-lang" in content) return true
        return rootStatement.containsMatchIn(content)
    }

    // Markers --------------------------------------------------------------------------------

    private class Section(val kind: String?, val attributes: Map<String, String> = emptyMap()) {
        val lines = ArrayList<String>()
    }

    private val rootStatement = Regex("(^|\n)\\s*root\\s*=")

    // A regenerated response writes its content line right after the cut-off text, with no
    // newline before it. Start it on its own line so the last content wins.
    private val retry = Regex("([^\n])(?=\\]\\]>openui:content(?:\\?|\r?\n|\\z))")

    // While streaming, hold back a mid-line `]]>openui:con` that may become a content line.
    private val retryCut = Regex("[^\n](\\]\\]>[a-z:]*)\\z")
    private val marker = Regex("^\\]\\]>openui:([a-z]+)(?:\\?(.*))?\\z")

    @Suppress("LongMethod")
    private fun parseMarkers(raw: String, isStreaming: Boolean): Pair<ParsedMessage, Boolean> {
        var text = retry.replace(raw, "$1\n")
        if (isStreaming) {
            val cut = retryCut.find(text)
            if (cut != null && CONTENT_MARKER.startsWith(cut.groupValues[1])) {
                text = text.substring(0, cut.range.first + 1)
            }
        }
        // Split on the line feed code unit, as JavaScript does.
        val lines = text.split("\n").toMutableList()
        val last = lines.last()
        val isHeld = isStreaming && last.isNotEmpty() && (last.startsWith(PREFIX) || PREFIX.startsWith(last))
        if (isHeld) lines.removeAt(lines.lastIndex)

        val sections = arrayListOf(Section(kind = null))
        var hasMarkers = false

        // The newline (and its `\r`) before a marker is not part of the body.
        fun close() {
            val body = sections.last().lines
            if (body.isNotEmpty()) body[body.lastIndex] = dropCarriageReturn(body.last())
        }
        var endAt = -1
        for ((index, line) in lines.withIndex()) {
            val match = marker.find(dropCarriageReturn(line))
            val kind = match?.groupValues?.get(1)
            when {
                match == null -> sections.last().lines += line
                kind == "end" -> {
                    hasMarkers = true
                    close()
                    endAt = sections.lastIndex
                    // A final end line ends the message; a trailing newline after it is not content.
                    if (index == lines.size - 2 && lines[index + 1].isEmpty()) break
                }
                else -> {
                    hasMarkers = true
                    close()
                    sections += Section(kind, readAttributes(match.groupValues[2]))
                }
            }
        }
        if (isHeld) close()

        // Last content wins; text before the first marker is content only when there is none.
        val kinds = sections.map { it.kind }
        val winner = maxOf(0, kinds.lastIndexOf("content"))
        val contextIndex = kinds.lastIndexOf("context")
        val context = if (contextIndex > winner) {
            val body = sections[contextIndex].lines.joinToString("\n")
            OpenUIJson.parseOrNull(body) ?: OpenUIValue.Str(body)
        } else {
            null
        }
        val message = ParsedMessage(
            content = sections[winner].lines.joinToString("\n"),
            context = context,
            attributes = sections[winner].attributes,
            // Like context, an `end` before the winning content belongs to an earlier attempt.
            isEnded = endAt >= winner,
        )
        return message to hasMarkers
    }

    private fun dropCarriageReturn(line: String): String = line.removeSuffix("\r")

    private fun readAttributes(query: String): Map<String, String> {
        val attributes = LinkedHashMap<String, String>()
        for (pair in query.split("&")) {
            val parts = pair.split("=")
            val key = decode(parts[0])
            if (key.isEmpty()) continue
            attributes[key] = if (parts.size > 1) decode(parts.drop(1).joinToString("=")) else ""
        }
        return attributes
    }

    // Same bytes as URLSearchParams: unreserved characters stay, a space is "+".
    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())

    private fun decode(value: String): String = try {
        URLDecoder.decode(value, Charsets.UTF_8.name())
    } catch (_: IllegalArgumentException) {
        // A malformed escape reads as is, with `+` as a space.
        value.replace('+', ' ')
    }

    // Legacy ---------------------------------------------------------------------------------

    private val legacyContext = Regex("<context>([\\s\\S]*)</context>\\s*$")
    private val legacyContent = Regex("^<content[^>]*>([\\s\\S]*)</content>\\s*$")

    private fun parseLegacyXml(raw: String): ParsedMessage? {
        if ("<content" !in raw && "<context>" !in raw) return null
        var content = raw
        var context: OpenUIValue? = null
        legacyContext.find(raw)?.let { match ->
            val inner = match.groupValues[1]
            context = OpenUIJson.parseOrNull(inner) ?: OpenUIValue.Str(inner)
            content = JS.trimEnd(raw.substring(0, match.range.first))
        }
        legacyContent.find(content)?.let { content = it.groupValues[1] }
        return ParsedMessage(content = content, context = context)
    }
}
