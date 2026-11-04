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

import io.getstream.chat.android.ai.openui.library.DefinedAction
import io.getstream.chat.android.ai.openui.library.DefinedFunction
import io.getstream.chat.android.ai.openui.library.PropField

/** Resolves runtime values for the evaluator. */
internal class EvaluationContext(
    /** Reads a `$variable` (`null` = undefined). */
    val getState: (String) -> OpenUIValue?,
    /** Resolves a reference to another declaration's runtime value (query/mutation results, loop vars). */
    val resolveRef: (String) -> OpenUIValue?,
    /** Extra scope used when evaluating reactive setters (`$value`). */
    val extraScope: Map<String, OpenUIValue>? = null,
    /** The library's functions, called as `@Name(...)`. */
    val functions: Map<String, DefinedFunction> = emptyMap(),
    /** The library's custom actions, used as `@Name(...)` steps. */
    val actions: Map<String, DefinedAction> = emptyMap(),
    /** Reports a runtime error, such as a library function call that evaluated to null. */
    val reportError: ((String) -> Unit)? = null,
) {
    /** Reports an error of one prop of an element, which [PropEvaluator] turns into [reportError]. */
    internal var reportPropError: ((component: String, statementId: String?, prop: String, message: String) -> Unit)? =
        null

    internal fun copy(
        resolveRef: (String) -> OpenUIValue? = this.resolveRef,
        extraScope: Map<String, OpenUIValue>? = this.extraScope,
        reportError: ((String) -> Unit)? = this.reportError,
    ): EvaluationContext = EvaluationContext(getState, resolveRef, extraScope, functions, actions, reportError).also {
        it.reportPropError = reportPropError
    }

    /** This context reporting the errors of [prop] of [element] through [reportPropError]. */
    internal fun forProp(element: ElementNode, prop: String): EvaluationContext {
        val report = reportPropError ?: return this
        return copy(reportError = { report(element.typeName, element.statementId, prop, it) })
    }
}

/**
 * Reactive binding markers produced when a `$variable` is passed to a reactive prop
 * (`{ __reactive: "assign", target, expr }` in the reference implementation).
 */
internal object ReactiveBinding {
    internal const val MARKER_KEY: String = "__reactive"

    internal fun marker(target: String, expression: ASTNode): OpenUIValue = OpenUIValue.obj(
        MARKER_KEY to OpenUIValue.Str("assign"),
        "target" to OpenUIValue.Str(target),
        "expr" to OpenUIValue.Expression(expression),
    )

    /** Whether `value` is a reactive binding marker. */
    fun isBinding(value: OpenUIValue?): Boolean = value.objectValue?.get(MARKER_KEY) == OpenUIValue.Str("assign")

    /** The bound `$variable` name. */
    fun target(value: OpenUIValue?): String? = if (isBinding(value)) value["target"].stringValue else null

    /**
     * The setter expression of a binding. Nested markers (produced by repeated prop evaluation)
     * are unwrapped to the innermost expression.
     */
    fun expression(value: OpenUIValue?): ASTNode? {
        var current = value["expr"]
        while (current != null) {
            if (current is OpenUIValue.Expression) return current.node
            if (!isBinding(current)) return null
            current = current["expr"]
        }
        return null
    }
}

/** Evaluates AST nodes to runtime values with JavaScript semantics (port of `evaluator.ts`). */
@Suppress("TooManyFunctions")
internal object Evaluator {

