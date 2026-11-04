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

import android.util.Log
import io.getstream.chat.android.ai.a2ui.compose.A2UICatalog
import io.getstream.chat.android.ai.a2ui.core.A2UIJson
import io.getstream.chat.android.ai.a2ui.core.A2UIProtocol
import io.getstream.chat.android.ai.a2ui.core.BasicCatalog
import io.getstream.chat.android.ai.a2ui.core.Catalog
import io.getstream.chat.android.ai.compose.generativeui.AIUIComponent
import io.getstream.chat.android.ai.compose.generativeui.AIUIEngine
import io.getstream.chat.android.ai.compose.generativeui.AIUIFormat
import io.getstream.chat.android.ai.compose.generativeui.AIUIHost
import io.getstream.chat.android.ai.compose.generativeui.AIUIRenderer
import io.getstream.chat.android.ai.compose.parts.AIClientTool
import io.getstream.chat.android.ai.compose.parts.AIClientToolDefinition
import io.getstream.chat.android.ai.compose.parts.definition

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
 * agent's words, which could say something the person didn't choose. A function the catalog
 * doesn't define runs as one of the host's tools: a read-only one wherever the agent calls it, any
 * other only from a person's tap, with its output as the function's value. Links open through the
 * host, which decides the kinds of link that open.
 *
 * An app component joins the catalog under its A2UI name, its name in `PascalCase` (`place_card` is
 * `PlaceCard`), with its parameters as its properties. One whose name the basic catalog already
 * has, or that isn't a valid A2UI name, is left out, so it never replaces one of the catalog's own.
 * The catalog with the app's components goes by [catalogId], and the basic catalog's id shows them
 * as well.
 *
 * @param catalog How each component of the basic catalog looks, and which of them the agent can
 * use: one removed with [A2UICatalog.removing] isn't in the catalog the agent is told about. The
 * app's components look as they draw themselves.
 * @param catalogId The id the agent names the catalog with the app's components by.
 * @param instructions What the agent's model is told before A2UI's own instructions, such as which
 * answers to write as UI and when to use the app's components.
 * @param mediaUrl The URL an image, video or audio clip loads from, given the one the agent wrote,
 * or `null` to not load it, such as to load media from the app's own storage or through its CDN.
 * The agent writes these URLs, so return only ones the app trusts. It runs while the reply draws,
 * on the main thread. Images load only from web links whatever it returns. Without it, only web
 * links load.
 * @param onError Receives each problem with a reply's A2UI that A2UI reports to the agent, such as a
 * message that doesn't read, with the reply's message id. A chat has no channel for them, so forward
 * them to your backend if the agent should hear about them. Without it, the problems go to the log.
 */
public class A2UIRenderer(
    public val catalog: A2UICatalog = A2UICatalog.Basic,
    public val catalogId: String = DEFAULT_CATALOG_ID,
    private val instructions: String? = null,
    internal val mediaUrl: ((url: String) -> String?)? = null,
    private val onError: ((error: A2UIError, messageId: String) -> Unit)? = null,
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
     * then how a reply carries A2UI, the protocol, the catalog's guidelines, each tool with whether
     * it only reads, what it does and the schema of its arguments, and the catalog itself, in
     * Markdown.
     */
    override fun capabilities(components: List<AIUIComponent>, tools: List<AIClientTool>): Map<String, Any?> = mapOf(
        "fence" to fence,
        "catalog_id" to catalogId,
        "instructions" to A2UIInstructions(
            catalog = catalogs(components).first(),
            fence = fence,
            preamble = instructions,
            tools = tools.map { line(it.definition) },
        ).text,
    )

    override fun makeEngine(host: AIUIHost): AIUIEngine = A2UIEngine(this, host, onError ?: ::log)

    // A reply's problem, for the log when the app takes none.
    private fun log(error: A2UIError, messageId: String) {
        Log.w(LOG_TAG, "The A2UI of reply $messageId can't show: ${error.code} ${error.message}")
    }

    /**
     * The catalog with the app's [components] under [catalogId], and the basic catalog with them under
     * its own id, so a surface naming either one shows them.
     */
    internal fun catalogs(components: List<AIUIComponent>): List<Catalog> {
        val definitions = PortableComponents.placeable(components, basicCatalog).map { it.second }
        val extended = basicCatalog.adding(definitions, catalogId)
        if (catalogId == basicCatalog.catalogId) return listOf(extended)
        return listOf(extended, basicCatalog.adding(definitions))
    }

    /** The basic catalog with only the components [catalog] draws, as the agent is told it. */
    internal val basicCatalog: Catalog by lazy {
        BasicCatalog.catalog.removing(BasicCatalog.catalog.components.keys - catalog.renderers.keys)
    }

    public companion object {
        // A tool as the agent's instructions list it: its name, whether it only reads, what it does,
        // and the schema of its arguments.
        internal fun line(tool: AIClientToolDefinition): String = buildString {
            append("- `").append(tool.name).append('`')
            if (tool.isReadOnly) append(" (read-only)")
            if (tool.description.isNotEmpty()) append(": ").append(tool.description)
            tool.inputSchema?.let { append(" Arguments: `").append(A2UIJson.write(it, sortKeys = true)).append('`') }
        }

        /** The version of A2UI it reads. */
        public const val VERSION: String = A2UIProtocol.VERSION

        /** The id of the catalog with the app's components, unless the renderer is given another. */
        public const val DEFAULT_CATALOG_ID: String = "getstream.io:app-catalog"

        /** The id of the A2UI 1.0 basic catalog. */
        public const val BASIC_CATALOG_ID: String = A2UIProtocol.BASIC_CATALOG_ID

        private const val LOG_TAG = "StreamA2UI"

        // The spellings of 1.0 agents use, including ones prompted before the 1.0 release.
        private val SUPPORTED_VERSIONS = setOf("1", "1.0", "1.0.0", "v1", "v1.0")
    }
}
