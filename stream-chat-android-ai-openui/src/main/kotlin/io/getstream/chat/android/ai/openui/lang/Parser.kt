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
 * Creates a one-shot parser for the library JSON Schema [schema] (`library.toJSONSchema()`), port
 * of `createParser`.
 *
 * ```kotlin
 * val parser = createParser(schema = library.toJSONSchema(), rootName = "Card")
 * val result = parser.parse(responseText)
 * ```
 *
 * @param rootName The library's root component. Without a `root` statement, the first statement
 * that calls it renders, with a `no-root` warning.
 */
public fun createParser(schema: OpenUIValue, rootName: String? = null): Parser = Parser(schema, rootName)

/**
 * One-shot parser for OpenUI Lang (port of `Parser` / `parse` in `parser.ts`); create one with
 * [createParser].
 *
 * @param paramMap Component params and calls compiled from the library schema ([compileSchema]),
 * or `null` to parse without a library.
 * @param rootName The library's root component.
 */
public class Parser internal constructor(
    internal val paramMap: ParamMap?,
    internal val rootName: String? = null,
) {
    /** A parser for the library JSON Schema [schema] (`library.toJSONSchema()`). */
    internal constructor(schema: OpenUIValue, rootName: String? = null) : this(compileSchema(schema), rootName)

    /** Parses a complete (or partial) program in one pass. */
    public fun parse(input: String): ParseResult {
        val trimmed = Preprocessor.preprocess(input)
        if (trimmed.isEmpty()) return ParseResult.empty()

        val (text, incomplete) = Statements.autoClose(trimmed)
        val raw = Statements.split(Lexer.tokenize(text))
        if (raw.isEmpty()) return ParseResult.empty(incomplete)

        val statements = LinkedHashMap<String, Statement>()
        for (statement in raw) statements[statement.id] = classify(statement, ExpressionParser.parse(statement.tokens))
        return buildResult(statements, incomplete, statements.size, paramMap, rootName)
    }

    /** Schema compilation. */
    internal companion object {
        private const val ROOT_STATEMENT_ID = "root"

        /** The action `$defs` of every library: `action` props are `{"$ref": "#/$defs/ActionExpression"}`. */
        private val actionDefinitionNames = listOf("ActionExpression", "OpenUrl", "ToAssistant")

        /**
         * Compiles a library JSON Schema (`library.toJSONSchema()`) into component params.
         * Components are the `$defs` listed in `properties`; when there is no `properties`
         * every `$def` that isn't an action shape is a component. The library's `functions` and
         * `actions` join the built-in calls.
         */
        fun compileSchema(schema: OpenUIValue): ParamMap {
            val definitions = schema["\$defs"].objectValue ?: OpenUIObject.Empty
            val components = schema["properties"].objectValue?.keys
            val customActions = schema["actions"].objectValue
            // Action $defs (ActionExpression, OpenUrl, custom actions) are data shapes, never components.
            val actionDefinitions = LinkedHashMap<String, OpenUIValue>()
            for (name in actionDefinitionNames + customActions?.keys.orEmpty()) {
                val body = definitions[name] ?: continue
                if (components?.contains(name) != true) actionDefinitions["#/\$defs/$name"] = body
            }
            val result = LinkedHashMap<String, List<ParamDef>>()
            for ((name, definition) in definitions) {
                // Skip non-component defs, e.g. hoisted recursive schemas.
                val isComponent = components?.contains(name) ?: ("#/\$defs/$name" !in actionDefinitions)
                if (isComponent) result[name] = compileParams(definition, actionDefinitions)
            }
            val calls = LinkedHashMap(Builtins.calls)
            for ((name, function) in schema["functions"].objectValue ?: OpenUIObject.Empty) {
                calls[name] = CallDef(CallDef.Kind.Function, compileParams(function["params"], actionDefinitions))
            }
            for ((name, action) in customActions ?: OpenUIObject.Empty) {
                calls[name] = CallDef(CallDef.Kind.Action, compileParams(action["params"], actionDefinitions))
            }
            return ParamMap(result, calls)
        }

        private fun compileParams(
            definition: OpenUIValue?,
            actionDefinitions: Map<String, OpenUIValue>,
        ): List<ParamDef> {
            val properties = definition["properties"].objectValue ?: OpenUIObject.Empty
            val required = definition["required"].arrayValue?.mapNotNull { it.stringValue }?.toSet().orEmpty()
            return properties.map { (key, property) ->
                ParamDef(
                    name = key,
                    required = key in required,
                    defaultValue = SchemaValidation.defaultValue(property),
                    schema = inlineActionRefs(property, actionDefinitions),
                )
            }
        }

        /** `{"$ref": "#/$defs/ActionExpression"}` → its body, so action slots validate as inline unions. */
        private fun inlineActionRefs(node: OpenUIValue, definitions: Map<String, OpenUIValue>): OpenUIValue {
            if (definitions.isEmpty()) return node
            return when (node) {
                is OpenUIValue.Obj -> {
                    val ref = node.fields["\$ref"].stringValue
                    if (ref != null) {
                        definitions[ref]?.let { inlineActionRefs(it, definitions) } ?: node
                    } else {
                        OpenUIValue.Obj(node.fields.mapValues { inlineActionRefs(it, definitions) })
                    }
                }
                is OpenUIValue.Arr -> OpenUIValue.Arr(node.items.map { inlineActionRefs(it, definitions) })
                else -> node
            }
        }

        /** Classifies a raw statement from its token type and expression shape. */
        internal fun classify(raw: RawStatement, expr: ASTNode): Statement {
            if (expr is ASTNode.Comp && expr.name == Builtins.QUERY) {
                val deps = expr.args.getOrNull(1)?.stateReferences.orEmpty()
                return Statement.Query(raw.id, expr.args, expr, deps.ifEmpty { null })
            }
            if (expr is ASTNode.Comp && expr.name == Builtins.MUTATION) {
                return Statement.Mutation(raw.id, expr.args, expr)
            }
            if (raw.idTokenType == TokenType.StateVar) return Statement.State(raw.id, expr)
            return Statement.Value(raw.id, expr)
        }

        /** A call to a library component: not an `@` function, built-in, `Query` or `Mutation`. */
        private fun componentCallName(node: ASTNode?): String? {
            val call = node as? ASTNode.Comp ?: return null
            if (call.isFunction || Builtins.isBuiltin(call.name) || Builtins.isReservedCall(call.name)) return null
            return call.name
        }

        /** The entry: `root`, else the first statement that calls the root component (a fallback). */
        private class Entry(val id: String, val isFallback: Boolean)

        private fun pickEntry(statements: Map<String, Statement>, rootName: String?): Entry? {
            if (ROOT_STATEMENT_ID in statements) return Entry(ROOT_STATEMENT_ID, isFallback = false)
            val fallback = statements.values.firstOrNull {
                it is Statement.Value && rootName != null && componentCallName(it.expr) == rootName
            }
            return fallback?.let { Entry(it.id, isFallback = true) }
        }

        /** Follows refs to the entry's value; a component call that didn't render was dropped by validation. */
        private fun resolvesToComponentCall(node: ASTNode?, symbols: Map<String, ASTNode>): Boolean {
            var current = node
            val seen = HashSet<String>()
            while (current is ASTNode.Ref && seen.add(current.name)) current = symbols[current.name]
            return componentCallName(current) != null
        }

        /** Whether a runtime expression can produce a component, such as `$t ? A : B`. */
        private fun containsComponent(node: ASTNode): Boolean {
            var found = false
            node.walk { if (componentCallName(it) != null) found = true }
            return found
        }

        @Suppress("LongMethod", "LoopWithTooManyJumpStatements", "NestedBlockDepth")
        internal fun buildResult(
            statements: Map<String, Statement>,
            incomplete: Boolean,
            statementCount: Int,
            catalog: ParamMap?,
            rootName: String?,
            partialReference: PartialReference? = null,
        ): ParseResult {
            val entry = pickEntry(statements, rootName)
            val symbols = statements.mapValues { it.value.symbol }
            // Orphan tracking starts with every value statement except the entry.
            val unreached = LinkedHashSet<String>()
            for ((id, statement) in statements) {
                if (id != entry?.id && statement is Statement.Value) unreached += id
            }
            val context = MaterializeContext(symbols, catalog, incomplete, entry?.id, unreached)
            var root: ElementNode? = null
            var rootExpression: ASTNode? = null
            val entrySymbol = entry?.let { symbols[it.id] }
            if (entry != null && entrySymbol != null) {
                when (val materialized = Materializer.materializeValue(entrySymbol, context)) {
                    is OpenUIValue.Element -> root = materialized.node.copy(statementId = entry.id)
                    is OpenUIValue.Expression ->
                        if (containsComponent(materialized.node)) rootExpression = materialized.node
                    else -> Unit
                }
            }

            val stateDeclarations = OpenUIObject.Builder()
            val queries = ArrayList<QueryStatementInfo>()
            val mutations = ArrayList<MutationStatementInfo>()
            for (statement in statements.values) {
                when (statement) {
                    is Statement.State -> stateDeclarations[statement.id] =
                        Materializer.materializeValue(statement.initial, context)
                    is Statement.Query -> {
                        val args = Materializer.nameRawCallArgs(statement.args, context, statement.id)
                        queries += QueryStatementInfo(
                            statementId = statement.id,
                            toolAST = args.getOrNull(0),
                            argsAST = args.getOrNull(1),
                            defaultsAST = args.getOrNull(2),
                            refreshAST = args.getOrNull(3),
                            deps = statement.deps,
                            complete = true,
                        )
                    }
                    is Statement.Mutation -> {
                        val args = Materializer.nameRawCallArgs(statement.args, context, statement.id)
                        mutations += MutationStatementInfo(
                            statementId = statement.id,
                            toolAST = args.getOrNull(0),
                            argsAST = args.getOrNull(1),
                        )
                    }
                    is Statement.Value -> Unit
                }
            }
            // Any $variable referenced but not declared gets a null default, except a `$name`
            // that may still be streaming in.
            for (statement in statements.values) {
                val nodes = when (statement) {
                    is Statement.State -> listOf(statement.initial)
                    is Statement.Value -> listOf(statement.expr)
                    is Statement.Query -> statement.args
                    is Statement.Mutation -> statement.args
                }
                for (node in nodes) {
                    for (dep in node.stateReferences) {
                        if (dep in stateDeclarations) continue
                        if (partialReference?.matches(dep, statement.id) == true) continue
                        stateDeclarations[dep] = OpenUIValue.Null
                    }
                }
            }

            // Streaming consumers drop parser errors until the stream ends, so this reads as a stream-end check.
            context.currentStatementId = entry?.id
            val component = rootName.orEmpty()
            val noRoot = when {
                entry == null -> NoRootReason.Missing
                root == null && rootExpression == null && !resolvesToComponentCall(entrySymbol, symbols) ->
                    NoRootReason.NotComponent
                entry.isFallback -> if (root != null) NoRootReason.Fallback else NoRootReason.Missing
                else -> null
            }
            if (noRoot != null) SchemaValidation.push(context, component, "", ValidationIssue.NoRoot(noRoot))

            return ParseResult(
                root = root,
                rootExpression = rootExpression,
                meta = ParseResult.Meta(
                    incomplete = incomplete,
                    unresolved = context.unresolved.toList(),
                    // With no entry nothing is reachable; `no-root` alone describes the problem.
                    orphaned = if (entry == null) emptyList() else unreached.toList(),
                    statementCount = statementCount,
                    errors = context.errors.toList(),
                ),
                stateDeclarations = stateDeclarations.build(),
                queryStatements = queries,
                mutationStatements = mutations,
            )
        }
    }
}

