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
import io.getstream.chat.android.ai.openui.lang.LibrarySchema
import io.getstream.chat.android.ai.openui.lang.OpenUIObject
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.ParamMap

/**
 * The model-facing half of a component library: the components' schemas, how the prompt groups
 * them, the prompt examples and rules recommended for it, and the app's own functions and actions
 * (mirrors `createLibrary` in `@openuidev/react-lang`, without the renderers).
 *
 * [chat] and [standard] are the two built-in libraries. A renderer pairs a definition with its
 * components' UI; a server can use [systemPrompt] or [specJson] on its own.
 *
 * A function or action that shares its name with a built-in, a component or another call is left
 * out.
 */
internal class LibraryDefinition(
    /** Root component name (`Card` for chat, `Stack` for the standard library). */
    val root: String?,
    components: List<ComponentSchema>,
    /** How the prompt groups the components. */
    val componentGroups: List<ComponentGroup> = emptyList(),
    /** Prompt examples and rules used by [systemPrompt] when no options are given. */
    val defaultPromptOptions: PromptOptions = PromptOptions(),
    functions: List<DefinedFunction> = emptyList(),
    customActions: List<DefinedAction> = emptyList(),
) {
    /** Components in library order; a later schema with the same name replaces the earlier one in place. */
    val components: List<ComponentSchema>

    /** The app's functions, called as `@Name(...)` in programs. */
    val functions: List<DefinedFunction>

    /** The app's action steps, used as `@Name(...)` in action props. */
    val customActions: List<DefinedAction>

    private val byName: Map<String, ComponentSchema>

    init {
        val ordered = LinkedHashMap<String, ComponentSchema>()
        components.forEach { ordered[it.name] = it }
        this.components = ordered.values.toList()
        byName = ordered
        if (root != null) require(root in byName) { "Root component \"$root\" was not found in components." }
        // A function or action can't share a name with a built-in, a component or another call.
        val names = HashSet(byName.keys)
        fun claim(name: String) = !Builtins.isBuiltin(name) && !Builtins.isReservedCall(name) && names.add(name)
        this.functions = functions.filter { claim(it.name) }
        this.customActions = customActions.filter { claim(it.name) }
    }

    /** Component names in library order. */
    val componentNames: List<String> get() = components.map { it.name }

    /** The schema of component [name], or `null` when the library has none. */
    fun component(name: String): ComponentSchema? = byName[name]

    /** The library JSON Schema (`library.toJSONSchema()`), with its functions and actions. */
    val jsonSchema: OpenUIValue by lazy { buildJsonSchema(this.components, this.functions, this.customActions) }

    private val reactive: Map<String, Set<String>> by lazy { byName.mapValues { it.value.reactiveProps } }

    /** What the parser and runtime need: the JSON Schema, the root, the reactive props and the calls. */
    val schema: LibrarySchema by lazy {
        LibrarySchema(
            jsonSchema = jsonSchema,
            root = root,
            reactiveProps = { component, prop -> reactive[component]?.contains(prop) == true },
            functions = functions,
            customActions = customActions,
        )
    }

    /** Compiled positional parameters and calls, used by the parser. */
    val paramMap: ParamMap get() = schema.paramMap

    /** Whether [prop] of [component] accepts a `$variable` binding. */
    fun isReactiveProp(component: String, prop: String): Boolean = reactive[component]?.contains(prop) == true

    /** The system prompt for this library; [defaultPromptOptions] when [options] is `null`. */
    fun systemPrompt(options: PromptOptions? = null): String =
        PromptGenerator.generate(promptSpec(options ?: defaultPromptOptions))

    /** Everything the prompt generator needs, with [options]. */
    fun promptSpec(options: PromptOptions): PromptSpec = PromptSpec(
        root = root,
        components = components.map { ComponentPromptSpec(it.name, it.signature, it.description) },
        componentGroups = componentGroups,
        functions = functions.map { ComponentPromptSpec(it.name, it.signature, it.description) },
        actions = customActions.map { ComponentPromptSpec(it.name, it.signature, it.description) },
        options = options,
    )

    /**
     * The library in the `LibrarySpec` format of `@openuidev/lang-core` (`library.toSpec()`),
     * for generating prompts on a server with `generateSystemPrompt({ library })`.
     */
    val specJson: OpenUIValue by lazy {
        fun entry(signature: String, description: String) = obj("signature" to signature, "description" to description)
        OpenUIValue.Obj(
            OpenUIObject.build {
                root?.let { this["root"] = OpenUIValue.Str(it) }
                this["components"] = OpenUIValue.Obj(
                    OpenUIObject.build { components.forEach { this[it.name] = entry(it.signature, it.description) } },
                )
                if (componentGroups.isNotEmpty()) {
                    this["componentGroups"] = OpenUIValue.Arr(componentGroups.map(::groupJson))
                }
                if (functions.isNotEmpty()) {
                    this["functions"] = OpenUIValue.Obj(
                        OpenUIObject.build {
                            functions.forEach { this[it.name] = entry(it.signature, it.description) }
                        },
                    )
                }
                if (customActions.isNotEmpty()) {
                    this["actions"] = OpenUIValue.Obj(
                        OpenUIObject.build {
                            customActions.forEach { this[it.name] = entry(it.signature, it.description) }
                        },
                    )
                }
                this["schema"] = jsonSchema
            },
        )
    }

    private fun groupJson(group: ComponentGroup): OpenUIValue = OpenUIValue.Obj(
        OpenUIObject.build {
            this["name"] = OpenUIValue.Str(group.name)
            this["components"] = OpenUIValue.Arr(group.components.map(OpenUIValue::Str))
            if (group.notes.isNotEmpty()) this["notes"] = OpenUIValue.Arr(group.notes.map(OpenUIValue::Str))
        },
    )

    // Customization -----------------------------------------------------------------------------

    /**
     * A library with [components] added. A component is left out when the library already has
     * one of that name, or its name can't be called in a program: it must start with an upper case
     * letter and not be a built-in, `Query` or `Mutation`.
     */
    fun adding(
        components: List<ComponentSchema>,
        groups: List<ComponentGroup> = emptyList(),
    ): LibraryDefinition = copy(
        components = this.components + addable(components),
        componentGroups = componentGroups + groups,
    )

    /**
     * A library without the components named [names]. They are also removed from every slot that
     * takes several types, and groups left empty are dropped. The root component, and a type that
     * is the only one a slot takes, stay. Default prompt examples that call a removed component, and
     * group notes and additional rules that name one, are dropped too.
     */
    fun removing(names: Set<String>): LibraryDefinition {
        var removed = names - setOfNotNull(root)
        // A component that is all a remaining slot takes stays, and so do the ones its slots need.
        while (true) {
            val needed = components.filter { it.name !in removed }
                .flatMap { schema -> schema.props.flatMap { it.type.neededComponents(removed) } }
            val next = removed - needed.toSet()
            if (next == removed) break
            removed = next
        }
        val remaining = components.filter { it.name !in removed }.map { schema ->
            schema.copy(props = schema.props.map { it.copy(type = it.type.removingComponents(removed)) })
        }
        val gone = components.map { it.name }.filter { it in removed }
        val mention = mentionOf(gone)
        val groups = componentGroups.mapNotNull { group ->
            group.components.filter { it !in removed }.takeIf { it.isNotEmpty() }?.let { kept ->
                group.copy(components = kept, notes = group.notes.filterNot { mention?.containsMatchIn(it) == true })
            }
        }
        return copy(
            components = remaining,
            componentGroups = groups,
            defaultPromptOptions = defaultPromptOptions.without(gone),
        )
    }

    /** Matches a whole-word mention of a component of [names], or `null` when there are none. */
    private fun mentionOf(names: List<String>): Regex? = names.takeIf { it.isNotEmpty() }
        ?.joinToString("|") { Regex.escape(it) }
        ?.let { Regex("\\b(?:$it)\\b") }

    /** These options without the examples that call, and the rules that name, a component of [names]. */
    private fun PromptOptions.without(names: List<String>): PromptOptions {
        val mention = mentionOf(names) ?: return this
        val alternatives = names.joinToString("|") { Regex.escape(it) }
        val call = Regex("\\b(?:$alternatives)\\(")
        return copy(
            additionalRules = additionalRules.filterNot { mention.containsMatchIn(it) },
            examples = examples.filterNot { call.containsMatchIn(it) },
            toolExamples = toolExamples.filterNot { call.containsMatchIn(it) },
        )
    }

    /**
     * A library with [components] added and placeable wherever the library's own content goes,
     * such as the children of a chat `Card` or the content of a `TabItem`. They are listed in the
     * prompt under [group], followed by [notes]. A component is left out when the library already
     * has one of that name, or its name can't be called in a program.
     */
    fun addingContent(
        components: List<ComponentSchema>,
        group: String = "App components",
        notes: List<String> = emptyList(),
    ): LibraryDefinition {
        val added = addable(components)
        if (added.isEmpty()) return this
        val names = added.map { it.name }
        val widened = this.components.map { schema ->
            schema.copy(props = schema.props.map { it.copy(type = it.type.addingContentComponents(names)) })
        }
        return copy(
            components = widened + added,
            componentGroups = componentGroups + ComponentGroup(group, names, notes),
        )
    }

    /**
     * The library with upstream's `library.extend` applied: [add] new components (placed in the
     * content slots given with them, if any), [override] components by name, [remove] components by
     * name, and append [functions] and [actions].
     *
     * @throws IllegalArgumentException Like upstream, when an added component already exists, an
     * overridden or removed one doesn't, the root or a slot's only type is removed, or a slot can't
     * be resolved. Also when an added component's name can't be called in a program, or is taken
     * by a function or action.
     */
    fun extended(
        add: List<Pair<ComponentSchema, List<String>?>>,
        override: List<ComponentSchema>,
        remove: List<String>,
        functions: List<DefinedFunction>,
        actions: List<DefinedAction>,
    ): LibraryDefinition {
        val byName = LinkedHashMap<String, ComponentSchema>()
        components.forEach { byName[it.name] = it }
        val known = HashSet(byName.keys)
        val touched = HashSet<String>()
        val slotRequests = LinkedHashMap<String, List<String>>()
        val callNames = (this.functions + functions).map { it.name } + (customActions + actions).map { it.name }
        for ((schema, slots) in add) {
            if (schema.name in byName) extendFailure("Component \"${schema.name}\" already exists. Use override.")
            checkCallable(schema.name, callNames)
            if (slots != null && slots.isEmpty()) {
                extendFailure("\"slots\" for \"${schema.name}\" is empty. Omit it to only list it.")
            }
            byName[schema.name] = schema
            touched += schema.name
            slots?.let { slotRequests[schema.name] = it }
        }
        val overridden = HashSet<String>()
        for (schema in override) {
            if (schema.name !in byName) {
                extendFailure("Cannot override \"${schema.name}\": not in the library. Use add.")
            }
            if (!overridden.add(schema.name)) extendFailure("Component \"${schema.name}\" is overridden twice.")
            byName[schema.name] = schema
            touched += schema.name
        }
        known += byName.keys
        val removed = remove.toSet()
        for (name in removed) {
            if (name in touched) extendFailure("Cannot remove \"$name\": it is added or overridden in the same call.")
            if (byName.remove(name) == null) extendFailure("Cannot remove \"$name\": not in the library.")
            if (name == root) extendFailure("Cannot remove \"$name\": it is the library root.")
        }
        val placements = placeInSlots(slotRequests, byName, known)
        val rebuilt = byName.values.map { schema ->
            schema.copy(
                props = schema.props.map { prop ->
                    val path = "${schema.name}.${prop.name}"
                    prop.copy(type = prop.type.rebound(path, placements[path].orEmpty(), removed, known))
                },
            )
        }
        val groups = componentGroups.mapNotNull { group ->
            group.components.filter { it !in removed }.takeIf { it.isNotEmpty() }?.let { group.copy(components = it) }
        }
        return copy(
            components = rebuilt,
            componentGroups = groups,
            functions = this.functions + functions,
            customActions = customActions + actions,
        )
    }

    /** A copy with different default prompt options. */
    fun withPromptOptions(options: PromptOptions): LibraryDefinition = copy(defaultPromptOptions = options)

    /** A copy with the app's [functions] and [customActions] instead of the current ones. */
    fun withCalls(
        functions: List<DefinedFunction> = this.functions,
        customActions: List<DefinedAction> = this.customActions,
    ): LibraryDefinition = copy(functions = functions, customActions = customActions)

    private fun copy(
        components: List<ComponentSchema> = this.components,
        componentGroups: List<ComponentGroup> = this.componentGroups,
        defaultPromptOptions: PromptOptions = this.defaultPromptOptions,
        functions: List<DefinedFunction> = this.functions,
        customActions: List<DefinedAction> = this.customActions,
    ) = LibraryDefinition(root, components, componentGroups, defaultPromptOptions, functions, customActions)

    /** The components of [candidates] that can join this library. */
    private fun addable(candidates: List<ComponentSchema>): List<ComponentSchema> {
        val names = HashSet(componentNames)
        names += functions.map { it.name }
        names += customActions.map { it.name }
        return candidates.filter {
            isCallableName(it.name) && !Builtins.isBuiltin(it.name) && !Builtins.isReservedCall(it.name) &&
                names.add(it.name)
        }
    }

    /** The built-in libraries. */
    companion object {
        /** The chat library (`openuiChatLibrary`): every response is one vertical `Card`. */
        val chat: LibraryDefinition by lazy {
            LibraryDefinition(
                root = ChatLibraryDefinition.root,
                components = ChatLibraryDefinition.schemas,
                componentGroups = ChatLibraryDefinition.componentGroups,
                defaultPromptOptions = openuiChatPromptOptions,
            )
        }

        /**
         * The standard library (`openuiLibrary`): `Stack` root with layouts, modals and cards, for
         * full-screen surfaces.
         */
        val standard: LibraryDefinition by lazy {
            LibraryDefinition(
                root = StandardLibraryDefinition.root,
                components = StandardLibraryDefinition.schemas,
                componentGroups = StandardLibraryDefinition.componentGroups,
                defaultPromptOptions = openuiPromptOptions,
            )
        }

        /** Whether programs can call a component by [name]: an upper case letter, then letters and digits. */
        internal fun isCallableName(name: String): Boolean =
            name.firstOrNull() in 'A'..'Z' && name.all { it in 'A'..'Z' || it in 'a'..'z' || it in '0'..'9' }

        internal fun buildJsonSchema(
            schemas: List<ComponentSchema>,
            functions: List<DefinedFunction> = emptyList(),
            actions: List<DefinedAction> = emptyList(),
        ): OpenUIValue {
            val byName = schemas.associateBy { it.name }
            // The JSON shapes of the actions slots refer to; a component of the same name wins.
            val actionShapes = linkedMapOf(
                PropType.ACTION_EXPRESSION to PropType.actionSchema,
                "OpenUrl" to PropType.openUrlSchema,
                "ToAssistant" to PropType.toAssistantSchema,
            )
            actions.forEach { actionShapes[it.name] = it.stepSchema }
            // $defs in depth-first, first-encounter order, as Zod's toJSONSchema emits them.
            val defs = OpenUIObject.Builder()
            val seen = HashSet<String>()
            fun visit(reference: PropType.Definition) {
                val name = when (reference) {
                    is PropType.Definition.Component -> reference.name
                    is PropType.Definition.Action -> reference.name
                }
                val component = byName[name]
                if (component != null) {
                    if (!seen.add(name)) return
                    defs[name] = component.jsonSchema
                    component.props.forEach { field -> field.type.definitionReferences.forEach(::visit) }
                } else if (reference is PropType.Definition.Action) {
                    val shape = actionShapes[name] ?: return
                    if (seen.add(name)) defs[name] = shape
                }
            }
            schemas.forEach { visit(PropType.Definition.Component(it.name)) }
            return OpenUIValue.Obj(
                OpenUIObject.build {
                    this["\$schema"] = OpenUIValue.Str("https://json-schema.org/draft/2020-12/schema")
                    this["type"] = OpenUIValue.Str("object")
                    this["properties"] = OpenUIValue.Obj(
                        OpenUIObject.build {
                            schemas.forEach { this[it.name] = obj("\$ref" to "#/\$defs/${it.name}") }
                        },
                    )
                    this["required"] = OpenUIValue.Arr(schemas.map { OpenUIValue.Str(it.name) })
                    this["additionalProperties"] = OpenUIValue.False
                    this["\$defs"] = OpenUIValue.Obj(defs.build())
                    if (functions.isNotEmpty()) {
                        this["functions"] = OpenUIValue.Obj(
                            OpenUIObject.build {
                                functions.forEach { function ->
                                    this[function.name] = OpenUIValue.Obj(
                                        OpenUIObject.build {
                                            this["description"] = OpenUIValue.Str(function.description)
                                            this["params"] = function.paramsSchema
                                            function.returns?.let { this["returns"] = it.jsonSchema }
                                        },
                                    )
                                }
                            },
                        )
                    }
                    if (actions.isNotEmpty()) {
                        this["actions"] = OpenUIValue.Obj(
                            OpenUIObject.build {
                                actions.forEach {
                                    this[it.name] = obj("description" to it.description, "params" to it.paramsSchema)
                                }
                            },
                        )
                    }
                },
            )
        }
    }
}

