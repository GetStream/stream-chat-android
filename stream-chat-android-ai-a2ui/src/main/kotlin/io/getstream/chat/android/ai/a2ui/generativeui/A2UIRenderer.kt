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

import io.getstream.chat.android.ai.a2ui.compose.A2UICatalog
import io.getstream.chat.android.ai.a2ui.core.A2UIProtocol
import io.getstream.chat.android.ai.a2ui.core.BasicCatalog
import io.getstream.chat.android.ai.a2ui.core.Catalog
import io.getstream.chat.android.ai.compose.generativeui.AIUIComponent
import io.getstream.chat.android.ai.compose.generativeui.AIUIEngine
import io.getstream.chat.android.ai.compose.generativeui.AIUIFormat
import io.getstream.chat.android.ai.compose.generativeui.AIUIHost
import io.getstream.chat.android.ai.compose.generativeui.AIUIRenderer
import io.getstream.chat.android.ai.compose.parts.AIClientTool

/**
 * Shows the replies an agent writes in [A2UI](https://a2ui.org) 1.0 in the generative UI of
 * `stream-chat-android-ai-compose`, with the basic catalog and the app's components beside the
 * catalog's own.
 *
 * ```
 * val ui = AIUIHost(renderers = listOf(A2UIRenderer()), components = listOf(placeCard)) { action ->
 *     viewModelScope.launch { channel.sendMessage(Message(text = action.label, extraData = action.customData)) }
 * }
 * ```
 *
 * A reply's document holds its A2UI messages: as the agent wrote them, one JSON object after
 * another, or as JSON. Each reply has surfaces of its own, kept while the host is, so what the
 * person enters in them survives scrolling. A button's event reaches the host as an `AIUIAction`
 * named after the event, labelled with the button's text, and with its `surfaceId`,
 * `sourceComponentId`, resolved `context` and the event's `userMessage` in its payload, with the
 * surface's `dataModel` too when the agent created it with `sendDataModel`. The label is never the
 * agent's words, which could say something the person didn't choose. Links open through the host,
 * which decides the kinds of link that open.
 *
 * An app component joins the catalog under its A2UI name, its name in `PascalCase` (`place_card` is
 * `PlaceCard`), with its parameters as its properties. One whose name the basic catalog already
 * has, or that isn't a valid A2UI name, is left out, so it never replaces one of the catalog's own.
 * The catalog with the app's components goes by [catalogId], and the basic catalog's id shows them
 * as well.
 *
 * @param catalog How each component of the basic catalog looks; the app's components look as they
 * draw themselves.
 * @param catalogId The id the agent names the catalog with the app's components by.
 * @param instructions What the agent's model is told before A2UI's own instructions, such as which
 * answers to write as UI and when to use the app's components.
 */
public class A2UIRenderer(
    public val catalog: A2UICatalog = A2UICatalog.Basic,
    public val catalogId: String = DEFAULT_CATALOG_ID,
    private val instructions: String? = null,
) : AIUIRenderer {
    override val format: AIUIFormat = AIUIFormat.A2UI

    /** The info string of the fenced block an agent writes a reply's A2UI in: `ui:a2ui@1.0`. */
    public val fence: String get() = "ui:a2ui@$VERSION"

    /** Reads documents of A2UI 1.0, under any of the names agents give it, or of no version. */
    override fun supports(version: String?): Boolean = version == null || version in SUPPORTED_VERSIONS

    /**
     * The fence the agent writes its UI in (`fence`), the id of the catalog with the app's
     * [components] (`catalog_id`), and the instructions its model needs to write UI with that
     * catalog and call the app's [tools] (`instructions`): the renderer's own `instructions` first,
     * then how a reply carries A2UI, the protocol, the catalog's guidelines, the tools by name and
     * the catalog itself, in Markdown.
     */
    override fun capabilities(components: List<AIUIComponent>, tools: List<AIClientTool>): Map<String, Any?> = mapOf(
        "fence" to fence,
        "catalog_id" to catalogId,
        "instructions" to A2UIInstructions(
            catalog = catalogs(components).first(),
            fence = fence,
            preamble = instructions,
            tools = tools.map { it.name },
        ).text,
    )

    /** The capabilities with the app's [components] and none of its tools. */
    override fun capabilities(components: List<AIUIComponent>): Map<String, Any?> =
        capabilities(components, emptyList())

    override fun makeEngine(host: AIUIHost): AIUIEngine = A2UIEngine(this, host)

    /**
     * The catalog with the app's [components] under [catalogId], and the basic catalog with them under
     * its own id, so a surface naming either one shows them.
     */
    internal fun catalogs(components: List<AIUIComponent>): List<Catalog> {
        val definitions = PortableComponents.placeable(components, BasicCatalog.catalog).map { it.second }
        val extended = BasicCatalog.catalog.adding(definitions, catalogId)
        if (catalogId == BasicCatalog.catalog.catalogId) return listOf(extended)
        return listOf(extended, BasicCatalog.catalog.adding(definitions))
    }

    public companion object {
        /** The version of A2UI it reads. */
        public const val VERSION: String = A2UIProtocol.VERSION

        /** The id of the catalog with the app's components, unless the renderer is given another. */
        public const val DEFAULT_CATALOG_ID: String = "getstream.io:app-catalog"

        /** The id of the A2UI 1.0 basic catalog. */
        public const val BASIC_CATALOG_ID: String = A2UIProtocol.BASIC_CATALOG_ID

        // The spellings of 1.0 agents use, including ones prompted before the 1.0 release.
        private val SUPPORTED_VERSIONS = setOf("1", "1.0", "1.0.0", "v1", "v1.0")
    }
}
