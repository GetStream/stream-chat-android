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

package io.getstream.chat.android.ai.a2ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import io.getstream.chat.android.ai.a2ui.core.A2UIJson
import io.getstream.chat.android.ai.a2ui.core.Node
import io.getstream.chat.android.ai.a2ui.core.NodeContext
import io.getstream.chat.android.ai.a2ui.core.Surface
import io.getstream.chat.android.ai.a2ui.core.ValidationResult
import io.getstream.chat.android.ai.compose.util.internal.AIImageLoader

/**
 * One component of a surface as its renderer sees it: its properties, resolved against the
 * surface's data, its children, and what it does when the person uses it.
 *
 * Values are plain Kotlin values: maps, lists, strings, numbers ([Long] or [Double]), booleans
 * and `null`. A value the agent bound to the data model reads the data model now, and the surface
 * draws again when the data changes.
 *
 * Read it while composing, on the main thread.
 */
@Stable
public class A2UIComponentScope internal constructor(
    internal val context: NodeContext,
    // What the parent gives the component, such as its weight in a `Row`, with its entrance.
    internal val layout: Modifier,
    // The agent's accessibility label, for the node a person interacts with.
    internal val accessibility: Modifier,
    // The agent's accessibility description: what using the component does.
    internal val clickLabel: String?,
    internal val environment: SurfaceEnvironment,
) {
    /**
     * The modifier the parent gives this component, such as its weight in a `Row`, with the
     * component's accessibility label. Apply it to the outermost element.
     */
    public val modifier: Modifier = layout.then(accessibility)

    /** The component's id, unique in its surface. */
    public val id: String get() = context.component.id

    /** The component's type, such as `Text`. */
    public val type: String get() = context.component.type

    /**
     * Property [name], resolved, or `null` when it is missing or not there yet. A list or an object
     * the agent wrote resolves all the way down, so a binding or a call nested in it, such as an
     * option's label, reads as its value. A value read from the data model is data, as it is.
     */
    public fun value(name: String): Any? = resolve(raw(name))

    /** Property [name] as text; numbers show without a needless `.0`. */
    public fun string(name: String): String? = context.string(name)

    /** Property [name] as a number. */
    public fun number(name: String): Double? = context.double(name)

    /** Property [name] as a boolean; a check's result reads as whether it passed. */
    public fun boolean(name: String): Boolean? = context.bool(name)

    /** Property [name] as a list of strings. */
    public fun stringList(name: String): List<String>? = context.stringList(name)

    /**
     * Property [name] as the URL an image, video or audio clip loads from: the one the agent wrote
     * when it is a web link, or what the app's media URL resolver makes of it. `null` doesn't load.
     */
    internal fun mediaUrl(name: String): String? {
        val url = string(name)?.takeIf { it.isNotEmpty() } ?: return null
        val resolve = environment.mediaUrl ?: return url.takeIf(AIImageLoader::isLoadable)
        return resolve(url)
    }

    /** Property [name] as the agent wrote it, for a hint such as `variant`, which is never bound. */
    public fun hint(name: String): String? = context.literalString(name)

    /** Whether property [name] is bound to the data model, so [write] changes it. */
    public fun isBound(name: String): Boolean = context.boundPath(name) != null

    /**
     * Writes [value] to the data model where property [name] is bound, as an input does when the
     * person changes it. Does nothing when the property isn't bound.
     */
    public fun write(name: String, value: Any?) {
        context.write(name, A2UIJson.normalize(value))
    }

    /** The single child property [name] refers to, such as a `Card`'s `child`. */
    public fun child(name: String = "child"): A2UIChild? = context.child(name)?.let(::A2UIChild)

    /**
     * The children property [name] lists: the components it names, or its template repeated for
     * every item of the list it is bound to.
     */
    public fun children(name: String = "children"): List<A2UIChild> = context.children(name).map(::A2UIChild)

    /** The component with [id], placed as this one's child, such as a tab's content. */
    public fun childWithId(id: String, slot: String = id): A2UIChild? = context.childNode(id, slot)?.let(::A2UIChild)

    /** Draws [child] with the surface's catalog. */
    @Composable
    public fun Child(child: A2UIChild, modifier: Modifier = Modifier) {
        RenderNode(environment, child.node, modifier)
    }

    /** Whether the component does something when the person uses it. */
    public val hasAction: Boolean get() = context.component.properties.containsKey("action")

    /**
     * Does what the component does when the person uses it: sends its event to the agent, with
     * its context read now, or runs its function.
     */
    public fun performAction() {
        context.performAction()
    }

    /**
     * The messages of the component's checks that fail now and should block it, such as a
     * button's form being incomplete.
     */
    public val validationErrors: List<String>
        get() = context.failingChecks()
            .filter { it.severity == ValidationResult.SEVERITY_ERROR }
            .mapNotNull { it.message ?: it.code }

    /** [raw], a value nested in a property such as a tab's title, resolved in this component's scope. */
    internal fun resolve(raw: Any?): Any? = context.surface.evaluator.evaluateRaw(raw, context.node.context)

    /** The property [name] as the agent wrote it. */
    internal fun raw(name: String): Any? = context.component.properties[name]

    /** Whether none of the component's checks blocks it. */
    public val isValid: Boolean get() = context.failingChecks().none { it.severity == ValidationResult.SEVERITY_ERROR }
}

/**
 * What every component of one surface draws with. [revision] is the surface's revision it was made
 * for: a new one makes every component draw again. [mediaUrl] is the app's media URL resolver, or
 * `null` to load media only from web links.
 */
internal class SurfaceEnvironment(
    val surface: Surface,
    val catalog: A2UICatalog,
    val entrances: Boolean,
    val revision: Int,
    val mediaUrl: ((String) -> String?)? = null,
)

/** A child of a component, ready to draw with [A2UIComponentScope.Child]. */
@Stable
public class A2UIChild internal constructor(internal val node: Node) {
    /** A key unique among its siblings and stable across updates, for `key()`. */
    public val key: String get() = node.key

    /** The child component's id. */
    public val id: String get() = node.component.id

    /** The child's layout weight in a `Row` or `Column`, when it has one. */
    public val weight: Float? get() = node.component.weight?.toFloat()?.takeIf { it > 0f }
}
