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

package io.getstream.chat.android.ai.a2ui.generativeui

import io.getstream.chat.android.ai.a2ui.core.A2UIIdentifier
import io.getstream.chat.android.ai.a2ui.core.A2UIJson
import io.getstream.chat.android.ai.a2ui.core.A2UIProtocol
import io.getstream.chat.android.ai.a2ui.core.Catalog
import io.getstream.chat.android.ai.a2ui.core.ComponentDefinition
import io.getstream.chat.android.ai.compose.generativeui.AIUIComponent
import java.util.Locale

/**
 * The app's components, described once for every platform and the agent, as A2UI components: a
 * component's parameters are its properties, and what its events do joins its description, since
 * it reports them itself rather than through an `action` the agent writes.
 */
internal object PortableComponents {

    /** The A2UI type of a component, named as the basic catalog names its own: `place_card` is `PlaceCard`. */
    fun typeName(name: String): String = name.split('_', '-', ' ').filter { it.isNotEmpty() }.joinToString("") { part ->
        part.replaceFirstChar { it.titlecase(Locale.ROOT) }
    }

    /**
     * The catalog entry of [component], or `null` when its name or a parameter's isn't an A2UI
     * identifier, or a parameter is named `component`, the key that holds a component's type.
     */
    fun definition(component: AIUIComponent): ComponentDefinition? {
        val definition = component.definition
        val type = typeName(definition.name)
        if (!A2UIIdentifier.isValid(type) || type == A2UIProtocol.SURFACE_TYPE) return null
        val schema = definition.parametersSchema
        val properties = A2UIJson.map(schema["properties"]).orEmpty().mapNotNull { (key, value) ->
            A2UIJson.map(value)?.let { key to it }
        }.toMap()
        if (properties.keys.any { it == TYPE_KEY || !A2UIIdentifier.isValid(it) }) return null
        val notes = listOf(definition.description) + definition.events.map { it.description }
        return ComponentDefinition(
            name = type,
            description = notes.filter { it.isNotBlank() }.joinToString(" ") { sentence(it) },
            properties = properties,
            required = A2UIJson.list(schema["required"]).orEmpty().filterIsInstance<String>(),
        )
    }

    /**
     * The app's components the agent can place with [catalog], each with its A2UI definition. One
     * whose definition isn't valid, or whose type the catalog or an earlier component already has,
     * is left out.
     */
    fun placeable(components: List<AIUIComponent>, catalog: Catalog): List<Pair<AIUIComponent, ComponentDefinition>> {
        val taken = HashSet(catalog.components.keys)
        return components.mapNotNull { component ->
            val definition = definition(component) ?: return@mapNotNull null
            if (taken.add(definition.name)) component to definition else null
        }
    }

    private const val TYPE_KEY = "component"

    private fun sentence(text: String): String {
        val trimmed = text.trim()
        return if (trimmed.last() in ".!?") trimmed else "$trimmed."
    }
}
