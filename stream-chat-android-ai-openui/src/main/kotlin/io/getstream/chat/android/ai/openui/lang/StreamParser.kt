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
 * Creates an incremental parser for streamed responses in the library JSON Schema [schema]
 * (`library.toJSONSchema()`), port of `createStreamingParser`.
 *
 * Keep one per response and pass it the full text so far with [StreamParser.set], or each new
 * chunk with [StreamParser.push]; pass `complete = true` once the stream has ended.
 *
 * @param rootName The library's root component.
 */
public fun createStreamingParser(schema: OpenUIValue, rootName: String? = null): StreamParser =
    StreamParser(schema, rootName)

/**
 * Incremental parser for streamed responses (port of `StreamParser`); create one with
 * [createStreamingParser].
 *
 * Completed statements (ended by a depth-0 newline) are parsed once and cached; only the
 * trailing, still-growing statement is re-parsed on each update. A statement already completed
 * is replaced by a later definition only once that definition no longer needs auto-closing.
 *
 * Not thread-safe: feed it from one thread.
 *
 * @param paramMap Component params and calls compiled from the library schema
 * ([Parser.compileSchema]), or `null` to parse without a library.
 * @param rootName The library's root component.
 */
public class StreamParser internal constructor(
    internal val paramMap: ParamMap?,
    internal val rootName: String? = null,
) {
    /** A stream parser for the library JSON Schema [schema] (`library.toJSONSchema()`). */
    internal constructor(schema: OpenUIValue, rootName: String? = null) :
        this(Parser.compileSchema(schema), rootName)

    /** Raw accumulated input (kept for [set] diffing). */
    private var buffer = ""

    /**
     * Preprocessed view of [buffer]. The completed-statement scan runs over this, never the raw
     * buffer, so leading prose or fences (an apostrophe in "You're …") can't corrupt it.
     */
    private var cleaned = ""

    /** How far into [cleaned] statements are already completed. */
    private var completedEnd = 0
    private val completed = LinkedHashMap<String, Statement>()
    private var completedCount = 0

    /** Appends the next chunk and returns the latest result. Pass [complete] once the stream has ended. */
    public fun push(chunk: String, complete: Boolean = false): ParseResult {
        buffer += chunk
        return getResult(complete)
    }

    /**
     * Sets the full text so far. Only the delta is consumed; if the text was replaced rather
     * than appended to, the parser starts over. Pass [complete] once the stream has ended.
     */
    public fun set(fullText: String, complete: Boolean = false): ParseResult {
        if (fullText.length < buffer.length || !fullText.startsWith(buffer)) reset()
        buffer += fullText.substring(buffer.length)
        return getResult(complete)
    }

    /**
     * The latest result without consuming new data. Until [complete], a `$name` ending the text
     * may still be growing, so it isn't declared yet.
     */
    public fun getResult(complete: Boolean = false): ParseResult {
        refreshCleaned()
        val pendingStart = scanNewCompleted()
        val pendingText = JS.trim(cleaned.substring(pendingStart))

        if (pendingText.isEmpty()) {
            if (completedCount == 0) return ParseResult.empty()
            return Parser.buildResult(completed, false, completedCount, paramMap, rootName)
        }

        // `cleaned` is already preprocessed: just auto-close the trailing statement.
        val (closed, incomplete) = Statements.autoClose(pendingText)
        val pending = Statements.split(Lexer.tokenize(closed))
        if (pending.isEmpty()) {
            if (completedCount == 0) return ParseResult.empty(incomplete)
            return Parser.buildResult(completed, incomplete, completedCount, paramMap, rootName)
        }

        // New ids render progressively; completed ids are replaced only once the pending
        // definition no longer needs auto-closing.
        val all = LinkedHashMap(completed)
        val pendingIds = HashSet<String>()
        for (statement in pending) {
            if (statement.id in completed && incomplete) continue
            all[statement.id] = Parser.classify(statement, ExpressionParser.parse(statement.tokens))
            pendingIds += statement.id
        }
        val tail = if (complete) null else partialStateName(buffer, pendingText)
        return Parser.buildResult(
            all,
            incomplete,
            completedCount + pending.size,
            paramMap,
            rootName,
            tail?.let { PartialReference(it, pendingIds) },
        )
    }

    /** Clears all state. */
    public fun reset() {
        buffer = ""
        cleaned = ""
        completedEnd = 0
        completed.clear()
        completedCount = 0
    }

    private fun addStatement(text: String) {
        val trimmed = JS.trim(text)
        if (trimmed.isEmpty()) return
        for (statement in Statements.split(Lexer.tokenize(trimmed))) {
            completed[statement.id] = Parser.classify(statement, ExpressionParser.parse(statement.tokens))
            completedCount++
        }
    }

    /**
     * Recomputes [cleaned]. If the completed prefix is no longer a prefix of the new cleaned text
     * (e.g. an opening fence just appeared), the cache is stale and is rebuilt.
     */
    private fun refreshCleaned() {
        val next = Preprocessor.preprocess(buffer)
        if (!next.startsWith(cleaned.substring(0, completedEnd))) {
            completedEnd = 0
            completed.clear()
            completedCount = 0
        }
        cleaned = next
    }

    /** Caches statements completed since the last scan; returns where the pending statement starts. */
    @Suppress("ComplexCondition", "NestedBlockDepth")
    private fun scanNewCompleted(): Int {
        var depth = 0
        var ternaryDepth = 0
        var inString: Char? = null
        var escaped = false
        var statementStart = completedEnd

        var i = completedEnd
        while (i < cleaned.length) {
            val c = cleaned[i]
            when {
                escaped -> escaped = false
                c == '\\' && inString != null -> escaped = true
                inString != null -> if (c == inString) inString = null
                c == '"' || c == '\'' -> inString = c
                c == '(' || c == '[' || c == '{' -> depth++
                c == ')' || c == ']' || c == '}' -> depth = maxOf(0, depth - 1)
                c == '?' && depth == 0 -> ternaryDepth++
                c == ':' && depth == 0 && ternaryDepth > 0 -> ternaryDepth--
                c == '\n' && depth <= 0 && ternaryDepth <= 0 -> {
                    // A next line starting with `?` (or `:` inside a ternary) continues the statement.
                    var peek = i + 1
                    while (peek < cleaned.length &&
                        cleaned[peek].let { it == ' ' || it == '\t' || it == '\r' || it == '\n' }
                    ) {
                        peek++
                    }
                    val continues =
                        peek < cleaned.length && (cleaned[peek] == '?' || (cleaned[peek] == ':' && ternaryDepth > 0))
                    if (!continues) {
                        val text = JS.trim(cleaned.substring(statementStart, i))
                        if (text.isNotEmpty()) addStatement(text)
                        statementStart = i + 1
                        completedEnd = i + 1
                    }
                }
            }
            i++
        }
        return statementStart
    }

    internal companion object {
        /**
         * A `$name` ending the raw text may still be growing (`$d` of `$dishes`): it isn't declared
         * unless the same name already appears in full in the pending text.
         */
        fun partialStateName(buffer: String, pendingText: String): String? {
            var start = buffer.length
            while (start > 0 && isWordCharacter(buffer[start - 1])) start--
            if (start == 0 || start == buffer.length || buffer[start - 1] != '$') return null
            val tail = buffer.substring(start - 1)
            if (!pendingText.endsWith(tail)) return null
            val before = pendingText.substring(0, pendingText.length - tail.length)
            var index = before.indexOf(tail)
            while (index != -1) {
                val next = index + tail.length
                if (next >= before.length || !isWordCharacter(before[next])) return null
                index = before.indexOf(tail, index + 1)
            }
            return tail
        }

        private fun isWordCharacter(c: Char): Boolean = c == '_' || c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9'
    }
}
