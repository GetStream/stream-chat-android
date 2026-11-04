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

import java.text.Collator
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow

/** Runtime action step type identifiers (the values of `ACTION_STEPS` upstream). */
internal object ActionStepType {
    /** Runs a `Query` refresh or a `Mutation` (`@Run`). */
    const val RUN: String = "run"

    /** Sends a message to the assistant (`@ToAssistant`). */
    const val CONTINUE_CONVERSATION: String = "continue_conversation"

    /** Opens a URL (`@OpenUrl`). */
    const val OPEN_URL: String = "open_url"

    /** Assigns a `$variable` (`@Set`). */
    const val SET: String = "set"

    /** Restores `$variables` to their declared defaults (`@Reset`). */
    const val RESET: String = "reset"
}

/** The `@`-prefixed builtin functions and action steps of OpenUI Lang (port of `builtins.ts`). */
internal object Builtins {
    // The position of @Filter's value among its arguments.
    private const val FILTER_TARGET_ARG = 3

    /** A data builtin: its prompt signature/description and runtime implementation. */
    class Definition internal constructor(
        /** PascalCase name, e.g. `Count`. */
        val name: String,
        /** Signature for prompt docs, e.g. `Count(array) → number`. */
        val signature: String,
        /** One-line description for prompt docs. */
        val description: String,
        internal val function: (List<OpenUIValue?>) -> OpenUIValue,
    )

    /** Data builtins in registry order (the order matters for prompt generation). */
    val definitions: List<Definition> = listOf(
        Definition("Count", "Count(array) → number", "Returns array length") { args ->
            OpenUIValue.Num(args.arrayAt(0)?.size?.toDouble() ?: 0.0)
        },
        Definition("First", "First(array) → element", "Returns first element of array") { args ->
            args.arrayAt(0)?.firstOrNull() ?: OpenUIValue.Null
        },
        Definition("Last", "Last(array) → element", "Returns last element of array") { args ->
            args.arrayAt(0)?.lastOrNull() ?: OpenUIValue.Null
        },
        Definition("Sum", "Sum(numbers[]) → number", "Sum of numeric array") { args ->
            OpenUIValue.Num(args.arrayAt(0)?.sumOf { JS.toNumber(it) } ?: 0.0)
        },
        Definition("Avg", "Avg(numbers[]) → number", "Average of numeric array") { args ->
            val items = args.arrayAt(0)
            OpenUIValue.Num(if (items.isNullOrEmpty()) 0.0 else items.sumOf { JS.toNumber(it) } / items.size)
        },
        Definition("Min", "Min(numbers[]) → number", "Minimum value in array") { args ->
            val items = args.arrayAt(0)
            OpenUIValue.Num(
                if (items.isNullOrEmpty()) {
                    0.0
                } else {
                    items.fold(
                        JS.toNumber(items[0]),
                    ) { acc, item -> JS.min(acc, JS.toNumber(item)) }
                },
            )
        },
        Definition("Max", "Max(numbers[]) → number", "Maximum value in array") { args ->
            val items = args.arrayAt(0)
            OpenUIValue.Num(
                if (items.isNullOrEmpty()) {
                    0.0
                } else {
                    items.fold(
                        JS.toNumber(items[0]),
                    ) { acc, item -> JS.max(acc, JS.toNumber(item)) }
                },
            )
        },
        Definition(
            "Sort",
            "Sort(array, field, direction?) → sorted array",
            "Sort array by field. Direction: \"asc\" (default) or \"desc\"",
            ::sort,
        ),
        Definition(
            "Filter",
            "Filter(array, field, operator: \"==\" | \"!=\" | \">\" | \"<\" | \">=\" | \"<=\" | \"contains\", value) " +
                "→ filtered array",
            "Filter array by field value",
            ::filter,
        ),
        Definition("Round", "Round(number, decimals?) → number", "Round to N decimal places (default 0)") { args ->
            val number = JS.toNumber(args.getOrNull(0))
            val decimals = args.getOrNull(1).let { if (JS.isNullish(it)) 0.0 else JS.toNumber(it) }
            val factor = 10.0.pow(decimals)
            OpenUIValue.Num(JS.round(number * factor) / factor)
        },
        Definition("Abs", "Abs(number) → number", "Absolute value") { args ->
            OpenUIValue.Num(abs(JS.toNumber(args.getOrNull(0))))
        },
        Definition("Floor", "Floor(number) → number", "Round down to nearest integer") { args ->
            OpenUIValue.Num(floor(JS.toNumber(args.getOrNull(0))))
        },
        Definition("Ceil", "Ceil(number) → number", "Round up to nearest integer") { args ->
            OpenUIValue.Num(ceil(JS.toNumber(args.getOrNull(0))))
        },
    )

    internal val definitionsByName: Map<String, Definition> = definitions.associateBy { it.name }

    /** Lazy builtins receive AST nodes and control their own evaluation. */
    internal val lazyNames: Set<String> = setOf("Each")

    /** Prompt docs for lazy builtins: signature to description. */
    val lazyDefinitions: List<Pair<String, String>> = listOf(
        "Each(array, varName, template)" to
            "Evaluate template for each element. varName is the loop variable — use it ONLY inside the template " +
            "expression (inline). Do NOT create a separate statement for the template.",
    )

    /** Parser-level action step names → runtime step type values. */
    internal val actionSteps: Map<String, String> = linkedMapOf(
        "Run" to ActionStepType.RUN,
        "ToAssistant" to ActionStepType.CONTINUE_CONVERSATION,
        "OpenUrl" to ActionStepType.OPEN_URL,
        "Set" to ActionStepType.SET,
        "Reset" to ActionStepType.RESET,
    )