/** A `$name` at the very end of streamed text, which may still be growing (`$d` of `$dishes`). */
internal class PartialReference(private val name: String, private val statementIds: Set<String>) {
    fun matches(dependency: String, statementId: String): Boolean = dependency == name && statementId in statementIds
}

/** Cleans a model response before parsing: markdown fences, comments, whitespace. */
internal object Preprocessor {

    fun preprocess(input: String): String = JS.trim(stripComments(stripFences(JS.trim(input))))

    /** Index after a double-quoted string starting at `start` (or `start` when there is none). */
    private fun skipString(input: String, start: Int): Int {
        if (input.getOrNull(start) != '"') return start
        var i = start + 1
        while (i < input.length) {
            when (input[i]) {
                '\\' -> i += 2
                '"' -> return i + 1
                else -> i++
            }
        }
        return i
    }

    private fun isFence(input: String, at: Int): Boolean =
        input[at] == '`' && at + 2 < input.length && input[at + 1] == '`' && input[at + 2] == '`'

    /** Extracts code from markdown fences (skipping ``` inside strings), or returns the input as-is. */
    @Suppress("LoopWithTooManyJumpStatements")
    fun stripFences(input: String): String {
        val blocks = ArrayList<String>()
        var i = 0
        while (i < input.length) {
            var fenceStart = -1
            while (i < input.length) {
                val next = skipString(input, i)
                if (next > i) {
                    i = next
                    continue
                }
                if (isFence(input, i)) {
                    fenceStart = i
                    break
                }
                i++
            }
            if (fenceStart == -1) break

            // Skip the language tag up to the newline.
            var j = fenceStart + 3
            while (j < input.length && input[j] != '\n') j++
            if (j >= input.length) {
                // No newline after the opening fence yet (streaming): nothing to take.
                blocks += input.substring(fenceStart + 3).replace(Regex("^[^\n]*\n?"), "")
                break
            }
            j++

            var closePosition = -1
            var k = j
            while (k < input.length) {
                val next = skipString(input, k)
                if (next > k) {
                    k = next
                    continue
                }
                if (isFence(input, k)) {
                    closePosition = k
                    break
                }
                k++
            }
            if (closePosition != -1) {
                blocks += input.substring(j, closePosition)
                i = closePosition + 3
            } else {
                // No closing fence (streaming): take everything after the opening fence.
                blocks += input.substring(minOf(j, input.length))
                break
            }
        }
        if (blocks.isNotEmpty()) return blocks.joinToString("\n")

        if (input.startsWith("```")) {
            var j = 3
            while (j < input.length && input[j] != '\n') j++
            val body = input.substring(if (j < input.length) j + 1 else 3)
            val trailingFence = body.lastIndexOf("```")
            return if (trailingFence != -1) body.substring(0, trailingFence) else body
        }
        return input
    }

    /** Strips `//` and `#` line comments outside of strings (`"` and `'`). */
    @Suppress("LoopWithTooManyJumpStatements")
    fun stripComments(input: String): String {
        var inString: Char? = null
        return input.split("\n").joinToString("\n") { line ->
            var i = 0
            var result: String? = null
            while (i < line.length) {
                val c = line[i]
                if (inString != null) {
                    if (c == '\\' && i + 1 < line.length) {
                        i += 2
                        continue
                    }
                    if (c == inString) inString = null
                    i++
                    continue
                }
                if (c == '"' || c == '\'') {
                    inString = c
                    i++
                    continue
                }
                if ((c == '/' && line.getOrNull(i + 1) == '/') || c == '#') {
                    result = JS.trimEnd(line.substring(0, i))
                    break
                }
                i++
            }
            result ?: line
        }
    }
}
