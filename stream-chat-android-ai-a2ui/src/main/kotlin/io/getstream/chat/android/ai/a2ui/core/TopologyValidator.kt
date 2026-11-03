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
 * Checks the shape of a surface's whole tree: a root, no references to components that don't
 * exist, no cycles, nothing unreachable from the root, and no deeper nesting than the protocol
 * allows.
 *
 * A2UI arrives while the agent writes it, so a tree is only complete when its messages are: the
 * renderer checks it then, never while it streams.
 *
 * @param allowOrphans Whether a component the root doesn't reach is fine.
 * @param allowDangling Whether a reference to a component that doesn't exist is fine.
 * @param allowMissingRoot Whether a surface without a root is fine.
 */
internal class TopologyValidator(
    private val allowOrphans: Boolean = false,
    private val allowDangling: Boolean = false,
    private val allowMissingRoot: Boolean = false,
) {

    /** The problems of [surface]'s tree. */
    fun errors(surface: Surface): List<RendererError> {
        val errors = mutableListOf<RendererError>()
        fun add(code: String, message: String) {
            errors += RendererError(code, message, surface.surfaceId)
        }
        val references = surface.components.values.associate { component ->
            component.id to surface.definition(component)?.childReferences(component).orEmpty()
        }
        for ((id, children) in references) {
            for ((pointer, childId) in children) {
                if (childId == id) {
                    add(
                        RendererError.RECURSION_LIMIT_EXCEEDED,
                        "Self-reference detected: component '$id' refers to itself in field '${field(pointer)}'.",
                    )
                } else if (surface.component(childId) == null && !allowDangling) {
                    add(
                        RendererError.INTEGRITY_ERROR,
                        "Dangling reference: component '$id' references non-existent component '$childId' " +
                            "in field '${field(pointer)}'.",
                    )
                }
            }
        }
        val root = surface.root
        if (root == null) {
            if (!allowMissingRoot) {
                add(
                    RendererError.VALIDATION_FAILED,
                    "Missing root component: surface '${surface.surfaceId}' has no component with id 'root'.",
                )
            }
            return errors
        }
        cycle(root.id, references)?.let { path ->
            add(RendererError.RECURSION_LIMIT_EXCEEDED, "Circular reference detected: ${path.joinToString(" -> ")}.")
            return errors
        }
        if (depth(root.id, references) > A2UIProtocol.MAX_NESTING_DEPTH) {
            add(
                RendererError.RECURSION_LIMIT_EXCEEDED,
                "Global recursion limit exceeded: the component tree nests deeper than " +
                    "${A2UIProtocol.MAX_NESTING_DEPTH} levels.",
            )
        }
        if (!allowOrphans) {
            val reachable = reachable(root.id, references)
            surface.components.keys.filter { it !in reachable }.forEach { id ->
                add(RendererError.INTEGRITY_ERROR, "Component '$id' is not reachable from 'root'.")
            }
        }
        return errors
    }

    // A cycle through the components [start] reaches, as the ids along it, or `null`.
    private fun cycle(start: String, references: Map<String, List<Pair<String, String>>>): List<String>? {
        val done = HashSet<String>()
        val path = mutableListOf<String>()
        fun visit(id: String): List<String>? {
            if (id in done) return null
            val index = path.indexOf(id)
            if (index >= 0) return path.subList(index, path.size) + id
            path.add(id)
            for ((_, child) in references[id].orEmpty()) {
                if (child != id) visit(child)?.let { return it }
            }
            path.removeAt(path.lastIndex)
            done.add(id)
            return null
        }
        return visit(start)
    }

    // How deep the tree under [id] goes; the tree has no cycle when this is asked.
    private fun depth(
        id: String,
        references: Map<String, List<Pair<String, String>>>,
        memo: HashMap<String, Int> = HashMap(),
    ): Int =
        memo[id] ?: run {
            val children = references[id].orEmpty().map { it.second }.filter { it != id && references.containsKey(it) }
            (1 + (children.maxOfOrNull { depth(it, references, memo) } ?: 0)).also { memo[id] = it }
        }

    private fun reachable(root: String, references: Map<String, List<Pair<String, String>>>): Set<String> {
        val reached = LinkedHashSet<String>()
        val pending = ArrayDeque(listOf(root))
        while (pending.isNotEmpty()) {
            val id = pending.removeFirst()
            if (reached.add(id)) references[id].orEmpty().forEach { pending.add(it.second) }
        }
        return reached
    }

    // A pointer such as `/slots/1` as the field it names, such as `slots[1]`.
    private fun field(pointer: String): String = pointer.removePrefix("/").split('/').fold("") { field, segment ->
        when {
            segment.all(Char::isDigit) && field.isNotEmpty() -> "$field[$segment]"
            field.isEmpty() -> segment
            else -> "$field.$segment"
        }
    }
}