    /** Evaluates an expression. Returns `null` for JavaScript `undefined`. */
    @Suppress("ComplexCondition")
    fun evaluate(node: ASTNode, context: EvaluationContext, schema: ReactivePropResolver? = null): OpenUIValue? =
        when (node) {
            is ASTNode.Str -> OpenUIValue.Str(node.value)
            is ASTNode.Num -> OpenUIValue.Num(node.value)
            is ASTNode.Bool -> OpenUIValue.of(node.value)
            ASTNode.Null, is ASTNode.Ph -> OpenUIValue.Null

            // `extraScope[name] ?? getState(name)`
            is ASTNode.StateRef -> context.extraScope?.get(node.name)?.takeUnless { JS.isNullish(it) }
                ?: context.getState(node.name)

            is ASTNode.Ref -> context.resolveRef(node.name)
            is ASTNode.RuntimeRef -> context.resolveRef(node.name)

            // `undefined` items serialize as null.
            is ASTNode.Arr -> OpenUIValue.Arr(node.elements.map { evaluate(it, context) ?: OpenUIValue.Null })
            is ASTNode.Obj -> OpenUIValue.Obj(
                OpenUIObject.build {
                    for ((key, value) in node.entries) {
                        this[key] = evaluate(value, context)
                    }
                },
            )

            is ASTNode.Comp -> evaluateCall(node, context, schema)
            is ASTNode.BinOp -> evaluateBinary(node, context)

            is ASTNode.UnaryOp -> when (node.op) {
                "!" -> OpenUIValue.of(!JS.isTruthy(evaluate(node.operand, context)))
                "-" -> OpenUIValue.Num(-JS.toNumber(evaluate(node.operand, context)))
                else -> OpenUIValue.Null
            }

            is ASTNode.Ternary ->
                if (JS.isTruthy(evaluate(node.cond, context))) {
                    evaluate(node.then, context)
                } else {
                    evaluate(node.otherwise, context)
                }

            is ASTNode.Member -> {
                val obj = evaluate(node.obj, context)
                when {
                    JS.isNullish(obj) -> OpenUIValue.Null
                    // Array pluck: extract the field from every element.
                    obj is OpenUIValue.Arr ->
                        if (node.field == "length") {
                            OpenUIValue.Num(obj.items.size.toDouble())
                        } else {
                            OpenUIValue.Arr(
                                obj.items.map { item ->
                                    JS.property(item, node.field)?.takeUnless { JS.isNullish(it) }
                                        ?: OpenUIValue.Null
                                },
                            )
                        }
                    else -> JS.property(obj, node.field)
                }
            }

            is ASTNode.Index -> {
                val obj = evaluate(node.obj, context)
                val index = evaluate(node.index, context)
                when {
                    JS.isNullish(obj) || JS.isNullish(index) -> OpenUIValue.Null
                    obj is OpenUIValue.Arr -> {
                        val position = JS.toNumber(index)
                        if (position.isFinite() &&
                            position == kotlin.math.floor(position) &&
                            position >= 0 && position < obj.items.size
                        ) {
                            obj.items[position.toInt()]
                        } else {
                            null
                        }
                    }
                    else -> JS.property(obj, JS.toString(index))
                }
            }

            is ASTNode.Assign -> ReactiveBinding.marker(node.target, node.value)
        }

    private fun evaluateBinary(node: ASTNode.BinOp, context: EvaluationContext): OpenUIValue? {
        // Short-circuit operators evaluate lazily and return an operand.
        if (node.op == "&&") {
            val left = evaluate(node.left, context)
            return if (JS.isTruthy(left)) evaluate(node.right, context) else left
        }
        if (node.op == "||") {
            val left = evaluate(node.left, context)
            return if (JS.isTruthy(left)) left else evaluate(node.right, context)
        }
        val left = evaluate(node.left, context)
        val right = evaluate(node.right, context)
        return when (node.op) {
            "+" ->
                if (JS.typeOf(left) == "string" || JS.typeOf(right) == "string") {
                    // null/undefined concatenate as "" (no "textnull").
                    OpenUIValue.Str(JS.toStringOrEmpty(left) + JS.toStringOrEmpty(right))
                } else {
                    OpenUIValue.Num(JS.toNumber(left) + JS.toNumber(right))
                }
            "-" -> OpenUIValue.Num(JS.toNumber(left) - JS.toNumber(right))
            "*" -> OpenUIValue.Num(JS.toNumber(left) * JS.toNumber(right))
            // DSL choice: division by zero yields 0 instead of Infinity/NaN.
            "/" -> JS.toNumber(right).let { divisor ->
                OpenUIValue.Num(
                    if (divisor == 0.0) {
                        0.0
                    } else {
                        JS.toNumber(left) / divisor
                    },
                )
            }
            "%" -> JS.toNumber(right).let { divisor ->
                OpenUIValue.Num(
                    if (divisor == 0.0) {
                        0.0
                    } else {
                        JS.remainder(JS.toNumber(left), divisor)
                    },
                )
            }
            "==" -> OpenUIValue.of(JS.looseEquals(left, right))
            "!=" -> OpenUIValue.of(!JS.looseEquals(left, right))
            ">" -> OpenUIValue.of(JS.toNumber(left) > JS.toNumber(right))
            "<" -> OpenUIValue.of(JS.toNumber(left) < JS.toNumber(right))
            ">=" -> OpenUIValue.of(JS.toNumber(left) >= JS.toNumber(right))
            "<=" -> OpenUIValue.of(JS.toNumber(left) <= JS.toNumber(right))
            else -> OpenUIValue.Null
        }
    }

