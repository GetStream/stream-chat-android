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
internal fun createStreamingParser(schema: OpenUIValue, rootName: String? = null): StreamParser =
    StreamParser(schema, rootName)

/**
 * Incremental parser for streamed responses (port of `StreamParser`); create one with
 * [createStreamingParser].
 *
 * Work per update grows with the new text, not with the whole response:
 * - preprocessing (fences, comments, whitespace) reads only the appended text and the line it
 *   ends; completed lines are not processed again ([IncrementalPreprocessor]);
 * - completed statements (ended by a depth-0 newline) are lexed and parsed once; only the
 *   trailing, still-growing statement is re-parsed on each update;
 * - what each referenced statement materialized to is reused until its source or any statement
 *   it reads changes ([MaterializationCache]); the entry statement, `$state` initializers and
 *   `Query`/`Mutation` args are materialized again each update.
 *
 * A statement already completed is replaced by a later definition only once that definition no
 * longer needs auto-closing.
 *
 * Not thread-safe: feed it from one thread.
 *
 * @param paramMap Component params and calls compiled from the library schema
 * ([Parser.compileSchema]), or `null` to parse without a library.
 * @param rootName The library's root component.
 */
internal class StreamParser internal constructor(
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
    private var cleaned = IncrementalPreprocessor()

    /** How far into [cleaned] statements are already completed. */
    private var completedEnd = 0
    private val completed = LinkedHashMap<String, Statement>()
    private var completedCount = 0

    // A new version for each statement parsed, so the cache can tell when one changed.
    private val completedVersions = HashMap<String, Int>()
    private var lastVersion = 0
    private var cache = MaterializationCache()

    /** How many characters preprocessing has read since the last [reset]. */
    internal val preprocessedChars: Int get() = cleaned.charsRead

    /** How many nodes were materialized afresh, not reused, since the last [reset]. */
    internal val materializedNodes: Int get() = cache.materializedNodes

    /** Appends the next chunk and returns the latest result. Pass [complete] once the stream has ended. */
    fun push(chunk: String, complete: Boolean = false): ParseResult {
        buffer += chunk
        refreshCleaned(chunk, 0)
        return getResult(complete)
    }

    /**
     * Sets the full text so far. Only the delta is consumed; if the text was replaced rather
     * than appended to, the parser starts over. Pass [complete] once the stream has ended.
     */
    fun set(fullText: String, complete: Boolean = false): ParseResult {
        if (fullText.length < buffer.length || !fullText.startsWith(buffer)) reset()
        if (fullText.length > buffer.length) {
            val start = buffer.length
            buffer = fullText
            refreshCleaned(fullText, start)
        }
        return getResult(complete)
    }

    /**
     * The latest result without consuming new data. Until [complete], a `$name` ending the text
     * may still be growing, so it isn't declared yet.
     */
    fun getResult(complete: Boolean = false): ParseResult {
        val pendingStart = scanNewCompleted()
        val pendingText = JS.trim(cleaned.text.substring(minOf(pendingStart, cleaned.length), cleaned.length))

        if (pendingText.isEmpty()) {
            if (completedCount == 0) return ParseResult.empty()
            return buildResult(completed, completedVersions, false, completedCount)
        }

        // `cleaned` is already preprocessed: just auto-close the trailing statement.
        val (closed, incomplete) = Statements.autoClose(pendingText)
        val pending = Statements.split(Lexer.tokenize(closed))
        if (pending.isEmpty()) {
            if (completedCount == 0) return ParseResult.empty(incomplete)
            return buildResult(completed, completedVersions, incomplete, completedCount)
        }

        // New ids render progressively; completed ids are replaced only once the pending
        // definition no longer needs auto-closing.
        val all = LinkedHashMap(completed)
        val versions = HashMap(completedVersions)
        val pendingIds = HashSet<String>()
        for (statement in pending) {
            if (statement.id in completed && incomplete) continue
            all[statement.id] = Parser.classify(statement, ExpressionParser.parse(statement.tokens))
            versions[statement.id] = nextVersion()
            pendingIds += statement.id
        }
        val tail = if (complete) null else partialStateName(buffer, pendingText)
        return buildResult(
            all,
            versions,
            incomplete,
            completedCount + pending.size,
            tail?.let { PartialReference(it, pendingIds) },
        )
    }

    /** Clears all state. */
    fun reset() {
        buffer = ""
        cleaned = IncrementalPreprocessor()
        resetCompleted()
    }

    private fun resetCompleted() {
        completedEnd = 0
        completed.clear()
        completedCount = 0
        completedVersions.clear()
        cache = MaterializationCache()
    }

    private fun nextVersion(): Int = ++lastVersion

    private fun buildResult(
        statements: Map<String, Statement>,
        versions: Map<String, Int>,
        incomplete: Boolean,
        statementCount: Int,
        partialReference: PartialReference? = null,
    ): ParseResult {
        cache.versions = versions
        return Parser.buildResult(statements, incomplete, statementCount, paramMap, rootName, partialReference, cache)
    }

    private fun addStatement(text: String) {
        val trimmed = JS.trim(text)
        if (trimmed.isEmpty()) return
        for (statement in Statements.split(Lexer.tokenize(trimmed))) {
            completed[statement.id] = Parser.classify(statement, ExpressionParser.parse(statement.tokens))
            completedVersions[statement.id] = nextVersion()
            completedCount++
        }
    }

    /**
     * Preprocesses `text[start, text.length)`. Completed statements are read again when the text
     * before [completedEnd] changes: a fence after unfenced text, or the line after them turning
     * into a comment or a closing fence.
     */
    private fun refreshCleaned(text: String, start: Int) {
        val startedOver = cleaned.append(text, start, text.length)
        if (startedOver || cleaned.length < completedEnd) resetCompleted()
    }

    /** Caches statements completed since the last scan; returns where the pending statement starts. */
    @Suppress("ComplexCondition", "NestedBlockDepth")
    private fun scanNewCompleted(): Int {
        var depth = 0
        var ternaryDepth = 0
        var inString: Char? = null
        var escaped = false
        var statementStart = completedEnd
        val text = cleaned.text
        val end = cleaned.length

        var i = completedEnd
        while (i < end) {
            val c = text[i]
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
                    while (peek < end &&
                        text[peek].let { it == ' ' || it == '\t' || it == '\r' || it == '\n' }
                    ) {
                        peek++
                    }
                    val continues =
                        peek < end && (text[peek] == '?' || (text[peek] == ':' && ternaryDepth > 0))
                    if (!continues) {
                        val statement = JS.trim(text.substring(statementStart, i))
                        if (statement.isNotEmpty()) addStatement(statement)
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
