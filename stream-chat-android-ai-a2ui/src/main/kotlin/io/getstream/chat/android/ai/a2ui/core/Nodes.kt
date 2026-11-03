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

package io.getstream.chat.android.ai.a2ui.core

/**
 * A component placed in a surface's tree, with the data scope its relative paths read.
 *
 * @param component The component.
 * @param context Its data scope: the root, or the item of a list template it repeats for.
 * @param key Unique among its siblings and stable across updates.
 * @param depth How deep it is; the root is 0.
 * @param ancestors The id and scope of every node above it. A component can only appear inside
 * itself in another scope, such as a template for nested replies.
 */
internal data class Node(
    val component: Component,
    val context: DataContext,
    val key: String,
    val depth: Int,
    val ancestors: Set<Pair<String, String>> = emptySet(),
)

/** The root node of [this] surface, once its root has arrived. */
internal fun Surface.rootNode(): Node? = root?.let { Node(it, DataContext(dataModel), it.id, depth = 0) }

/** Reads a node's properties, resolved against its surface's data, and builds its children. */
internal class NodeContext(val surface: Surface, val node: Node) {

    val component: Component get() = node.component

    private val evaluator: Evaluator get() = surface.evaluator

    /** Property [name], resolved. */
    fun value(name: String): Any? = evaluator.evaluate(component.dynamic(name), node.context)

    fun string(name: String): String? = A2UIJson.string(value(name))

    fun double(name: String): Double? = A2UIJson.double(value(name))

    fun bool(name: String): Boolean? = value(name)?.let { value ->
        val result = A2UIJson.map(value)?.takeIf { it.containsKey("valid") }
        if (result != null) A2UIJson.bool(result["valid"]) else A2UIJson.bool(value)
    }

    fun stringList(name: String): List<String>? = A2UIJson.stringList(value(name))

    /** Property [name] as the agent wrote it, for hints such as `variant` that are never bound. */
    fun literalString(name: String): String? = component.properties[name] as? String

    /** The absolute path property [name] is bound to, for an input's two-way `value`. */
    fun boundPath(
        name: String,
    ): DataPath? = (component.dynamic(name) as? DynamicValue.Binding)?.let { node.context.resolve(it.path) }

    /**
     * Writes [value] to the path property [name] is bound to, as an input does when the person
     * changes it. Does nothing when the property isn't bound.
     */
    fun write(name: String, value: Any?) {
        val path = boundPath(name) ?: return
        try {
            surface.dataModel.set(path, value)
        } catch (error: DataModelException) {
            surface.report(RendererError(RendererError.DATA_ERROR, error.message.orEmpty(), surface.surfaceId))
        }
    }

    /** The single child property [name] refers to, such as a `Card`'s `child`. */
    fun child(
        name: String = "child",
    ): Node? = (component.properties[name] as? String)?.let { childNode(it, node.context, "${node.key}/$it") }

    /** The child with [id], in this node's scope. */
    fun childNode(id: String, slot: String = id): Node? = childNode(id, node.context, "${node.key}/$slot")

    /**
     * The children property [name] lists: the ids, or the template repeated for every item of
     * the list it is bound to, each in the scope of its item.
     */
    fun children(
        name: String = "children",
    ): List<Node> = when (val children = ChildList.of(component.properties[name])) {
        is ChildList.Ids -> children.ids.mapIndexedNotNull { index, id ->
            childNode(id, node.context, "${node.key}/$index:$id")
        }
        is ChildList.Template -> {
            val items = A2UIJson.list(node.context.value(children.path)).orEmpty()
            items.indices.mapNotNull { index ->
                val itemContext = node.context.item(children.path, index)
                childNode(children.componentId, itemContext, "${node.key}/${itemContext.scope}")
            }
        }
        ChildList.None -> emptyList()
    }

    /** What the component does when the person uses it, if anything. */
    val action: ActionDefinition? get() = ActionDefinition.of(component.properties["action"])

    /** Runs the component's action, if it has one. */
    fun performAction() {
        surface.performRaw(component.properties["action"], component.id, node.context)
    }

    /** The component's checks that fail now. */
    fun failingChecks(): List<ValidationResult> = evaluator.failingChecks(component.checks, node.context)

    /** The component's accessibility attributes, resolved. */
    fun accessibility(): Accessibility? {
        val attributes = component.accessibility ?: return null
        return Accessibility(
            label = A2UIJson.string(evaluator.evaluate(DynamicValue.of(attributes["label"]), node.context)),
            description = A2UIJson.string(evaluator.evaluate(DynamicValue.of(attributes["description"]), node.context)),
            live = attributes["live"] as? String ?: "off",
            hidden = A2UIJson.bool(evaluator.evaluate(DynamicValue.of(attributes["hidden"]), node.context)) ?: false,
        )
    }

    private fun childNode(id: String, context: DataContext, key: String): Node? {
        if (node.depth >= A2UIProtocol.MAX_NESTING_DEPTH) {
            surface.report(
                RendererError(
                    RendererError.RECURSION_LIMIT_EXCEEDED,
                    "The component tree nests deeper than ${A2UIProtocol.MAX_NESTING_DEPTH} levels " +
                        "at '${node.component.id}'.",
                    surface.surfaceId,
                ),
            )
            return null
        }
        val component = surface.component(id) ?: return null
        val ancestors = node.ancestors + (node.component.id to node.context.scope.toString())
        // A component inside itself with the same data would repeat forever.
        if ((id to context.scope.toString()) in ancestors) return null
        return Node(component, context, key, node.depth + 1, ancestors)
    }
}

/** A component's accessibility attributes, resolved. */
internal data class Accessibility(val label: String?, val description: String?, val live: String, val hidden: Boolean)

/**
 * The A2UI messages of a reply as the agent writes them: JSON objects one after another, on
 * lines of their own, pretty-printed or in an array. Each complete one counts, in order; the one
 * still being written doesn't, so a UI shows as its messages arrive.
 */
internal object A2UIStream {
    /** The complete top-level JSON objects in [text], as written. */
    fun messages(text: String): List<String> {
        val messages = mutableListOf<String>()
        var depth = 0
        var start = 0
        var inString = false
        var escaped = false
        for (index in text.indices) {
            val char = text[index]
            if (inString) {
                when {
                    escaped -> escaped = false
                    char == '\\' -> escaped = true
                    char == '"' -> inString = false
                }
                continue
            }
            when {
                char == '"' && depth > 0 -> inString = true
                char == '{' -> {
                    if (depth == 0) start = index
                    depth++
                }
                char == '}' && depth > 0 -> {
                    depth--
                    if (depth == 0) messages.add(text.substring(start, index + 1))
                }
            }
        }
        return messages
    }
}
