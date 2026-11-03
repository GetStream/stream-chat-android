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
 * Schema-aware, single-pass lowering of parsed statements (port of `materialize.ts`).
 *
 * Resolves references, maps positional component arguments to named props, validates required
 * props, applies defaults, converts literals to plain values and keeps runtime expressions as
 * AST for the evaluator.
 */
@Suppress("TooManyFunctions")
internal object Materializer {

    /**
     * How deep the materialized tree nests, references included. Materializing recurses for each
     * level, so a chain of references deeper than any real program reads as null instead of
     * running out of stack.
     */
    internal const val MAX_DEPTH = 256

    /** Whether a prop value contains anything that needs runtime evaluation. */
    fun containsDynamicValue(value: OpenUIValue?): Boolean = when (value) {
        is OpenUIValue.Expression -> true
        is OpenUIValue.Arr -> value.items.any(::containsDynamicValue)
        is OpenUIValue.Element ->
            value.node.props.values
                .any(::containsDynamicValue)
        // A `{steps: [...]}` literal is evaluated too, so data can't pose as custom action steps.
        is OpenUIValue.Obj ->
            value.fields["steps"] is OpenUIValue.Arr || value.fields.values.any(::containsDynamicValue)
        else -> false
    }

    // Calls ----------------------------------------------------------------------------------

    private fun callDefs(context: MaterializeContext): Map<String, CallDef> =
        context.catalog?.callDefs ?: Builtins.calls

    /** Calls the runtime evaluates, which stay AST: functions, steps and reserved calls. */
    private fun isRuntimeCall(name: String, context: MaterializeContext): Boolean =
        name in callDefs(context) || Builtins.isReservedCall(name)

    /** `[Num 1, Ref x]` with params `[a, b]` → `a: Num 1, b: Ref x`; extra args are dropped. */
    private fun nameArgs(args: List<ASTNode>, params: List<ParamDef>): LinkedHashMap<String, ASTNode> {
        val named = LinkedHashMap<String, ASTNode>()
        for ((param, arg) in params.zip(args)) named[param.name] = arg
        return named
    }

    /**
     * Maps a library call's args to its params and checks the literal ones like props. Returns
     * `null` when the call is invalid: it then evaluates to null, or is a no-op for an action.
     */
    @Suppress("LoopWithTooManyJumpStatements", "NestedBlockDepth")
    private fun mapCallArgs(
        name: String,
        args: List<ASTNode>,
        def: CallDef,
        context: MaterializeContext,
    ): Map<String, ASTNode>? {
        val params = def.params
        if (args.size > params.size) {
            SchemaValidation.push(context, name, "", ValidationIssue.ExcessArgs(params.size, args.size))
        }
        val mapped = nameArgs(args, params)
        val errorsBefore = context.errors.size
        var invalid = false
        for (param in params) {
            val present = param.name in mapped
            // References, calls and expressions are checked when they run.
            val value = if (present) mapped.getValue(param.name).literalValue ?: continue else null
            if (value == null || value is OpenUIValue.Null) {
                if (param.required && param.defaultValue == null) {
                    val signature = SchemaValidation.paramsSignature(name, params)
                    val issue = if (present) {
                        ValidationIssue.NullRequired(signature)
                    } else {
                        ValidationIssue.MissingRequired(signature)
                    }
                    SchemaValidation.push(context, name, "/${param.name}", issue)
                    invalid = true
                }
                continue
            }
            val schema = param.schema ?: continue
            val next = when (val checked = SchemaValidation.validate(value, schema, name, "/${param.name}", context)) {
                is Checked.Keep -> checked.value
                Checked.Invalid -> when {
                    param.defaultValue != null -> param.defaultValue
                    param.required -> {
                        invalid = true
                        continue
                    }
                    else -> {
                        mapped.remove(param.name)
                        continue
                    }
                }
            }
            if (next != value) mapped[param.name] = ASTNode.literal(next)
        }
        // An action never runs with a value the model didn't write.
        if (def.kind == CallDef.Kind.Action && context.errors.size > errorsBefore) invalid = true
        return if (invalid) null else mapped
    }

