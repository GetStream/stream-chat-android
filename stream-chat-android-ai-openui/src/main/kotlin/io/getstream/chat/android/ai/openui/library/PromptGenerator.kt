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

package io.getstream.chat.android.ai.openui.library

import io.getstream.chat.android.ai.openui.lang.Builtins
import io.getstream.chat.android.ai.openui.lang.JS
import io.getstream.chat.android.ai.openui.lang.OpenUIJson
import io.getstream.chat.android.ai.openui.lang.OpenUIObject
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.arrayValue
import io.getstream.chat.android.ai.openui.lang.get
import io.getstream.chat.android.ai.openui.lang.objectValue
import io.getstream.chat.android.ai.openui.lang.stringValue

/** A tool the model can call through `Query()`/`Mutation()` (shaped like an MCP tool). */
public data class ToolSpec(
    val name: String,
    val description: String? = null,
    val inputSchema: OpenUIValue = OpenUIValue.Obj(OpenUIObject.Empty),
    val outputSchema: OpenUIValue = OpenUIValue.Obj(OpenUIObject.Empty),
)

/** A tool listed in the prompt: just a name, or a full spec. */
public sealed interface ToolDescriptor {
    /** A tool listed by name only. */
    public data class Name(val name: String) : ToolDescriptor

    /** A tool listed with its description and schemas. */
    public data class Spec(val spec: ToolSpec) : ToolDescriptor
}

/** A named group of components in the prompt, with optional usage notes. */
public data class ComponentGroup(val name: String, val components: List<String>, val notes: List<String> = emptyList())

/** Options for system prompt generation (mirrors `PromptOptions` in `@openuidev/lang-core`). */
public data class PromptOptions(
    /** Replaces the default preamble. */
    val preamble: String? = null,
    val additionalRules: List<String> = emptyList(),
    /** Examples (static and layout patterns). */
    val examples: List<String> = emptyList(),
    /** Examples using `Query`/`Mutation`. */
    val toolExamples: List<String> = emptyList(),
    val tools: List<ToolDescriptor> = emptyList(),
    /** Incremental editing: the model outputs only changed statements. */
    val editMode: Boolean = false,
    /** Text plus fenced code responses: the model can answer without generating UI. */
    val inlineMode: Boolean = false,
    /** `Query()`, `Mutation()`, `@Run`. Defaults to `true` when tools are given. */
    val toolCalls: Boolean? = null,
    /** `$variables`, `@Set`, `@Reset`. Defaults to [toolCalls]. */
    val bindings: Boolean? = null,
    /** Lists the built-in functions (`@Count`, `@Sum`, ...). Defaults to `toolCalls || bindings`. */
    val builtinFunctions: Boolean? = null,
)

/**
 * A component, function or action as the prompt lists it: `signature — description` (port of
 * `ComponentPromptSpec`).
 */
public data class ComponentPromptSpec(val name: String, val signature: String, val description: String? = null)

/** Everything needed to render a system prompt (port of `PromptSpec`). */
public data class PromptSpec(
    val root: String?,
    val components: List<ComponentPromptSpec>,
    val componentGroups: List<ComponentGroup> = emptyList(),
    /** The library's functions, listed with the built-in ones. */
    val functions: List<ComponentPromptSpec> = emptyList(),
    /** The library's custom actions, listed with the built-in action steps. */
    val actions: List<ComponentPromptSpec> = emptyList(),
    val options: PromptOptions = PromptOptions(),
) {
    internal companion object {
        /** Reads upstream's `LibrarySpec` JSON (`library.toSpec()`). */
        fun fromLibrarySpec(spec: OpenUIValue, options: PromptOptions = PromptOptions()): PromptSpec =
            PromptSpec(
                root = spec["root"].stringValue,
                components = spec["components"].objectValue.orEmpty().map { (name, component) ->
                    ComponentPromptSpec(
                        name,
                        component["signature"].stringValue.orEmpty(),
                        component["description"].stringValue,
                    )
                },
                componentGroups = spec["componentGroups"].arrayValue.orEmpty().map { group ->
                    ComponentGroup(
                        name = group["name"].stringValue.orEmpty(),
                        components = group["components"].arrayValue.orEmpty().mapNotNull { it.stringValue },
                        notes = group["notes"].arrayValue.orEmpty().mapNotNull { it.stringValue },
                    )
                },
                functions = PromptGenerator.calls(spec["functions"]),
                actions = PromptGenerator.calls(spec["actions"]),
                options = options,
            )
    }
}

