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

package io.getstream.chat.android.ai.openui.generativeui

import io.getstream.chat.android.ai.compose.generativeui.AIUIComponent
import io.getstream.chat.android.ai.compose.generativeui.AIUIEngine
import io.getstream.chat.android.ai.compose.generativeui.AIUIFormat
import io.getstream.chat.android.ai.compose.generativeui.AIUIHost
import io.getstream.chat.android.ai.compose.generativeui.AIUIRenderer
import io.getstream.chat.android.ai.openui.lang.ToolProvider
import io.getstream.chat.android.ai.openui.library.ComponentSchema
import io.getstream.chat.android.ai.openui.library.LibraryDefinition
import io.getstream.chat.android.ai.openui.library.OpenUIPortableComponentException
import io.getstream.chat.android.ai.openui.library.PromptOptions
import io.getstream.chat.android.ai.openui.library.fromPortableDefinition
import io.getstream.chat.android.ai.openui.library.openuiChatPromptOptions
import io.getstream.chat.android.ai.openui.rendering.Library
import io.getstream.chat.android.ai.openui.rendering.openuiChatLibrary

/**
 * Shows the replies an agent writes in OpenUI Lang in the generative UI of
 * `stream-chat-android-ai-compose`, with the app's components placeable wherever OpenUI's own
 * content goes.
 *
 * ```
 * val ui = AIUIHost(renderers = listOf(OpenUIRenderer()), components = listOf(placeCard)) { action ->
 *     viewModelScope.launch { channel.sendMessage(Message(text = action.label, extraData = action.customData)) }
 * }
 * ```
 *
 * Each reply keeps its own runtime while the host is kept, so its forms and the state of its
 * `$variables` survive scrolling. What the person does that is for the agent, such as a follow-up
 * or a form's submit button, reaches the host as an `AIUIAction` named after OpenUI's action, with
 * the action's `context`, its other `params`, the `form` and the form's `values` in its payload.
 * Links open through the host, which decides the kinds of link that open.
 *
 * An app component joins the library under its OpenUI name, its name in `PascalCase` (`place_card`
 * is `PlaceCard`). One whose name the library already has, or that can't be called in a program,
 * is left out of both the instructions and the UI, so it never replaces one of OpenUI's own.
 *
 * @param library The library the agent writes with, the chat library by default.
 * @param promptOptions The examples and rules of the system prompt; pass the ones made for
 * [library] (`openuiPromptOptions` for `openuiLibrary`).
 * @param toolProvider Runs the tools of `Query()` and `Mutation()`. By default they are the host's
 * tools, run on the device.
 */
public class OpenUIRenderer(
    public val library: Library = openuiChatLibrary,
    public val promptOptions: PromptOptions = openuiChatPromptOptions,
    private val toolProvider: ToolProvider? = null,
) : AIUIRenderer {
    override val format: AIUIFormat = AIUIFormat.OpenUI

    /**
     * The fence the agent writes its program in (`openui-lang`), and the instructions its model
     * needs to write one with the library and [components].
     */
    override fun capabilities(components: List<AIUIComponent>): Map<String, Any?> {
        val schemas = placeable(components, library.definition).map { it.second }
        return mapOf(
            "fence" to Fence,
            "instructions" to library.definition.addingContent(schemas).systemPrompt(promptOptions),
        )
    }

    override fun makeEngine(host: AIUIHost): AIUIEngine =
        OpenUIEngine(library, host, toolProvider ?: HostToolProvider(host))

    internal companion object {
        /** The OpenUI schema of an app's component. */
        fun schema(component: AIUIComponent): ComponentSchema? = try {
            ComponentSchema.fromPortableDefinition(openUIValue(component.definition.toJson()))
        } catch (_: OpenUIPortableComponentException) {
            null
        }

        /**
         * The app's components the agent can place with [library], each with its OpenUI schema. One
         * whose definition can't be read, whose OpenUI name can't be called in a program, or whose
         * name the library or an earlier component already has, is left out.
         */
        fun placeable(
            components: List<AIUIComponent>,
            library: LibraryDefinition,
        ): List<Pair<AIUIComponent, ComponentSchema>> {
            val names = HashSet(library.componentNames)
            names += library.functions.map { it.name }
            names += library.customActions.map { it.name }
            return components.mapNotNull { component ->
                val schema = schema(component) ?: return@mapNotNull null
                val joins = library.addingContent(listOf(schema)).component(schema.name) === schema
                if (joins && names.add(schema.name)) component to schema else null
            }
        }
    }
}

// The code fence agents write OpenUI Lang in.
private const val Fence = "openui-lang"