    /** All action expression names (the steps plus the `Action` container). */
    internal val actionNames: Set<String> = setOf("Action") + actionSteps.keys

    private val allNames: Set<String> = definitionsByName.keys + lazyNames + actionNames

    /** Whether `name` is a builtin function or action expression (not a component). */
    fun isBuiltin(name: String): Boolean = name in allNames

    // Positional parameter names of the built-in functions and steps, as upstream defines them.
    private val functionParams: Map<String, List<String>> = mapOf(
        "Count" to listOf("array"),
        "First" to listOf("array"),
        "Last" to listOf("array"),
        "Sum" to listOf("numbers"),
        "Avg" to listOf("numbers"),
        "Min" to listOf("numbers"),
        "Max" to listOf("numbers"),
        "Sort" to listOf("array", "field", "direction"),
        "Filter" to listOf("array", "field", "operator", "value"),
        "Round" to listOf("number", "decimals"),
        "Abs" to listOf("number"),
        "Floor" to listOf("number"),
        "Ceil" to listOf("number"),
        "Each" to listOf("array", "varName", "template"),
    )

    // Step name → (params, isLazy).
    private val stepParams: Map<String, Pair<List<String>, Boolean>> = mapOf(
        "Run" to (listOf("ref") to true),
        "ToAssistant" to (listOf("message", "context") to false),
        "OpenUrl" to (listOf("url") to false),
        "Set" to (listOf("variable", "value") to true),
        "Reset" to (listOf("variable") to true),
    )

    /** The built-in functions, steps and `Action`: the base of every library's call registry. */
    val calls: Map<String, CallDef> = buildMap {
        fun params(names: List<String>) = names.map { ParamDef(it, required = false) }
        for ((name, names) in functionParams) {
            put(name, CallDef(CallDef.Kind.Function, params(names), isBuiltin = true, isLazy = name in lazyNames))
        }
        for ((name, step) in stepParams) {
            put(name, CallDef(CallDef.Kind.Action, params(step.first), isBuiltin = true, isLazy = step.second))
        }
        put(
            "Action",
            CallDef(CallDef.Kind.Action, listOf(ParamDef("steps", required = true)), isBuiltin = true, isLazy = true),
        )
    }

    internal const val QUERY: String = "Query"
    internal const val MUTATION: String = "Mutation"

    /** Whether `name` is a reserved statement call (`Query`, `Mutation`). */
    fun isReservedCall(name: String): Boolean = name == QUERY || name == MUTATION

    // Implementations ----------------------------------------------------------------------

    private fun List<OpenUIValue?>.arrayAt(index: Int): List<OpenUIValue>? = getOrNull(index).arrayValue

    /** Resolves a field path on a value. Supports dot paths: `"state.name"`. */
    internal fun resolveField(value: OpenUIValue?, path: String): OpenUIValue? {
        if (path.isEmpty() || JS.isNullish(value)) return null
        if ('.' !in path) return JS.property(value, path)
        var current = value
        for (part in path.split('.')) {
            if (JS.isNullish(current)) return null
            current = JS.property(current, part)
        }
        return current
    }

    private fun isNumericLike(value: OpenUIValue?): Boolean = when (value) {
        is OpenUIValue.Num -> true
        is OpenUIValue.Str -> value.value.isNotEmpty() && !JS.stringToNumber(value.value).isNaN()
        else -> false
    }

    private fun sort(args: List<OpenUIValue?>): OpenUIValue {
        val first = args.getOrNull(0)
        val items = first.arrayValue ?: return first ?: OpenUIValue.Null
        val field = JS.toStringOrEmpty(args.getOrNull(1))
        val descending = args.getOrNull(2).let { if (JS.isNullish(it)) "asc" else JS.toString(it) } == "desc"
        val collator = Collator.getInstance()

        fun compare(a: OpenUIValue, b: OpenUIValue): Double {
            val av = if (field.isEmpty()) a else resolveField(a, field)
            val bv = if (field.isEmpty()) b else resolveField(b, field)
            if (isNumericLike(av) && isNumericLike(bv)) {
                val diff = JS.toNumber(av) - JS.toNumber(bv)
                return if (descending) -diff else diff
            }
            val order = collator.compare(JS.toStringOrEmpty(av), JS.toStringOrEmpty(bv)).toDouble()
            return if (descending) -order else order
        }

        // Stable, like Array.prototype.sort.
        return OpenUIValue.Arr(
            items.sortedWith { a, b ->
                val order = compare(a, b)
                when {
                    order < 0 -> -1
                    order > 0 -> 1
                    else -> 0
                }
            },
        )
    }

    private fun filter(args: List<OpenUIValue?>): OpenUIValue {
        val items = args.arrayAt(0) ?: return OpenUIValue.EmptyArray
        val field = JS.toStringOrEmpty(args.getOrNull(1))
        val op = args.getOrNull(2).let { if (JS.isNullish(it)) "==" else JS.toString(it) }
        val target = args.getOrNull(FILTER_TARGET_ARG)
        return OpenUIValue.Arr(
            items.filter { item ->
                val value = if (field.isEmpty()) item else resolveField(item, field)
                when (op) {
                    "==" -> JS.looseEquals(value, target)
                    "!=" -> !JS.looseEquals(value, target)
                    ">" -> JS.toNumber(value) > JS.toNumber(target)
                    "<" -> JS.toNumber(value) < JS.toNumber(target)
                    ">=" -> JS.toNumber(value) >= JS.toNumber(target)
                    "<=" -> JS.toNumber(value) <= JS.toNumber(target)
                    "contains" -> JS.toStringOrEmpty(target) in JS.toStringOrEmpty(value)
                    else -> false
                }
            },
        )
    }
}
