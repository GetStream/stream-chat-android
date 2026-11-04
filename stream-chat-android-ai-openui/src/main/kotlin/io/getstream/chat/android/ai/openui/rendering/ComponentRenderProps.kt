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
import androidx.compose.runtime.Stable
import io.getstream.chat.android.ai.openui.lang.ElementNode
import io.getstream.chat.android.ai.openui.lang.JS
import io.getstream.chat.android.ai.openui.lang.OpenUIObject
import io.getstream.chat.android.ai.openui.lang.OpenUIRuntime
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.StateField
import io.getstream.chat.android.ai.openui.lang.elementValue
import io.getstream.chat.android.ai.openui.lang.objectValue
import io.getstream.chat.android.ai.openui.lang.stringValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Read access to a component's evaluated props with lenient, JavaScript-like conversions. */
@Stable
public class ComponentProps internal constructor(internal val values: OpenUIObject) {

    /** The raw value of prop [key]. */
    internal operator fun get(key: String): OpenUIValue? = values[key]

    /** The value if it is a string. */
    public fun string(key: String): String? = values[key].stringValue

    /** The value as display text (`String(value)`), or `null` when null or absent. */
    public fun text(key: String): String? = values[key].takeUnless(JS::isNullish)?.let(JS::toString)

    /** The value as a number (numbers and numeric strings). */
    public fun number(key: String): Double? = when (val value = values[key]) {
        is OpenUIValue.Num -> value.value
        is OpenUIValue.Str -> JS.stringToNumber(value.value).takeUnless { it.isNaN() }
        else -> null
    }

    /** The value as a boolean (booleans and `"true"`/`"false"` strings). */
    public fun bool(key: String): Boolean? = when (val value = values[key]) {
        is OpenUIValue.Bool -> value.value
        OpenUIValue.Str("true") -> true
        OpenUIValue.Str("false") -> false
        else -> null
    }

    /** The value as an array: arrays as they are, `null`/absent as empty, anything else wrapped. */
    internal fun array(key: String): List<OpenUIValue> = when (val value = values[key]) {
        is OpenUIValue.Arr -> value.items
        null, OpenUIValue.Null -> emptyList()
        else -> listOf(value)
    }

    /** Elements in an array prop (non-elements are skipped). */
    internal fun elements(key: String): List<ElementNode> = array(key).mapNotNull { it.elementValue }

    /** The value if it is an element. */
    internal fun element(key: String): ElementNode? = values[key].elementValue

    /** The value if it is an object. */
    internal fun obj(key: String): OpenUIObject? = values[key].objectValue

    /** Strings (non-strings converted with `String(value)`). */
    public fun strings(key: String): List<String> = array(key).map { JS.toStringOrEmpty(it) }

    /** Numbers (non-numbers coerced; invalid values become `NaN`, like `Number()`). */
    public fun numbers(key: String): List<Double> = array(key).map(JS::toNumber)
}

/**
 * What a component's renderer gets (port of `ComponentRenderProps`, with the context hooks
 * component renderers use): its evaluated [props], whether the response is still streaming, and
 * ways to render its children and the library's own view of it.
 */
@Stable
public class ComponentRenderProps internal constructor(
    /** The element being rendered. */
    internal val element: ElementNode,
    /** The runtime that owns the response's state. */
    internal val runtime: OpenUIRuntime,
    /** The library the element is rendered with. */
    internal val library: Library,
    /** Whether the response is still streaming, so inputs stay disabled until it ends (`useIsStreaming`). */
    public val isStreaming: Boolean,
    private val scope: CoroutineScope,
    /** The library's own view of the component, when an app replaced it. */
    private val defaultRenderer: ComponentRenderer? = null,
) {
    /** The element's evaluated props. */
    public val props: ComponentProps = ComponentProps(element.props)

    /** The component name, e.g. `Button`. */
    internal val typeName: String get() = element.typeName

    /** The statement that declared the element, if any; `null` for inline components. */
    internal val statementId: String? get() = element.statementId

    /**
     * Renders a value: an element, an array of values (as siblings) or a primitive (as text), port
     * of `renderNode`.
     */
    @Composable
    internal fun RenderNode(value: OpenUIValue?) {
        OpenUINode(value)
    }

    /**
     * Renders the prop named [key] that holds components, such as a card's `children`.
     *
     * @param key The prop's name.
     */
    @Composable
    public fun RenderProp(key: String) {
        OpenUINode(element.props[key])
    }

    /**
     * Renders the library's own view of the component, to wrap or restyle in a view that replaces
     * it with [Library.replacingView]. It keeps the component's validation and actions. It renders
     * nothing for a component that was not replaced.
     */
    @Composable
    public fun DefaultView() {
        defaultRenderer?.invoke(this)
    }

    /** Renders one element with its library renderer. */
    @Composable
    internal fun RenderElement(element: ElementNode) {
        OpenUIElement(element)
    }

    /**
     * A field bound to a `$variable` (when [binding] is one) or stored as the enclosing form's
     * state (port of `useStateField`). Reading it subscribes the caller to state changes.
     */
    @Composable
    internal fun stateField(name: String, binding: OpenUIValue?, componentType: String? = null): StateField {
        LocalOpenUIStoreSnapshot.current // Recompose when the store changes.
        return runtime.stateField(name, binding, LocalOpenUIFormName.current, componentType)
    }

    /**
     * Runs [action] (a step, `Action([...])` or a legacy action object) with the state of the form
     * [formName]; without one, continues the conversation with [label] (port of `useTriggerAction`).
     */
    internal fun triggerAction(label: String, formName: String? = null, action: OpenUIValue? = null) {
        scope.launch { runtime.triggerAction(label, formName, action) }
    }

    /** A copy of the context for another element (used by parents to render data-only children). */
    internal fun forElement(element: ElementNode): ComponentRenderProps =
        ComponentRenderProps(element, runtime, library, isStreaming, scope)

    /** A copy of the context whose [DefaultView] draws [renderer]. */
    internal fun withDefaultView(renderer: ComponentRenderer): ComponentRenderProps =
        ComponentRenderProps(element, runtime, library, isStreaming, scope, renderer)

    /** Helpers for parents that render their children. */
    internal companion object {
        /** A copy of [element] with some props overridden (used by parents to style children). */
        fun overriding(element: ElementNode, overrides: Map<String, OpenUIValue>): ElementNode =
            element.copy(props = element.props + overrides)
    }
}