    private fun evaluateCall(
        node: ASTNode.Comp,
        context: EvaluationContext,
        schema: ReactivePropResolver?,
    ): OpenUIValue? {
        // Built-ins coerce their args, so they skip validation. Lazy ones get them unevaluated.
        if (node.name in Builtins.lazyNames) return evaluateLazyBuiltin(node.name, node.args, context, schema)
        Builtins.definitionsByName[node.name]?.let { builtin ->
            val params = Builtins.calls[node.name]?.params.orEmpty()
            val named = node.mappedProps ?: nameArgs(node.args, params)
            return builtin.function(params.map { param -> named[param.name]?.let { evaluate(it, context) } })
        }
        if (node.name in Builtins.actionNames) return evaluateActionCall(node, context)
        context.functions[node.name]?.let { return callFunction(it, node, context) }
        context.actions[node.name]?.let { return customActionPlan(it, node, context) }
        // After materialization only builtins and mapped catalog calls reach here.
        val mappedProps = node.mappedProps ?: return OpenUIValue.Null
        return evaluateMappedComponent(node.name, mappedProps, context, schema)
    }

    /** `[Num 1, Ref x]` with params `[a, b]` → `a: Num 1, b: Ref x`; extra args are dropped. */
    private fun nameArgs(args: List<ASTNode>, params: List<ParamDef>): Map<String, ASTNode> =
        params.zip(args).associate { (param, arg) -> param.name to arg }

    /** A catalog component call inside an expression (a ternary branch or an `@Each` template). */
    private fun evaluateMappedComponent(
        name: String,
        mappedProps: Map<String, ASTNode>,
        context: EvaluationContext,
        schema: ReactivePropResolver?,
    ): OpenUIValue {
        val props = OpenUIObject.Builder()
        for ((key, value) in mappedProps) {
            props[key] = if (value is ASTNode.StateRef) {
                when {
                    schema?.isReactiveProp(
                        name,
                        key,
                    ) == true -> ReactiveBinding.marker(value.name, ASTNode.StateRef("\$value"))
                    schema != null -> context.getState(value.name)
                    // Without schema context the reference is kept for prop evaluation.
                    else -> OpenUIValue.Expression(value)
                }
            } else {
                evaluate(value, context, schema)
            }
        }
        var element = ElementNode(name, props.build(), partial = false, hasDynamicProps = true)
        if (schema != null) {
            element = element.copy(
                props = element.props.mapValues { value ->
                    when (value) {
                        is OpenUIValue.Element -> OpenUIValue.Element(
                            evaluateElementInline(value.node, context, schema),
                        )
                        is OpenUIValue.Arr -> OpenUIValue.Arr(
                            value.items.map { item ->
                                if (item is OpenUIValue.Element) {
                                    OpenUIValue.Element(evaluateElementInline(item.node, context, schema))
                                } else {
                                    item
                                }
                            },
                        )
                        else -> value
                    }
                },
            )
        }
        return OpenUIValue.Element(element)
    }

