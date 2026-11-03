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

package io.getstream.chat.android.ai.compose.generativeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.UriHandler

/**
 * A view of the app's that the agent can place in a reply's generated UI: its description, and
 * how it looks for the arguments the agent wrote.
 *
 * Every format places it by name, so one component works whichever format the agent writes. Its
 * arguments are the JSON the agent wrote, as plain Kotlin values: read what you need and show
 * nothing while they don't fit, such as while they are still streaming in. A component can load
 * data of its own, such as the person's calendar, which then never reaches the agent.
 *
 * ```
 * val placeCard = AIUIComponent(
 *     AIUIComponentDefinition(
 *         name = "place_card",
 *         description = "A photo and a short summary of a place.",
 *         parameters = listOf(AIUIParameter.required("title", AIUIParameterType.String)),
 *         events = listOf(AIUIComponentDefinition.Event("ask_about", "The person asks about the place.")),
 *     ),
 * ) { arguments, context ->
 *     val title = arguments["title"] as? String ?: return@AIUIComponent
 *     PlaceCard(title = title, onClick = { context.emit("ask_about", label = "Tell me more about $title") })
 * }
 * ```
 */
@Stable
public interface AIUIComponent {
    /** Its description, the same on every platform and for the agent. */
    public val definition: AIUIComponentDefinition

    /** The name the agent places it by. */
    public val name: String get() = definition.name

    /**
     * How it looks for the arguments the agent wrote. It tells the agent what the person does in
     * it through [context]. Renderers bring it in as it shows, with [AIUIEntrance].
     *
     * @param arguments The JSON the agent wrote for its parameters, as plain Kotlin values.
     * @param context Which reply it is in, and how it tells the agent what the person did.
     */
    @Composable
    public fun Content(arguments: Map<String, Any?>, context: AIUIComponentContext)
}

/**
 * An [AIUIComponent] that shows [content].
 *
 * @param definition Its description, the same on every platform and for the agent.
 * @param content How it looks for the arguments the agent wrote.
 */
public fun AIUIComponent(
    definition: AIUIComponentDefinition,
    content: @Composable (arguments: Map<String, Any?>, context: AIUIComponentContext) -> Unit,
): AIUIComponent = object : AIUIComponent {
    override val definition: AIUIComponentDefinition = definition

    @Composable
    override fun Content(arguments: Map<String, Any?>, context: AIUIComponentContext) {
        content(arguments, context)
    }
}

/**
 * What a component gets besides its arguments: which reply it is in, and how it tells the agent
 * what the person did.
 *
 * @param messageId The reply the component is in.
 * @param isStreaming Whether the agent is still writing the reply's UI, so the arguments may still
 * change.
 * @param host The conversation's host, for running the app's tools and keeping state.
 * @param uriHandler Opens the links [openUrl] lets through, such as the composition's
 * `LocalUriHandler`.
 * @param emit Sends an event to the agent the way the renderer's format does.
 */
@Stable
public class AIUIComponentContext(
    public val messageId: String,
    public val isStreaming: Boolean,
    public val host: AIUIHost,
    private val uriHandler: UriHandler,
    private val emit: (event: String, label: String, payload: Map<String, Any?>) -> Unit,
) {
    /**
     * Tells the agent about one of the component's events, as the person's next message.
     *
     * @param event The event's name in the component's definition.
     * @param label What the person did, in their words, such as "Tell me more about St. Naum".
     * @param payload What the event carries, as plain Kotlin values.
     */
    public fun emit(event: String, label: String, payload: Map<String, Any?> = emptyMap()) {
        emit.invoke(event, label, payload)
    }

    /**
     * Opens a link, when the host lets links of its kind open (see [AIUIHost.canOpen]).
     *
     * @return Whether the link was opened.
     */
    public fun openUrl(url: String): Boolean = host.open(url, uriHandler)
}