    /**
     * `@Percent(3, total)` → `mappedProps` `part: Num 3, total: <total AST>`. An invalid library
     * function is null, and an invalid action step an empty plan.
     */
    private fun materializeCall(
        node: ASTNode.Comp,
        context: MaterializeContext,
        materializeArg: (ASTNode) -> ASTNode,
    ): ASTNode {
        val recursed = node.args.map(materializeArg)
        val def = callDefs(context)[node.name]
        if (def == null || def.isLazy) return node.copy(args = recursed)
        val mapped = if (def.isBuiltin) {
            nameArgs(recursed, def.params)
        } else {
            mapCallArgs(node.name, recursed, def, context)
        }
        return when {
            mapped != null -> node.copy(args = recursed, mappedProps = mapped)
            def.kind == CallDef.Kind.Action -> ASTNode.Comp("Action", listOf(ASTNode.Arr(emptyList())))
            else -> ASTNode.Null
        }
    }

    /**
     * Names the args of the calls inside a `Query` or `Mutation`'s args, such as
     * `Query("s", {n: @Count(rows)})`. Literal library args are checked like props; an invalid
     * call keeps its raw names and is null at runtime.
     */
    fun nameRawCallArgs(args: List<ASTNode>, context: MaterializeContext, statementId: String): List<ASTNode> {
        val previousStatementId = context.currentStatementId
        context.currentStatementId = statementId
        try {
            return args.map { nameCallArgs(it, context) }
        } finally {
            context.currentStatementId = previousStatementId
        }
    }

    private fun nameCallArgs(node: ASTNode, context: MaterializeContext): ASTNode {
        fun recurse(child: ASTNode) = nameCallArgs(child, context)
        return when (node) {
            is ASTNode.Comp -> {
                val def = callDefs(context)[node.name]
                if (def == null || def.isLazy) {
                    node.copy(
                        args = node.args.map(::recurse),
                        mappedProps = node.mappedProps?.mapValues { recurse(it.value) },
                    )
                } else {
                    val checked = if (def.isBuiltin) null else mapCallArgs(node.name, node.args, def, context)
                    val named = (checked ?: nameArgs(node.args, def.params)).mapValues { recurse(it.value) }
                    node.copy(args = node.args.map(::recurse), mappedProps = named)
                }
            }
            is ASTNode.Arr -> ASTNode.Arr(node.elements.map(::recurse))
            is ASTNode.Obj -> ASTNode.Obj(node.entries.map { (key, value) -> key to recurse(value) })
            is ASTNode.BinOp -> node.copy(left = recurse(node.left), right = recurse(node.right))
            is ASTNode.UnaryOp -> node.copy(operand = recurse(node.operand))
            is ASTNode.Ternary -> ASTNode.Ternary(recurse(node.cond), recurse(node.then), recurse(node.otherwise))
            is ASTNode.Member -> node.copy(obj = recurse(node.obj))
            is ASTNode.Index -> ASTNode.Index(recurse(node.obj), recurse(node.index))
            is ASTNode.Assign -> node.copy(value = recurse(node.value))
            is ASTNode.Str, is ASTNode.Num, is ASTNode.Bool, ASTNode.Null, is ASTNode.Ref, is ASTNode.Ph,
            is ASTNode.StateRef, is ASTNode.RuntimeRef,
            -> node
        }
    }

    /** An `@Name(...)` call that is not a built-in or one of the library's functions or actions. */
    private fun isUnknownFunction(node: ASTNode.Comp, context: MaterializeContext): Boolean {
        if (!node.isFunction || isRuntimeCall(node.name, context)) return false
        SchemaValidation.push(context, node.name, "", ValidationIssue.UnknownFunction)
        return true
    }

    // References -----------------------------------------------------------------------------

    private enum class Mode { Value, Expression }

    /**
     * Resolves a reference: inlines its statement, detects cycles, and turns references to
     * `Query`/`Mutation` declarations into [ASTNode.RuntimeRef]s. In value mode an unresolvable
     * reference is `null`; in expression mode it is a placeholder.
     */
    private fun resolveRef(name: String, context: MaterializeContext, mode: Mode): OpenUIValue {
        val target = context.symbols[name]
        if (name in context.visited || target == null) {
            context.unresolved += name
            return if (mode == Mode.Expression) OpenUIValue.Expression(ASTNode.Ph(name)) else OpenUIValue.Null
        }
        context.unreached -= name
        if (target is ASTNode.Comp && Builtins.isReservedCall(target.name)) {
            val refType = if (target.name == Builtins.MUTATION) RuntimeRefType.Mutation else RuntimeRefType.Query
            return OpenUIValue.Expression(ASTNode.RuntimeRef(name, refType))
        }
        context.visited += name
        val previousStatementId = context.currentStatementId
        context.currentStatementId = name
        try {
            return when (mode) {
                Mode.Value -> when (val result = materializeValue(target, context)) {
                    // Tag elements with their source statement.
                    is OpenUIValue.Element -> OpenUIValue.Element(result.node.copy(statementId = name))
                    else -> result
                }
                Mode.Expression -> OpenUIValue.Expression(materializeExpression(target, context))
            }
        } finally {
            context.currentStatementId = previousStatementId
            context.visited -= name
        }
    }