private fun extendFailure(message: String): Nothing = throw IllegalArgumentException("[extend] $message")

/** Fails unless programs can call a component named [name]: callable, and not taken by one of [callNames]. */
private fun checkCallable(name: String, callNames: List<String>) {
    if (!LibraryDefinition.isCallableName(name) || Builtins.isBuiltin(name) || Builtins.isReservedCall(name)) {
        extendFailure("Component \"$name\" can't be called in a program. Use a PascalCase name.")
    }
    if (name in callNames) extendFailure("Component \"$name\" has the name of a function or action.")
}

/** Whether [type] is a content slot: a union of [known] components, or an array of one. */
private fun isContentSlot(type: PropType, known: Set<String>): Boolean = when (val kind = type.kind) {
    is PropType.Kind.ArrayOf -> isContentSlot(kind.items, known)
    is PropType.Kind.Union -> kind.options.any { (it.kind as? PropType.Kind.Component)?.name in known }
    else -> false
}

/** The content slot each requested component joins: `"Card.children"` -> the components placed there. */
private fun placeInSlots(
    requests: Map<String, List<String>>,
    byName: Map<String, ComponentSchema>,
    known: Set<String>,
): Map<String, List<String>> {
    val slots = byName.values.flatMap { schema ->
        schema.props.filter { isContentSlot(it.type, known) }.map { "${schema.name}.${it.name}" }
    }
    val placements = LinkedHashMap<String, MutableList<String>>()
    for ((name, requested) in requests) {
        for (slot in requested) {
            val placed = placements.getOrPut(resolveSlot(slot, name, slots, byName)) { mutableListOf() }
            if (name !in placed) placed += name
        }
    }
    return placements
}

