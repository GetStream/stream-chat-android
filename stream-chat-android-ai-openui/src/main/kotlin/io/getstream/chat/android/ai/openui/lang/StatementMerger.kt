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
 * Merges [patch] (an edit-mode response, Markdown fences allowed) into [existing] (the previous
 * program) and returns the merged program text (port of `mergeStatements`).
 *
 * Asked to change UI it already produced, a model can answer with a patch that holds only the
 * statements that change instead of the whole program. Statements are matched by id: a patch
 * statement replaces the existing statement with the same id or is appended when the id is new,
 * and `id = null` deletes one. Statements no longer reachable from the root statement [rootId]
 * (`root` by default) are dropped; `$state` variables are always kept.
 */
internal fun mergeStatements(existing: String, patch: String, rootId: String = "root"): String =
    StatementMerger.merge(existing, patch, rootId)

/** The statement merge behind [mergeStatements] (port of `merge.ts`). */
internal object StatementMerger {

    private class Parsed(val id: String, val ast: ASTNode, val raw: String)

    /**
     * Merges [patch] (an edit-mode response, Markdown fences allowed) into [existing] (the
     * previous program) and returns the merged program text. [rootId] is the id of the root
     * statement that reachability is measured from; `root` by default.
     */
    fun merge(existing: String, patch: String, rootId: String = "root"): String {
        val existingStatements = parseStatements(existing)
        val patchStatements = parseStatements(Preprocessor.stripFences(patch))

        if (existingStatements.isEmpty()) return patchStatements.joinToString("\n") { it.raw }
        if (patchStatements.isEmpty()) return existing

        val merged = LinkedHashMap<String, String>()
        val asts = HashMap<String, ASTNode>()
        val order = ArrayList<String>()
        for (statement in existingStatements) {
            merged[statement.id] = statement.raw
            asts[statement.id] = statement.ast
            order += statement.id
        }
        for (statement in patchStatements) {
            if (statement.ast == ASTNode.Null) {
                // `name = null` in a patch deletes the statement.
                merged.remove(statement.id)
                asts.remove(statement.id)
                order.remove(statement.id)
                continue
            }
            if (statement.id !in merged) order += statement.id
            merged[statement.id] = statement.raw
            asts[statement.id] = statement.ast
        }

        collectGarbage(order, merged, asts, rootId)
        return order.mapNotNull { merged[it] }.joinToString("\n")
    }

    private fun parseStatements(input: String): List<Parsed> {
        val trimmed = JS.trim(input)
        if (trimmed.isEmpty()) return emptyList()
        return splitStatementSource(trimmed).mapNotNull { raw ->
            Statements
                .split(Lexer.tokenize(raw)).firstOrNull()
                ?.let { Parsed(it.id, ExpressionParser.parse(it.tokens), raw) }
        }
    }

    /** Splits source text at depth-0 newlines (outside strings and brackets). */
    private fun splitStatementSource(input: String): List<String> {
        val statements = ArrayList<String>()
        var depth = 0
        var inString: Char? = null
        var escaped = false
        var start = 0
        input.forEachIndexed { i, c ->
            when {
                escaped -> escaped = false
                c == '\\' && inString != null -> escaped = true
                inString != null -> if (c == inString) inString = null
                c == '"' || c == '\'' -> inString = c
                c == '(' || c == '[' || c == '{' -> depth++
                c == ')' || c == ']' || c == '}' -> depth = maxOf(0, depth - 1)
                c == '\n' && depth <= 0 -> {
                    JS.trim(input.substring(start, i)).takeIf { it.isNotEmpty() }?.let { statements += it }
                    start = i + 1
                }
            }
        }
        JS.trim(input.substring(start)).takeIf { it.isNotEmpty() }?.let { statements += it }
        return statements
    }

    /** Removes statements unreachable from the root. `$state` variables are always kept. */
    private fun collectGarbage(
        order: MutableList<String>,
        merged: MutableMap<String, String>,
        asts: Map<String, ASTNode>,
        rootId: String,
    ) {
        if (rootId !in asts) return
        val reachable = hashSetOf(rootId)
        val queue = ArrayDeque(listOf(rootId))
        while (queue.isNotEmpty()) {
            val ast = asts[queue.removeLast()] ?: continue
            ast.walk { node ->
                val name = when (node) {
                    is ASTNode.Ref -> node.name
                    is ASTNode.RuntimeRef -> node.name
                    else -> null
                }
                if (name != null && name !in reachable && name in asts) {
                    reachable += name
                    queue += name
                }
            }
        }
        order.filter { it.startsWith("$") }.forEach { reachable += it }
        order.removeAll { id -> (id !in reachable).also { if (it) merged.remove(id) } }
    }
}