    /**
     * `@Each(array, varName, template)`: scopes the loop variable while materializing the other
     * arguments so template references to it are kept. `null` when `node` is not a lazy builtin.
     */
    @Suppress("ComplexCondition")
    private fun materializeLazyBuiltin(node: ASTNode.Comp, context: MaterializeContext, scoped: Set<String>): ASTNode? {
        val def = callDefs(context)[node.name]
        if (def == null || def.kind != CallDef.Kind.Function || !def.isLazy || node.args.size < 3) return null
        val variable = when (val arg = node.args[1]) {
            is ASTNode.Ref -> arg.name
            is ASTNode.Str -> arg.value
            else -> null
        }
        if (variable.isNullOrEmpty()) return null
        val nextScoped = scoped + variable
        return ASTNode.Comp(
            name = node.name,
            args = node.args.mapIndexed { index, arg ->
                if (index == 1) arg else materializeExpression(arg, context, nextScoped)
            },
        )
    }

    // Lowering -------------------------------------------------------------------------------

    /**
     * Normalizes a node for use inside a runtime expression: resolves references and adds
     * `mappedProps` to catalog component and library calls. The structure is kept for the evaluator.
     */
    fun materializeExpression(node: ASTNode, context: MaterializeContext, scoped: Set<String> = emptySet()): ASTNode {
        if (context.depth >= MAX_DEPTH) return ASTNode.Null
        context.depth++
        try {
            return lowerExpression(node, context, scoped)
        } finally {
            context.depth--
        }
    }

    private fun lowerExpression(node: ASTNode, context: MaterializeContext, scoped: Set<String>): ASTNode {
        fun recurse(child: ASTNode) = materializeExpression(child, context, scoped)
        return when (node) {
            is ASTNode.Ref -> if (node.name in scoped) {
                node
            } else {
                resolveRef(node.name, context, Mode.Expression).expressionValue ?: ASTNode.Null
            }
            is ASTNode.Comp -> lowerCallExpression(node, context, scoped)
            is ASTNode.Arr -> ASTNode.Arr(node.elements.map(::recurse))
            is ASTNode.Obj -> ASTNode.Obj(node.entries.map { (key, value) -> key to recurse(value) })
            is ASTNode.BinOp -> node.copy(left = recurse(node.left), right = recurse(node.right))
            is ASTNode.UnaryOp -> node.copy(operand = recurse(node.operand))
            is ASTNode.Ternary -> ASTNode.Ternary(recurse(node.cond), recurse(node.then), recurse(node.otherwise))
            is ASTNode.Member -> node.copy(obj = recurse(node.obj))
            is ASTNode.Index -> ASTNode.Index(recurse(node.obj), recurse(node.index))
            is ASTNode.Assign -> node.copy(value = recurse(node.value))
            is ASTNode.Str, is ASTNode.Num, is ASTNode.Bool, ASTNode.Null, is ASTNode.Ph,
            is ASTNode.StateRef, is ASTNode.RuntimeRef,
            -> node
        }
    }

    private fun lowerCallExpression(node: ASTNode.Comp, context: MaterializeContext, scoped: Set<String>): ASTNode {
        if (isUnknownFunction(node, context)) return ASTNode.Null
        materializeLazyBuiltin(node, context, scoped)?.let { return it }
        if (isRuntimeCall(node.name, context)) {
            return materializeCall(node, context) { materializeExpression(it, context, scoped) }
        }
        val args = node.args.map { materializeExpression(it, context, scoped) }
        val params = context.catalog?.get(node.name)
        if (params != null) {
            val mapped = LinkedHashMap<String, ASTNode>()
            for (i in 0 until minOf(params.size, args.size)) mapped[params[i].name] = args[i]
            return node.copy(args = args, mappedProps = mapped)
        }
        SchemaValidation.push(context, node.name, "", ValidationIssue.UnknownComponent(context.catalog?.names))
        return node.copy(args = args)
    }

    /**
     * Materializes a node into a prop value:
     * - an [ElementNode] for catalog components (or `null` when validation drops it)
     * - an [ASTNode] for calls and runtime expressions
     * - plain values for literals, arrays and objects
     * - `null` for placeholders and unresolved references
     */
    fun materializeValue(node: ASTNode, context: MaterializeContext): OpenUIValue {
        if (context.depth >= MAX_DEPTH) return OpenUIValue.Null
        context.depth++
        try {
            return lowerValue(node, context)
        } finally {
            context.depth--
        }
    }