    /** Evaluates an element's props with reactive awareness (the inline evaluation path). */
    internal fun evaluateElementInline(
        element: ElementNode,
        context: EvaluationContext,
        schema: ReactivePropResolver,
    ): ElementNode {
        if (element.hasDynamicProps == false) return element
        val props = OpenUIObject.build {
            for ((key, value) in element.props) {
                val propContext = context.forProp(element, key)
                this[key] =
                    PropEvaluator.evaluateProp(
                        value,
                        propContext,
                        schema,
                        schema.isReactiveProp(element.typeName, key),
                    ) { child ->
                        evaluateElementInline(child, propContext, schema)
                    }
            }
        }
        return element.copy(props = props)
    }

    // Library functions and custom actions -----------------------------------------------------

    /**
     * A library function's or action's args by name. An explicit null for an optional param is
     * left out, so its default applies.
     */
    private fun callArgs(params: List<PropField>, node: ASTNode.Comp, context: EvaluationContext): OpenUIObject {
        val named = node.mappedProps ?: nameArgs(node.args, params.map { ParamDef(it.name, required = false) })
        return OpenUIObject.build {
            for ((key, arg) in named) {
                val value = evaluate(arg, context)
                val isOptional = params.firstOrNull { it.name == key }?.isOptional ?: true
                if (JS.isNullish(value) && isOptional) continue
                this[key] = value ?: OpenUIValue.Null
            }
        }
    }

    /** Whether a required arg without a default is missing, such as `@Percent($p)` with `$p` unset. */
    private fun missesRequired(args: OpenUIObject, params: List<PropField>): Boolean = params.any { param ->
        !param.isOptional && JS.isNullish(args[param.name]) && param.defaultValue == null
    }

    /**
     * Checks a value against a schema, reporting each problem. Returns `null` when it is invalid,
     * or, when [strict], when anything was reported.
     */
    private fun checkValue(
        value: OpenUIValue,
        schema: OpenUIValue,
        name: String,
        path: String,
        context: EvaluationContext,
        strict: Boolean = false,
    ): OpenUIValue? {
        val validation = MaterializeContext(emptyMap(), null, false, null, mutableSetOf())
        val checked = SchemaValidation.validate(value, schema, name, path, validation)
        for (error in validation.errors) context.reportError?.invoke("@$name: ${error.message}")
        if (checked !is Checked.Keep || (strict && validation.errors.isNotEmpty())) return null
        return checked.value
    }

    /**
     * `@Percent(3, 4)` → the function's result for `part: 3, total: 4`; bad args or result, or a
     * throw, give null.
     */
    @Suppress("TooGenericExceptionCaught")
    private fun callFunction(function: DefinedFunction, node: ASTNode.Comp, context: EvaluationContext): OpenUIValue {
        val raw = callArgs(function.params, node, context)
        if (missesRequired(raw, function.params)) return OpenUIValue.Null
        val checked = checkValue(OpenUIValue.Obj(raw), function.paramsSchema, function.name, "", context).objectValue
            ?: return OpenUIValue.Null
        val result = try {
            function.fn(checked)
        } catch (error: Exception) {
            // The app's function failing must not take the UI down: the call is null instead.
            context.reportError?.invoke("@${function.name} threw: ${error.message ?: error.toString()}")
            return OpenUIValue.Null
        }
        val returns = function.returns ?: return result
        return checkValue(result, returns.jsonSchema, function.name, "/returns", context) ?: OpenUIValue.Null
    }

    /**
     * A custom action call is a one-step plan. Any invalid arg gives an empty plan, a no-op, even an
     * optional one with a default: an action never runs with a value the model didn't write.
     */
    private fun customActionPlan(
        action: DefinedAction,
        node: ASTNode.Comp,
        context: EvaluationContext,
    ): OpenUIValue {
        val raw = callArgs(action.params, node, context)
        if (missesRequired(raw, action.params)) return ActionPlan.empty
        val params = checkValue(OpenUIValue.Obj(raw), action.paramsSchema, action.name, "", context, strict = true)
            ?: return ActionPlan.empty
        return ActionPlan.plan(listOf(ActionPlan.customStep(action.name, params)))
    }

