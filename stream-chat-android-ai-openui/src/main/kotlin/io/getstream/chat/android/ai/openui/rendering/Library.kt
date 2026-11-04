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

package io.getstream.chat.android.ai.openui.rendering

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import io.getstream.chat.android.ai.openui.components.BuiltInRenderers
import io.getstream.chat.android.ai.openui.lang.LibrarySchema
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.library.ComponentGroup
import io.getstream.chat.android.ai.openui.library.ComponentSchema
import io.getstream.chat.android.ai.openui.library.DefinedAction
import io.getstream.chat.android.ai.openui.library.DefinedFunction
import io.getstream.chat.android.ai.openui.library.LibraryDefinition
import io.getstream.chat.android.ai.openui.library.PromptOptions
import io.getstream.chat.android.ai.openui.library.PromptSpec
import io.getstream.chat.android.ai.openui.library.PropField

/** Renders a component from its evaluated props (port of `ComponentRenderer`). */
public typealias ComponentRenderer = @Composable (ComponentRenderProps) -> Unit

/**
 * Defines a component: its name, its positional [props], the [description] the prompt shows and
 * the renderer that draws it (port of `defineComponent`).
 *
 * ```kotlin
 * val placeCard = defineComponent(
 *     name = "PlaceCard",
 *     props = listOf(PropField.required("name", PropType.string), PropField.optional("rating", PropType.number)),
 *     description = "A place with its rating",
 * ) { props -> PlaceCard(props.props.string("name").orEmpty(), props.props.number("rating")) }
 * ```
 *
 * @param component Draws the component; `null` for data-only components their parent reads
 * (`SelectItem`, `Series`, ...).
 */
internal fun defineComponent(
    name: String,
    props: List<PropField>,
    description: String,
    component: ComponentRenderer?,
): DefinedComponent = DefinedComponent(ComponentSchema(name, description, props), component)

/** A component of a library (port of `DefinedComponent`); create one with [defineComponent]. */
@Immutable
internal class DefinedComponent internal constructor(
    /** The model-facing contract: name, props and description. */
    internal val schema: ComponentSchema,
    /** Draws the component; `null` for data-only components their parent reads. */
    val component: ComponentRenderer?,
) : ComponentAddition {
    /** The name programs call it by. */
    val name: String get() = schema.name

    /** Its positional props. */
    val props: List<PropField> get() = schema.props

    /** What it is, for the prompt. */
    val description: String get() = schema.description
}

/**
 * A component [LibraryExtension.add] adds: a [DefinedComponent] that is only listed, or a
 * [SlottedComponent] that is also placed in content slots.
 */
internal sealed interface ComponentAddition

/**
 * A component added to a library and placed in [slots] (the `{ component, slots }` form of
 * `LibraryExtension.components.add`). A slot is `"Parent.prop"`, or `"Parent"` when that component
 * has one content slot: a prop that takes a union of components, or an array of one.
 */
internal class SlottedComponent(
    /** The component added. */
    val component: DefinedComponent,
    /** The content slots it joins, such as `"Card.children"` or `"Card"`. */
    val slots: List<String>,
) : ComponentAddition

/**
 * What [Library.extend] changes (port of `LibraryExtension`).
 *
 * @param add New components. A [SlottedComponent] also joins the content slots it names.
 * @param override Replacements, schema and renderer, by name; every parent points at the new one.
 * @param remove Names to drop from the library, from every parent union and from the groups.
 * @param functions Functions that join the library and are listed in the prompt.
 * @param actions Action steps that join the library and are listed in the prompt.
 */
internal class LibraryExtension(
    val add: List<ComponentAddition> = emptyList(),
    val override: List<DefinedComponent> = emptyList(),
    val remove: List<String> = emptyList(),
    val functions: List<DefinedFunction> = emptyList(),
    val actions: List<DefinedAction> = emptyList(),
)

/**
 * Creates a library: the components the model may use, how they are grouped in the prompt, the
 * root component, and the library's own functions and actions (port of `createLibrary`).
 *
 * @throws IllegalArgumentException When [root] is not one of [components].
 */
internal fun createLibrary(
    components: List<DefinedComponent>,
    root: String? = null,
    componentGroups: List<ComponentGroup> = emptyList(),
    functions: List<DefinedFunction> = emptyList(),
    actions: List<DefinedAction> = emptyList(),
): Library = Library(
    LibraryDefinition(
        root = root,
        components = components.map { it.schema },
        componentGroups = componentGroups,
        functions = functions,
        customActions = actions,
    ),
    components.associate { it.name to it.component },
)

/**
 * The chat library (port of `openuiChatLibrary`): every response is a single vertical `Card`. Use
 * it for assistant messages in a conversation.
 */
public val openuiChatLibrary: Library by lazy {
    Library(LibraryDefinition.chat, BuiltInRenderers.forLibrary(BuiltInRenderers.Flavor.Chat))
}

/**
 * The standard library (port of `openuiLibrary`): a `Stack` root with layouts, modals and cards,
 * for full-screen surfaces such as dashboards.
 */
public val openuiLibrary: Library by lazy {
    Library(LibraryDefinition.standard, BuiltInRenderers.forLibrary(BuiltInRenderers.Flavor.Standard))
}

/**
 * A component library: the components the model may use, how to render them, and what the system
 * prompt says about them (port of `Library`). Start from [openuiChatLibrary] or [openuiLibrary],
 * and restyle or drop components with [replacingView] and [removing].
 */