    private fun lowerValue(node: ASTNode, context: MaterializeContext): OpenUIValue = when (node) {
        is ASTNode.Ref -> resolveRef(node.name, context, Mode.Value)
        is ASTNode.Str -> OpenUIValue.Str(node.value)
        is ASTNode.Num -> OpenUIValue.Num(node.value)
        is ASTNode.Bool -> OpenUIValue.of(node.value)
        ASTNode.Null, is ASTNode.Ph -> OpenUIValue.Null
        is ASTNode.Arr -> {
            val items = ArrayList<OpenUIValue>()
            for (element in node.elements) {
                val value = if (element is ASTNode.Ph) null else materializeValue(element, context)
                // Placeholders, and components and references that resolved to null, are dropped.
                val dropped = value == null ||
                    (value is OpenUIValue.Null && (element is ASTNode.Comp || element is ASTNode.Ref))
                if (!dropped) items += value!!
            }
            OpenUIValue.Arr(items)
        }
        is ASTNode.Obj -> OpenUIValue.Obj(
            OpenUIObject.build { for ((key, value) in node.entries) this[key] = materializeValue(value, context) },
        )
        is ASTNode.Comp -> lowerCallValue(node, context)
        else -> OpenUIValue.Expression(materializeExpression(node, context))
    }

    private fun lowerCallValue(node: ASTNode.Comp, context: MaterializeContext): OpenUIValue {
        if (isUnknownFunction(node, context)) return OpenUIValue.Null
        // An inline Query/Mutation (not a statement-level declaration) is an error.
        if (Builtins.isReservedCall(node.name)) {
            SchemaValidation.push(context, node.name, "", ValidationIssue.InlineReserved)
            return OpenUIValue.Null
        }
        // Functions and steps (Count, Action, library functions, ...) stay AST for the runtime.
        if (isRuntimeCall(node.name, context)) {
            val lowered = materializeLazyBuiltin(node, context, emptySet())
                ?: materializeCall(node, context) { materializeExpression(it, context) }
            return OpenUIValue.Expression(lowered)
        }
        return materializeComponent(node, context)
    }

    @Suppress("LoopWithTooManyJumpStatements")
    private fun materializeComponent(node: ASTNode.Comp, context: MaterializeContext): OpenUIValue {
        val name = node.name
        val args = node.args

        val params = context.catalog?.get(name)
        if (params == null) {
            SchemaValidation.push(context, name, "", ValidationIssue.UnknownComponent(context.catalog?.names))
            return OpenUIValue.Null
        }

        val props = OpenUIObject.Builder()
        // Set when a required prop holds invalid data with no default to fall back on.
        var dropComponent = false
        for (i in 0 until minOf(params.size, args.size)) {
            val param = params[i]
            val value = materializeValue(args[i], context)
            props[param.name] = value
            if (param.schema == null) continue
            var next = SchemaValidation.validate(value, param.schema, name, "/${param.name}", context)
            if (next is Checked.Invalid) {
                next = SchemaValidation.resolveInvalid(param.required, param.defaultValue)
                if (next is Checked.Invalid) {
                    dropComponent = true
                    continue
                }
            }
            props[param.name] = (next as Checked.Keep).value
        }

        // Extra positional args are dropped with an error.
        if (args.size > params.size) {
            SchemaValidation.push(context, name, "", ValidationIssue.ExcessArgs(params.size, args.size))
        }

        // Required props: fall back to defaults before dropping the component.
        val stillMissing = params.filter { param ->
            if (!param.required || (param.name in props && props[param.name] !is OpenUIValue.Null)) return@filter false
            val fallback = param.defaultValue ?: return@filter true
            props[param.name] = fallback
            false
        }
        if (stillMissing.isNotEmpty()) {
            for (param in stillMissing) {
                SchemaValidation.push(
                    context,
                    name,
                    "/${param.name}",
                    if (param.name in props) {
                        ValidationIssue.NullRequired(SchemaValidation.paramsSignature(name, params))
                    } else {
                        ValidationIssue.MissingRequired(SchemaValidation.paramsSignature(name, params))
                    },
                )
            }
            return OpenUIValue.Null
        }
        if (dropComponent) return OpenUIValue.Null

        val built = props.build()
        return OpenUIValue.Element(
            ElementNode(
                typeName = name,
                props = built,
                partial = context.partial,
                hasDynamicProps = built.values.any(::containsDynamicValue),
            ),
        )
    }
}