    // Actions --------------------------------------------------------------------------------

    /** A step call is a one-step plan and an invalid step an empty plan; `Action([...])` flattens. */
    private fun evaluateActionCall(node: ASTNode.Comp, context: EvaluationContext): OpenUIValue {
        val args = node.args
        if (node.name == "Action") {
            val list = if (args.isEmpty()) OpenUIValue.EmptyArray else evaluate(args[0], context)
            return ActionPlan.flatten(list.arrayValue.orEmpty())
        }
        val step: OpenUIValue? = when (node.name) {
            "Run" -> (args.firstOrNull() as? ASTNode.RuntimeRef)?.let { ref ->
                OpenUIValue.obj(
                    "type" to OpenUIValue.Str(ActionStepType.RUN),
                    "statementId" to OpenUIValue.Str(ref.name),
                    "refType" to OpenUIValue.Str(ref.refType.wireName),
                )
            }
            "ToAssistant" -> {
                val named = stepArgs(node, context)
                OpenUIValue.obj(
                    "type" to OpenUIValue.Str(ActionStepType.CONTINUE_CONVERSATION),
                    "message" to OpenUIValue.Str(JS.toStringOrEmpty(named["message"])),
                    // The context may be any value and is passed through unchanged; null means none.
                    "context" to named["context"]?.takeUnless { JS.isNullish(it) },
                )
            }
            "OpenUrl" -> OpenUIValue.obj(
                "type" to OpenUIValue.Str(ActionStepType.OPEN_URL),
                "url" to OpenUIValue.Str(JS.toStringOrEmpty(stepArgs(node, context)["url"])),
            )
            // The value expression is kept and evaluated at click time.
            "Set" -> {
                val target = args.firstOrNull() as? ASTNode.StateRef
                if (args.size < 2 || target == null) {
                    null
                } else {
                    OpenUIValue.obj(
                        "type" to OpenUIValue.Str(ActionStepType.SET),
                        "target" to OpenUIValue.Str(target.name),
                        "valueAST" to OpenUIValue.Expression(args[1]),
                    )
                }
            }
            // Restores declared defaults at runtime.
            "Reset" -> {
                val targets = args.filterIsInstance<ASTNode.StateRef>().map { OpenUIValue.Str(it.name) }
                if (targets.isEmpty()) {
                    null
                } else {
                    OpenUIValue.obj(
                        "type" to OpenUIValue.Str(ActionStepType.RESET),
                        "targets" to OpenUIValue.Arr(targets),
                    )
                }
            }
            else -> null
        }
        return ActionPlan.plan(listOfNotNull(step))
    }

    private fun stepArgs(node: ASTNode.Comp, context: EvaluationContext): OpenUIObject {
        val named = node.mappedProps ?: nameArgs(node.args, Builtins.calls[node.name]?.params.orEmpty())
        return OpenUIObject.build {
            for ((key, arg) in named) this[key] = evaluate(arg, context) ?: OpenUIValue.Null
        }
    }

    // Lazy builtins --------------------------------------------------------------------------

    /**
     * `@Each(array, varName, template)` evaluates the template once per item. Loop variable
     * references are substituted with literal values first so deferred expressions (action
     * steps evaluated at click time) capture concrete values.
     */
    private fun evaluateLazyBuiltin(
        name: String,
        args: List<ASTNode>,
        context: EvaluationContext,
        schema: ReactivePropResolver?,
    ): OpenUIValue {
        if (name != "Each") return OpenUIValue.Null
        if (args.size < 3) return OpenUIValue.EmptyArray
        val items = evaluate(args[0], context).arrayValue ?: return OpenUIValue.EmptyArray
        val variable = when (val arg = args[1]) {
            is ASTNode.Ref -> arg.name
            is ASTNode.Str -> arg.value
            else -> null
        }
        if (variable.isNullOrEmpty()) return OpenUIValue.EmptyArray
        val template = args[2]

        return OpenUIValue.Arr(
            items.map { item ->
                val substituted = substituteRef(template, variable, item)
                val childContext = context.copy(resolveRef = { ref ->
                    if (ref == variable) {
                        item
                    } else {
                        context.resolveRef(ref)
                    }
                })
                val result = evaluate(substituted, childContext, schema)
                if (schema != null && result is OpenUIValue.Element) {
                    OpenUIValue.Element(evaluateElementInline(result.node, childContext, schema))
                } else {
                    result ?: OpenUIValue.Null
                }
            },
        )
    }