@Immutable
public class Library internal constructor(
    /** The model-facing half: schemas, groups, calls. */
    internal val definition: LibraryDefinition,
    renderers: Map<String, ComponentRenderer?>,
) {
    private val renderers: Map<String, ComponentRenderer?> = renderers.toMap()

    /** The root component name. */
    internal val root: String? get() = definition.root

    /** The components by name, in library order. */
    internal val components: Map<String, DefinedComponent> by lazy {
        definition.components.associate { it.name to DefinedComponent(it, renderers[it.name]) }
    }

    /** How the prompt groups the components. */
    internal val componentGroups: List<ComponentGroup> get() = definition.componentGroups

    /** The library's functions by name, called as `@Name(...)` in programs. */
    internal val functions: Map<String, DefinedFunction> get() = definition.functions.associateBy { it.name }

    /** The library's action steps by name, used as `@Name(...)` in action props. */
    internal val actions: Map<String, DefinedAction> get() = definition.customActions.associateBy { it.name }

    /**
     * The prompt options made for this library: the examples and rules of [openuiChatLibrary] or
     * [openuiLibrary], none for a library made with [createLibrary].
     */
    internal val defaultPromptOptions: PromptOptions get() = definition.defaultPromptOptions

    /**
     * The system prompt for this library, with [options] (examples, rules, tools, modes), the
     * library's own [defaultPromptOptions] by default.
     */
    internal fun prompt(options: PromptOptions = defaultPromptOptions): String = definition.systemPrompt(options)

    /**
     * What [io.getstream.chat.android.ai.openui.library.generatePrompt] needs for this library, with
     * its [defaultPromptOptions].
     */
    internal fun toSpec(): PromptSpec = definition.promptSpec(defaultPromptOptions)

    /** The library JSON Schema, which [io.getstream.chat.android.ai.openui.lang.createParser] takes. */
    internal fun toJSONSchema(): OpenUIValue = definition.jsonSchema

    /**
     * A library derived from this one with [extension] applied; this one stays as it is.
     *
     * @throws IllegalArgumentException When the extension adds a component the library already
     * has, overrides or removes one it doesn't, removes the root or a component that is the only
     * type a slot takes, or names a slot the library doesn't have.
     */
    internal fun extend(extension: LibraryExtension): Library {
        val added = extension.add.map { addition ->
            when (addition) {
                is DefinedComponent -> addition to null
                is SlottedComponent -> addition.component to addition.slots
            }
        }
        val next = definition.extended(
            add = added.map { (component, slots) -> component.schema to slots },
            override = extension.override.map { it.schema },
            remove = extension.remove,
            functions = extension.functions,
            actions = extension.actions,
        )
        val joined = (added.map { it.first } + extension.override).associate { it.name to it.component }
        val kept = renderers + joined
        return Library(next, next.componentNames.associateWith { kept[it] })
    }

    /** The names of the components, in library order. */
    public val componentNames: List<String> get() = definition.componentNames

    /**
     * A library in which [view] draws the component named [name], which keeps its props, such as
     * to restyle the chat library's buttons. [view] gets the component's props, and the library's
     * own view of it, [ComponentRenderProps.DefaultView], to wrap or restyle. It is the same library
     * when it has no such component, or when the component is data its parent reads.
     *
     * ```kotlin
     * val library = openuiChatLibrary.replacingView("Button") { props ->
     *     Box(Modifier.border(1.dp, Color.Red)) { props.DefaultView() }
     * }
     * OpenUIRenderer(library = library)
     * ```
     *
     * @param name The component's name, such as `"Button"`.
     * @param view Draws the component.
     */
    public fun replacingView(name: String, view: ComponentRenderer): Library {
        val original = renderers[name] ?: return this
        return replacingRenderer(name) { props -> view(props.withDefaultView(original)) }
    }

    /**
     * A library without the components named [names]. They are also removed from every slot that
     * takes several types, and groups left empty are dropped. The root component, and a component
     * that is the only type a remaining slot takes, stay.
     *
     * @param names The names of the components to remove.
     */
    public fun removing(names: Set<String>): Library = joining(definition.removing(names), emptyList())

    /** What the parser and runtime need. */
    internal val schema: LibrarySchema get() = definition.schema

    /** The component [name], or `null` when the library has none. */
    internal fun component(name: String): DefinedComponent? = components[name]

    /** The renderer of component [name], or `null` for data-only and unknown components. */
    internal fun renderer(name: String): ComponentRenderer? = renderers[name]

    /** The system prompt with [options]; the library's own default prompt options when `null`. */
    internal fun systemPrompt(options: PromptOptions? = null): String = definition.systemPrompt(options)

    /** A library where [name] renders with [renderer] (the schema and prompt stay the same). */
    internal fun replacingRenderer(name: String, renderer: ComponentRenderer): Library =
        Library(definition, renderers + (name to renderer))

    /**
     * A library with [components] added. A component is left out when the library already has one
     * of that name, or its name can't be called in a program (see [LibraryDefinition.adding]).
     */
    internal fun adding(
        components: List<DefinedComponent>,
        groups: List<ComponentGroup> = emptyList(),
    ): Library = joining(definition.adding(components.map { it.schema }, groups), components)

    /**
     * A library with [components] added and placeable wherever the library's own content goes, such
     * as the children of a chat `Card`, listed in the prompt under [group] and [notes]. A component
     * is left out when the library already has one of that name, or its name can't be called in a
     * program.
     */
    internal fun addingContent(
        components: List<DefinedComponent>,
        group: String = "App components",
        notes: List<String> = emptyList(),
    ): Library = joining(definition.addingContent(components.map { it.schema }, group, notes), components)

    // The library of [next], with the renderers of the components it keeps and of the ones of
    // [added] that joined it.
    private fun joining(next: LibraryDefinition, added: List<DefinedComponent>): Library {
        val joined = added.filter { definition.component(it.name) == null }.associate { it.name to it.component }
        val kept = renderers + joined
        return Library(next, next.componentNames.associateWith { kept[it] })
    }
}
