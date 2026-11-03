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

import android.content.ActivityNotFoundException
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.UriHandler
import io.getstream.chat.android.ai.compose.conversation.AIMessage
import io.getstream.chat.android.ai.compose.conversation.AIMessageRole
import io.getstream.chat.android.ai.compose.parts.AIClientTool
import io.getstream.chat.android.ai.compose.parts.AIClientToolResult
import io.getstream.chat.android.ai.compose.parts.AIToolCallPart
import io.getstream.chat.android.ai.compose.parts.Json
import org.json.JSONException
import java.util.Locale
import java.util.UUID

/**
 * The generated UI of one conversation: the renderers of the formats it reads, the app's
 * components and tools, each reply's state, and where what the person does in it goes.
 *
 * A reply carries its UI as an [AIUIDocument], which shows in place of its text when one of the
 * host's renderers reads its format. Keep one host for as long as its conversation shows, such as
 * in a `ViewModel`, so each reply's UI keeps its state across scrolling and configuration
 * changes. It is confined to the main thread.
 *
 * ```
 * val ui = AIUIHost(
 *     renderers = listOf(OpenUIRenderer()),
 *     components = listOf(placeCard),
 * ) { action ->
 *     viewModelScope.launch { channel.sendMessage(Message(text = action.label, extraData = action.customData)) }
 * }
 *
 * AIConversation(messages = messages, ui = ui)
 * ```
 *
 * @param renderers The renderer of each format the agent writes, the first one listed asked first.
 * @param components The app's views the agent can place.
 * @param tools The app's tools a reply's UI can run, to load data on this device.
 * @param linkSchemes The kinds of link a reply's UI may open: web and email links by default
 * (`http`, `https` and `mailto`). The agent writes every link, so add another scheme only when the
 * app wants it.
 * @param send Sends what the person did to the agent, as their next message.
 */