    private fun shadows(node: ASTNode.Comp, variable: String): Boolean {
        if (node.name !in Builtins.lazyNames || node.args.size < 3) return false
        val loopVariable = node.args[1]
        return loopVariable == ASTNode.Ref(variable) || loopVariable == ASTNode.Str(variable)
    }

    /** Replaces every `Ref(variable)` in `node` with the literal form of `value`. */
    internal fun substituteRef(node: ASTNode, variable: String, value: OpenUIValue): ASTNode {
        fun recurse(child: ASTNode) = substituteRef(child, variable, value)
        return when (node) {
            is ASTNode.Ref -> if (node.name == variable) ASTNode.literal(value) else node
            is ASTNode.Member -> {
                // Member access on the loop variable resolves straight to the field's literal.
                val obj = recurse(node.obj)
                (obj as? ASTNode.Obj)?.entries?.firstOrNull { it.first == node.field }?.second ?: node.copy(obj = obj)
            }
            is ASTNode.Index -> ASTNode.Index(recurse(node.obj), recurse(node.index))
            is ASTNode.BinOp -> node.copy(left = recurse(node.left), right = recurse(node.right))
            is ASTNode.UnaryOp -> node.copy(operand = recurse(node.operand))
            is ASTNode.Ternary -> ASTNode.Ternary(recurse(node.cond), recurse(node.then), recurse(node.otherwise))
            is ASTNode.Arr -> ASTNode.Arr(node.elements.map(::recurse))
            is ASTNode.Obj -> ASTNode.Obj(node.entries.map { (key, item) -> key to recurse(item) })
            is ASTNode.Comp -> {
                // An inner `@Each` with the same loop variable shadows this one in its template.
                val shadowed = shadows(node, variable)
                val args = node.args.mapIndexed { index, arg -> if (shadowed && index > 0) arg else recurse(arg) }
                // A prop that is one of the args reuses its substitution, so each arg is done once.
                val mappedProps = node.mappedProps?.mapValues { (_, prop) ->
                    val index = node.argIndexOf(prop)
                    when {
                        index >= 0 -> args[index]
                        shadowed -> prop
                        else -> recurse(prop)
                    }
                }
                node.copy(args = args, mappedProps = mappedProps)
            }
            is ASTNode.Assign -> node.copy(value = recurse(node.value))
            else -> node
        }
    }
}

/** Prop evaluation shared by the inline and tree paths (port of `evaluate-prop.ts` / `evaluate-tree.ts`). */
internal object PropEvaluator {

    /**
     * Evaluates `root`, or the component a runtime entry (`root = $t ? A : B`) picks, with the
     * runtime errors evaluation reports (upstream `evaluateRoot`).
     */
    internal fun evaluateRoot(
        result: ParseResult?,
        context: EvaluationContext,
        schema: ReactivePropResolver,
    ): Pair<ElementNode?, List<OpenUIError>> {
        val errors = ArrayList<OpenUIError>()
        val reporting = context.copy().apply {
            reportPropError = { component, statementId, prop, message ->
                errors += OpenUIError(
                    source = OpenUIError.Source.Runtime,
                    code = "runtime-error",
                    message = "Evaluating prop \"$prop\" on $component failed: $message",
                    statementId = statementId,
                    component = component,
                    hint = "Check the expression used for prop \"$prop\"",
                )
            }
        }
        result?.root?.let { return evaluateElementProps(it, reporting, schema) to errors }
        val rootExpression = result?.rootExpression ?: return null to errors
        val element = Evaluator.evaluate(rootExpression, reporting, schema).elementValue
        if (element != null) return evaluateElementProps(element, reporting, schema) to errors
        errors += OpenUIError(
            source = OpenUIError.Source.Runtime,
            code = "no-root",
            message = "root does not evaluate to a component",
            statementId = "root",
        )
        return null to errors
    }