/** Generates the OpenUI Lang system prompt for [spec] (port of `generatePrompt`). */
public fun generatePrompt(spec: PromptSpec): String = PromptGenerator.generate(spec)

/** The system prompt generator behind [generatePrompt] (port of `prompt.ts`). */
@Suppress("TooManyFunctions")
internal object PromptGenerator {

    internal const val DEFAULT_PREAMBLE: String =
        "You are an AI assistant that responds using openui-lang, a declarative UI language. Your ENTIRE response " +
            "must be valid openui-lang code — no markdown, no explanations, just openui-lang."

    /** The system prompt for [spec]. */
    fun generate(spec: PromptSpec): String {
        val options = spec.options
        val rootName = spec.root ?: "Root"
        val hasTools = options.tools.isNotEmpty()
        val toolCalls = options.toolCalls ?: hasTools
        val bindings = options.bindings ?: toolCalls
        val supportsExpressions = toolCalls || bindings
        val usesActionExpression =
            spec.components.any { "ActionExpression" in it.signature || hasRestrictedSlot(it.signature) }

        val parts = mutableListOf<String>()
        parts += options.preamble ?: DEFAULT_PREAMBLE
        parts += ""
        parts += syntaxRules(rootName, supportsExpressions, bindings)
        parts += ""
        parts += componentSignatures(spec, toolCalls, bindings, usesActionExpression)

        // Library functions get the section alone when built-ins are not listed.
        val customFunctions = spec.functions.map(::functionLine)
        if (options.builtinFunctions ?: supportsExpressions) {
            parts += listOf("", builtinFunctionsSection(customFunctions, toolCalls))
        } else if (customFunctions.isNotEmpty()) {
            parts += listOf("", "## Built-in Functions\n\n" + customFunctions.joinToString("\n"))
        }
        if (toolCalls) parts += listOf("", PromptSections.query, "", PromptSections.mutation)
        // Library actions get the Action section alone when no component uses ActionExpression.
        val customActions = spec.actions.map { "- " + functionLine(it) }
        if (usesActionExpression) {
            parts += listOf("", actionSection(toolCalls, bindings, customActions))
        } else if (customActions.isNotEmpty()) {
            parts += listOf(
                "",
                "$ACTION_HEADER\n\nPass one step, or Action([...]) with several steps, as a button's action.\n\n" +
                    "Available steps:\n" + customActions.joinToString("\n"),
            )
        }
        if (toolCalls && bindings) parts += listOf("", PromptSections.interactiveFilters)
        if (toolCalls) parts += listOf("", PromptSections.toolWorkflow)
        if (hasTools) parts += listOf("", toolsSection(options.tools))

        parts += ""
        parts += streamingRules(rootName, supportsExpressions, toolCalls)

        val allExamples = options.examples + options.toolExamples
        if (allExamples.isNotEmpty()) {
            parts += listOf("", "## Examples", "")
            for (example in allExamples) parts += listOf(example, "")
        }

        if (options.editMode) parts += listOf("", PromptSections.editMode)
        if (options.inlineMode) parts += listOf("", PromptSections.inlineMode)

        parts += importantRules(rootName, toolCalls, bindings)

        if (options.additionalRules.isNotEmpty()) {
            parts += ""
            options.additionalRules.forEach { parts += "- $it" }
        }
        return parts.joinToString("\n")
    }

    // JSON Schema helpers ---------------------------------------------------------------------

    internal fun jsonSchemaTypeString(schema: OpenUIValue?): String = when (schema["type"].stringValue) {
        "string" -> schema["enum"].arrayValue?.joinToString(" | ") { "\"${JS.toString(it)}\"" } ?: "string"
        "number", "integer" -> "number"
        "boolean" -> "boolean"
        "array" -> schema["items"].let { if (JS.isNullish(it)) "any[]" else "${jsonSchemaTypeString(it)}[]" }
        "object" -> {
            val properties = schema["properties"].objectValue
            if (properties.isNullOrEmpty()) {
                "object"
            } else {
                val required = schema["required"].arrayValue.orEmpty().mapNotNull { it.stringValue }
                properties.entries.joinToString(", ", "{", "}") { (key, value) ->
                    "$key${if (key in required) "" else "?"}: ${jsonSchemaTypeString(value)}"
                }
            }
        }
        else -> "any"
    }