public class AIUIHost(
    public val renderers: List<AIUIRenderer>,
    public val components: List<AIUIComponent> = emptyList(),
    tools: List<AIClientTool> = emptyList(),
    linkSchemes: Set<String> = setOf(HTTP, HTTPS, MAILTO),
    private val send: (AIUIAction) -> Unit,
) {
    private val tools: Map<String, AIClientTool> = tools.distinctBy { it.name }.associateBy { it.name }
    private val linkSchemes: Set<String> = linkSchemes.mapTo(mutableSetOf()) { it.lowercase(Locale.ROOT) }
    private val engines = mutableMapOf<Int, AIUIEngine>()
    private val states = mutableMapOf<String, Any?>()
    private val received = mutableMapOf<String, AIUIDocument>()

    /** The app's component the agent places by [name]. */
    public fun component(name: String): AIUIComponent? = components.firstOrNull { it.name == name }

    /**
     * Whether one of its renderers reads [document]. The one that does can still show nothing of
     * its own for a reply, which then shows its text, as [canShow] tells.
     */
    public fun reads(document: AIUIDocument): Boolean = position(document) != null

    /**
     * Whether a reply shows its UI rather than its text: one of the host's renderers reads
     * [document], and shows something of its own for the reply.
     */
    public fun canShow(document: AIUIDocument, messageId: String): Boolean = content(document, messageId) != null

    /** Sends what the person did to the agent. */
    public fun send(action: AIUIAction) {
        send.invoke(action)
    }

    /**
     * Runs one of the app's tools for a reply's UI, such as to load data on this device.
     *
     * @param name The tool's name.
     * @param arguments Its arguments, as plain Kotlin values.
     * @return The tool's output, as plain Kotlin values.
     * @throws AIUIToolException When the host has no such tool, or it fails.
     */
    public suspend fun runTool(name: String, arguments: Map<String, Any?> = emptyMap()): Any? {
        val tool = tools[name]
        val call = AIToolCallPart(
            id = UUID.randomUUID().toString(),
            name = name,
            status = AIToolCallPart.Status.AwaitingClient,
            executor = AIToolCallPart.Executor.Client,
            arguments = Json.write(arguments),
        )
        val result = tool?.run(call) ?: AIClientToolResult.failed("No tool named $name")
        result.failure?.let { throw AIUIToolException(it) }
        val output = result.output ?: return null
        return try {
            Json.parse(output)
        } catch (_: JSONException) {
            throw AIUIToolException("$name returned output that is not JSON")
        }
    }

    /**
     * Whether a reply's UI may open [url]: a link of one of the host's `linkSchemes`, web and
     * email links by default, with a host of its own when it is a web link.
     */
    public fun canOpen(url: String): Boolean {
        val scheme = url.substringBefore(':', missingDelimiterValue = "").lowercase(Locale.ROOT)
        val allowed = scheme.isNotEmpty() && scheme in linkSchemes
        if (!allowed || url.any { it.isWhitespace() || it.isISOControl() }) return false
        return scheme != HTTPS && scheme != HTTP || hasHost(url.substring(scheme.length + 1))
    }

    // Whether the rest of a web link, after its scheme, names a host.
    private fun hasHost(rest: String): Boolean {
        if (!rest.startsWith("//")) return false
        val authority = rest.drop(2).substringBefore('/').substringBefore('?').substringBefore('#')
        return authority.substringAfterLast('@').isNotEmpty()
    }

    /**
     * Opens a link from a reply's UI with [uriHandler], when [canOpen] lets it.
     *
     * @return Whether the link was opened.
     */
    public fun open(url: String, uriHandler: UriHandler): Boolean {
        if (!canOpen(url)) return false
        return try {
            uriHandler.openUri(url)
            true
        } catch (_: IllegalArgumentException) {
            // No app on the device opens it.
            false
        } catch (_: ActivityNotFoundException) {
            false
        }
    }

    /** The state a reply's UI was left in, such as a form's values, kept while the host is. */
    public fun state(messageId: String): Any? = states[messageId]

    /** Keeps the state a reply's UI was left in, or forgets it when [state] is `null`. */
    public fun setState(messageId: String, state: Any?) {
        if (state == null) states.remove(messageId) else states[messageId] = state
    }

    /**
     * Gives its renderers the UI of the conversation's replies, oldest first, as each arrives or
     * changes, whether or not the reply is on screen, so a format whose later replies change an
     * earlier one's UI follows them all. `AIConversation` does it: call it with the
     * conversation's messages when you show replies yourself with [AIUIView].
     */
    public fun receive(messages: List<AIMessage>) {
        messages.filter { it.role != AIMessageRole.User }.forEach { message ->
            val document = message.uiDocument
            if (document != null && received[message.id] != document) {
                received[message.id] = document
                position(document)?.let { engine(it).receive(document, message.id) }
            }
        }
    }

    /**
     * What the agent's backend needs to write UI this app shows, as plain Kotlin values: each
     * format it reads, with what its renderer adds, and the app's components.
     */
    public val capabilities: Map<String, Any?>
        get() = mapOf(
            "v" to 1,
            "renderers" to renderers.map { renderer ->
                renderer.capabilities(components) + ("format" to renderer.format.rawValue)
            },
            "components" to components.map { it.definition.toJson() },
        )

    /** The content of a reply's document from the first renderer that reads its format and version. */
    internal fun content(document: AIUIDocument, messageId: String): (@Composable () -> Unit)? {
        val position = position(document) ?: return null
        return engine(position).content(document, messageId)
    }

    private fun position(document: AIUIDocument): Int? = renderers
        .indexOfFirst { it.format == document.format && it.supports(document.version) }
        .takeIf { it >= 0 }

    // The engine of the renderer at this position, made the first time it is needed.
    private fun engine(position: Int): AIUIEngine = engines.getOrPut(position) { renderers[position].makeEngine(this) }
}

private const val HTTPS = "https"
private const val HTTP = "http"
private const val MAILTO = "mailto"

/**
 * A tool a reply's UI ran that the host has none of, or that failed.
 *
 * @param message Why, for logs.
 */
public class AIUIToolException(message: String) : Exception(message)