    /** Evaluates every dynamic prop of an element tree into concrete values. */
    fun evaluateElementProps(
        element: ElementNode,
        context: EvaluationContext,
        schema: ReactivePropResolver,
    ): ElementNode {
        if (element.hasDynamicProps == false) return element
        val props = OpenUIObject.build {
            for ((key, value) in element.props) {
                val propContext = context.forProp(element, key)
                this[key] =
                    evaluateProp(value, propContext, schema, schema.isReactiveProp(element.typeName, key)) { child ->
                        evaluateElementProps(child, propContext, schema)
                    }
            }
        }
        return element.copy(props = props)
    }

    /**
     * Evaluates a single prop value: AST nodes are resolved (or turned into reactive binding
     * markers), nested elements recurse, action plans are kept for click-time evaluation.
     */
    internal fun evaluateProp(
        value: OpenUIValue?,
        context: EvaluationContext,
        schema: ReactivePropResolver,
        isReactive: Boolean,
        recurseElement: (ElementNode) -> ElementNode,
    ): OpenUIValue? {
        val result = evaluatePropValue(value, context, schema, isReactive, recurseElement)
        // Only real calls make custom steps: a plan read from data loses them.
        return if (ActionPlan.isPlan(result)) ActionPlan.ownSteps(result!!) else result
    }

    @Suppress("NestedBlockDepth")
    private fun evaluatePropValue(
        value: OpenUIValue?,
        context: EvaluationContext,
        schema: ReactivePropResolver,
        isReactive: Boolean,
        recurseElement: (ElementNode) -> ElementNode,
    ): OpenUIValue? = when (value) {
        null, OpenUIValue.Null, is OpenUIValue.Bool, is OpenUIValue.Num, is OpenUIValue.Str -> value

        is OpenUIValue.Expression -> {
            val node = value.node
            if (isReactive && node is ASTNode.StateRef) {
                ReactiveBinding.marker(node.name, ASTNode.StateRef("\$value"))
            } else {
                when (val result = Evaluator.evaluate(node, context, schema)) {
                    is OpenUIValue.Element -> OpenUIValue.Element(recurseElement(result.node))
                    is OpenUIValue.Arr -> OpenUIValue.Arr(
                        result.items.map { item ->
                            if (item is OpenUIValue.Element) {
                                OpenUIValue.Element(recurseElement(item.node))
                            } else {
                                item
                            }
                        },
                    )
                    else -> {
                        val target = ReactiveBinding.target(result)
                        if (!isReactive && target != null) {
                            // Binding markers in non-reactive props read the current value.
                            context.getState(target)?.takeUnless { JS.isNullish(it) } ?: OpenUIValue.Null
                        } else {
                            result
                        }
                    }
                }
            }
        }

        is OpenUIValue.Arr -> OpenUIValue.Arr(
            value.items.map { evaluateProp(it, context, schema, isReactive, recurseElement) ?: OpenUIValue.Null },
        )

        is OpenUIValue.Element -> OpenUIValue.Element(recurseElement(value.node))

        is OpenUIValue.Obj -> {
            val fields = value.fields
            when {
                // Action plans are kept for deferred (click-time) evaluation.
                fields["steps"] is OpenUIValue.Arr -> value
                fields.values.none {
                    it is OpenUIValue.Arr ||
                        it is OpenUIValue.Obj ||
                        it is OpenUIValue.Element ||
                        it is OpenUIValue.Expression
                } -> value
                else -> OpenUIValue.Obj(
                    OpenUIObject.build {
                        for ((key, nested) in fields) {
                            this[key] = evaluateProp(nested, context, schema, isReactive, recurseElement)
                        }
                    },
                )
            }
        }
    }
}