    internal fun defaultValue(schema: OpenUIValue?): OpenUIValue = when (schema["type"].stringValue) {
        "string" -> OpenUIValue.Str("")
        "number", "integer" -> OpenUIValue.Num(0.0)
        "boolean" -> OpenUIValue.False
        "array" -> OpenUIValue.EmptyArray
        "object" -> {
            val properties = schema["properties"].objectValue
            if (properties.isNullOrEmpty()) {
                OpenUIValue.Obj(OpenUIObject.Empty)
            } else {
                OpenUIValue.Obj(properties.mapValues(::defaultValue))
            }
        }
        else -> OpenUIValue.Null
    }

    // Sections ----------------------------------------------------------------------------------

    private fun String.fill(vararg values: Pair<String, String>): String =
        values.fold(this) { text, (key, value) -> text.replace("{{$key}}", value) }

    internal fun syntaxRules(rootName: String, supportsExpressions: Boolean, bindings: Boolean): String {
        val lines = mutableListOf(
            "## Syntax Rules",
            "",
            "1. Each statement is on its own line: `identifier = Expression`",
            "2. `root` is the entry point — every program must define `root = $rootName(...)`",
            "3. Expressions are: strings (\"...\"), numbers, booleans (true/false), null, arrays ([...]), objects " +
                "({...}), or component calls TypeName(arg1, arg2, ...)",
            "4. Use references for readability: define `name = ...` on one line, then use `name` later",
            "5. EVERY variable (except root) MUST be referenced by at least one other variable. Unreferenced " +
                "variables are silently dropped and will NOT render. Always include defined variables in their " +
                "parent's children/items array.",
            "6. Arguments are POSITIONAL (order matters, not names). Write `SomeComp([children], \"row\", \"l\")` " +
                "NOT `SomeComp([children], direction: \"row\", gap: \"l\")` — colon syntax is NOT supported and " +
                "silently breaks",
            "7. Optional arguments can be omitted from the end",
        )
        var ruleNumber = 8
        if (bindings) {
            lines +=
                "$ruleNumber. Declare mutable state with `\$varName = defaultValue`. Components marked with " +
                "`\$binding` can read/write these. Undeclared \$variables are auto-created with null default."
            ruleNumber++
        }
        if (supportsExpressions) {
            val rules = listOf(
                "String concatenation: `\"text\" + \$var + \"more\"`",
                "Dot member access: `query.field` reads a field; on arrays it extracts that field from every element",
                "Index access: `arr[0]`, `data[index]`",
                "Arithmetic operators: +, -, *, /, % (work on numbers; + is string concat when either side is a " +
                    "string)",
                "Comparison: ==, !=, >, <, >=, <=",
                "Logical: &&, ||, ! (prefix)",
                "Ternary: `condition ? valueIfTrue : valueIfFalse`",
                "Parentheses for grouping: `(a + b) * c`",
            )
            for (rule in rules) lines += "${ruleNumber++}. $rule"
        }
        lines += "- Strings use double quotes with backslash escaping"
        return lines.joinToString("\n")
    }

    // The built-in functions' prompt signatures, as upstream generates them from their schemas.
    private val builtinFunctionSignatures: Map<String, String> = mapOf(
        "Count" to "Count(array: any[]) → number",
        "First" to "First(array: any[]) → any",
        "Last" to "Last(array: any[]) → any",
        "Sum" to "Sum(numbers: number[]) → number",
        "Avg" to "Avg(numbers: number[]) → number",
        "Min" to "Min(numbers: number[]) → number",
        "Max" to "Max(numbers: number[]) → number",
        "Sort" to "Sort(array: any[], field: string, direction?: \"asc\" | \"desc\") → any[]",
        "Filter" to "Filter(array: any[], field: string, operator: \"==\" | \"!=\" | \">\" | \"<\" | \">=\" | " +
            "\"<=\" | \"contains\", value: any) → any[]",
        "Round" to "Round(number: number, decimals?: number) → number",
        "Abs" to "Abs(number: number) → number",
        "Floor" to "Floor(number: number) → number",
        "Ceil" to "Ceil(number: number) → number",
    )

    private const val EACH_SIGNATURE = "Each(array: any[], varName: string, template: any)"

    private const val ACTION_HEADER = "## Action — Button Behavior"