/** `"Card.children"` stays; `"Card"` is `"Card.children"` when that is Card's only content slot. */
private fun resolveSlot(slot: String, name: String, slots: List<String>, byName: Map<String, ComponentSchema>): String {
    if (slot in slots) return slot
    val parent = slot.substringBefore('.')
    val candidates = slots.filter { it.startsWith("$parent.") }
    if ('.' !in slot && candidates.size == 1) return candidates.single()
    val what = "Slot \"$slot\" for \"$name\""
    if (parent !in byName) extendFailure("$what: \"$parent\" is not in the library.")
    if (candidates.isEmpty()) {
        extendFailure(
            "$what: \"$parent\" has no content slot (a prop that is a union of components, or an optional or " +
                "array of one).",
        )
    }
    extendFailure("$what must be one content slot. Candidates: ${candidates.joinToString(", ")}.")
}

/**
 * The type of the prop at [path] with the [removed] components taken out of its unions and [added]
 * joining its content-slot union.
 */
private fun PropType.rebound(path: String, added: List<String>, removed: Set<String>, known: Set<String>): PropType {
    fun onlyType(name: String?): Nothing =
        extendFailure("Cannot remove \"$name\": it is the only type allowed in $path.")
    return when (val kind = kind) {
        is PropType.Kind.Component -> if (kind.name in removed) onlyType(kind.name) else this
        is PropType.Kind.Union -> {
            val names = kind.options.map { (it.kind as? PropType.Kind.Component)?.name }
            // An option goes when every component it refers to is removed, such as `array(A)` without A.
            val kept = kind.options
                .filter { option ->
                    val references = option.componentReferences
                    references.isEmpty() || references.any { it !in removed }
                }
                .map { it.rebound(path, emptyList(), removed, known) }
            val holdsComponents = names.any { it in known }
            val joining = if (holdsComponents) {
                added.filter { name -> kept.none { (it.kind as? PropType.Kind.Component)?.name == name } }
            } else {
                emptyList()
            }
            if (kept.isEmpty() && joining.isEmpty()) {
                onlyType(kind.options.firstNotNullOfOrNull { it.componentReferences.firstOrNull() })
            }
            if (kept == kind.options && joining.isEmpty()) {
                this
            } else {
                PropType.union(kept + joining.map(PropType::component))
            }
        }
        is PropType.Kind.ArrayOf -> PropType.array(kind.items.rebound(path, added, removed, known), kind.minItems)
        is PropType.Kind.RecordOf -> PropType.record(kind.value.rebound(path, emptyList(), removed, known))
        is PropType.Kind.ObjectOf -> PropType.objectOf(
            kind.fields.map { it.copy(type = it.type.rebound("$path.${it.name}", emptyList(), removed, known)) },
        )
        else -> this
    }
}