    // The built-in action steps, in prompt order.
    private val builtinSteps: List<ComponentPromptSpec> = listOf(
        ComponentPromptSpec(
            "Run",
            "Run(ref: Query | Mutation)",
            "Execute a Mutation or re-fetch a Query (ref must be a declared Query/Mutation)",
        ),
        ComponentPromptSpec(
            "ToAssistant",
            "ToAssistant(message: string, context?: any)",
            "Send a message to the assistant (for conversational buttons like \"Tell me more\", \"Explain this\"). " +
                "The optional context is any value (object, array, string, number) passed to the assistant as " +
                "hidden data, e.g. @ToAssistant(\"Show details\", {orderId: 42})",
        ),
        ComponentPromptSpec("OpenUrl", "OpenUrl(url: string)", "Navigate to a URL"),
        ComponentPromptSpec("Set", "Set(variable: \$variable, value: any)", "Set a \$variable to a specific value"),
        ComponentPromptSpec(
            "Reset",
            "Reset(variable: \$variable)",
            "Reset \$variables to their declared defaults; pass several to reset each (e.g. @Reset(\$title, " +
                "\$priority) restores \$title=\"\" and \$priority=\"medium\")",
        ),
    )

    /** The built-in steps the prompt lists: `@Run` needs tool calls, `@Set` and `@Reset` bindings. */
    private fun listedSteps(toolCalls: Boolean, bindings: Boolean): List<ComponentPromptSpec> =
        builtinSteps.filter { step ->
            when (step.name) {
                "Run" -> toolCalls
                "Set", "Reset" -> bindings
                else -> true
            }
        }

    private fun functionLine(function: ComponentPromptSpec): String =
        if (function.description.isNullOrEmpty()) {
            "@${function.signature}"
        } else {
            "@${function.signature} — ${function.description}"
        }

    private val restrictedSlot = Regex("[:|] @[A-Z]")

    /** Whether a signature has a slot that takes only some steps: `share?: @CopyToClipboard | @OpenUrl`. */
    private fun hasRestrictedSlot(signature: String): Boolean = restrictedSlot.containsMatchIn(signature)

    internal fun builtinFunctionsSection(custom: List<String>, toolCalls: Boolean): String {
        val builtins = Builtins.definitions.map { definition ->
            functionLine(
                ComponentPromptSpec(
                    definition.name,
                    builtinFunctionSignatures[definition.name] ?: definition.signature,
                    definition.description,
                ),
            )
        }
        val lazy = Builtins.lazyDefinitions.map { (_, description) ->
            functionLine(ComponentPromptSpec("Each", EACH_SIGNATURE, description))
        }
        return PromptSections.builtinFunctions.fill(
            "target" to if (toolCalls) "Query results" else "data",
            "lines" to (builtins + lazy + custom).joinToString("\n"),
        )
    }

    internal fun actionSection(toolCalls: Boolean, bindings: Boolean, custom: List<String>): String {
        val steps = listedSteps(toolCalls, bindings).map { "- " + functionLine(it) } + custom
        val examples = buildList {
            if (toolCalls) add(PromptSections.actionMutationExample)
            add(PromptSections.actionNavExample)
        }
        val rules = buildList {
            add(
                "- An action can be assigned to a variable or inlined: Button(\"Go\", onSubmit) and Button(\"Go\", " +
                    "Action([...])) both work",
            )
            if (toolCalls) {
                add("- If a @Run(mutation) step fails, remaining steps are skipped (halt on failure)")
                add("- @Run(queryRef) re-fetches the query (fire-and-forget, cannot fail)")
            }
        }
        return PromptSections.action.fill(
            "steps" to steps.joinToString("\n"),
            "examples" to examples.joinToString("\n\n"),
            "rules" to rules.joinToString("\n"),
        )
    }

    internal fun streamingRules(rootName: String, supportsExpressions: Boolean, toolCalls: Boolean): String {
        val steps = mutableListOf("1. `root = $rootName(...)` — UI shell appears immediately")
        if (supportsExpressions) {
            steps += "2. \$variable declarations — state ready for bindings"
            if (toolCalls) steps += "3. Query statements — defaults resolve immediately so components render with data"
            val count = steps.size
            steps += "${count + 1}. Component definitions — fill in with data already available"
            steps += "${count + 2}. Data values — leaf content last"
        } else {
            steps += "2. Component definitions — fill in as they stream"
            steps += "3. Data values — leaf content last"
        }
        return PromptSections.streaming.fill("steps" to steps.joinToString("\n"), "rootName" to rootName)
    }

    internal fun importantRules(rootName: String, toolCalls: Boolean, bindings: Boolean): String {
        val lines = mutableListOf(
            "1. root = $rootName(...) is the FIRST line (for optimal streaming).",
            "2. Every referenced name is defined. Every defined name (other than root) is reachable from root.",
        )
        if (toolCalls) lines += "3. Every Query result is referenced by at least one component."
        if (bindings) {
            lines +=
                "${if (toolCalls) "4" else "3"}. Every \$binding appears in at least one component or expression."
        }
        if (toolCalls && bindings) {
            lines += "5. Every visible filter \$binding appears in at least one Query args object."
        }
        return PromptSections.importantRules.fill("verifyLines" to lines.joinToString("\n"))
    }

    internal fun toolSignature(tool: ToolSpec): String {
        val properties = tool.inputSchema["properties"].objectValue
        val args = if (properties.isNullOrEmpty()) {
            ""
        } else {
            val required = tool.inputSchema["required"]
                .arrayValue
                .orEmpty()
                .mapNotNull { it.stringValue }
            properties.entries.joinToString(", ") { (key, value) ->
                "$key${if (key in required) "" else "?"}: ${jsonSchemaTypeString(value)}"
            }
        }
        var line = "- ${tool.name}($args) → ${jsonSchemaTypeString(tool.outputSchema)}"
        if (!tool.description.isNullOrEmpty()) line += "\n  ${tool.description}"
        return line
    }

    internal fun toolsSection(tools: List<ToolDescriptor>): String {
        val lines = mutableListOf(
            "## Available Tools",
            "",
            "Use these with Query() for read operations or Mutation() for write operations. The LLM decides which is " +
                "appropriate based on the tool's purpose.",
            "",
        )
        tools.filterIsInstance<ToolDescriptor.Name>().forEach { lines += "- ${it.name}" }
        val specs = tools.filterIsInstance<ToolDescriptor.Spec>().map { it.spec }
        specs.forEach { lines += toolSignature(it) }
        if (specs.isNotEmpty()) {
            lines +=
                listOf("", "### Default values for Query results", "", "Use these shapes as minimal Query defaults:")
            specs.forEach { lines += "- ${it.name}: `${OpenUIJson.stringify(defaultValue(it.outputSchema))}`" }
        }
        lines += ""
        lines +=
            "CRITICAL: Use ONLY the tools listed above in Query() and Mutation() calls. Do NOT invent or guess tool " +
            "names. If the user asks for functionality that doesn't match any available tool, use realistic mock " +
            "data instead of fabricating a tool call."
        return lines.joinToString("\n")
    }

    @Suppress("LoopWithTooManyJumpStatements", "NestedBlockDepth")
    internal fun componentSignatures(
        spec: PromptSpec,
        toolCalls: Boolean,
        bindings: Boolean,
        usesActionExpression: Boolean,
    ): String {
        val lines = mutableListOf(
            "## Component Signatures",
            "",
            "Arguments marked with ? are optional. Sub-components can be inline or referenced; prefer references for " +
                "better streaming.",
        )
        if (usesActionExpression) {
            val steps = (listedSteps(toolCalls, bindings).map { it.name } + spec.actions.map { it.name }).map { "@$it" }
            lines += "Props typed `ActionExpression` accept one @step, or Action([@steps...]) for several. See the " +
                "Action section for available steps (${steps.joinToString(", ")})."
            if (spec.components.any { hasRestrictedSlot(it.signature) }) {
                lines += "Props typed like `@OpenUrl | @ToAssistant` accept only the steps listed."
            }
        }
        if (bindings || spec.components.any { "\$binding" in it.signature }) {
            lines += "Props marked `\$binding<type>` accept a `\$variable` reference for two-way binding."
        }

        fun format(component: ComponentPromptSpec): String =
            if (component.description.isNullOrEmpty()) {
                component.signature
            } else {
                "${component.signature} — " +
                    "${component.description}"
            }

        val byName = LinkedHashMap<String, ComponentPromptSpec>()
        spec.components.forEach { byName.putIfAbsent(it.name, it) }
        if (spec.componentGroups.isNotEmpty()) {
            val grouped = HashSet<String>()
            for (group in spec.componentGroups) {
                lines += listOf("", "### ${group.name}")
                for (name in group.components) {
                    if (name in grouped) continue
                    val component = byName[name] ?: continue
                    grouped += name
                    lines += format(component)
                }
                lines += group.notes
            }
            val ungrouped = spec.components.filter { it.name !in grouped }
            if (ungrouped.isNotEmpty()) {
                lines += listOf("", "### Other")
                lines += ungrouped.map(::format)
            }
        } else {
            lines += ""
            lines += spec.components.map(::format)
        }
        return lines.joinToString("\n")
    }

    internal fun calls(section: OpenUIValue?): List<ComponentPromptSpec> =
        section.objectValue.orEmpty().map { (name, call) ->
            ComponentPromptSpec(name, call["signature"].stringValue.orEmpty(), call["description"].stringValue)
        }
}
