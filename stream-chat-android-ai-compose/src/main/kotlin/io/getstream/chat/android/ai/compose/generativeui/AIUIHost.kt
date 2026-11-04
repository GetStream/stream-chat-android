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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.platform.UriHandler
import io.getstream.chat.android.ai.compose.conversation.AIMessage
import io.getstream.chat.android.ai.compose.conversation.AIMessageRole
import io.getstream.chat.android.ai.compose.parts.AIClientTool
import io.getstream.chat.android.ai.compose.parts.AIClientToolResult
import io.getstream.chat.android.ai.compose.parts.AIToolCallPart
import io.getstream.chat.android.ai.compose.parts.Json
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import org.json.JSONException
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

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
 * @param tools The app's tools a reply's UI can run on this device, which the agent's instructions
 * list. They run without asking the person. One that is [AIClientTool.isReadOnly] runs whenever
 * the UI asks, such as to load data while it shows; any other only when the person does something
 * that runs it.
 * @param linkSchemes The kinds of link a reply's UI may open: web and email links by default
 * (`http`, `https` and `mailto`). The agent writes every link, so add another scheme only when the
 * app wants it.
 * @param send Sends what the person did to the agent, as their next message.
 */
public class AIUIHost(
    renderers: List<AIUIRenderer>,
    components: List<AIUIComponent> = emptyList(),
    tools: List<AIClientTool> = emptyList(),
    linkSchemes: Set<String> = setOf(HTTP, HTTPS, MAILTO),
    private val send: (AIUIAction) -> Unit,
) {
    /** The renderer of each format the agent writes, the first one listed asked first. */
    public val renderers: List<AIUIRenderer> = renderers.toList()

    /** The app's views the agent can place. */
    public val components: List<AIUIComponent> = components.toList()

    private val tools: Map<String, AIClientTool> = tools.distinctBy { it.name }.associateBy { it.name }

    // The app's tools a reply's UI can run, in a steady order for the agent's instructions.
    private val toolList: List<AIClientTool> = this.tools.values.sortedBy { it.name }
    private val linkSchemes: Set<String> = linkSchemes.mapTo(mutableSetOf()) { it.lowercase(Locale.ROOT) }
    private val engines = mutableMapOf<Int, AIUIEngine>()
    private val states = mutableMapOf<String, Any?>()

    // Snapshot state like the engines' own, so a composition that is thrown away forgets what it
    // received along with what the engines did with it.
    private val received = mutableStateMapOf<String, AIUIDocument>()

    /**
     * The app's component the agent places by [name].
     *
     * @param name The component's name.
     */
    public fun component(name: String): AIUIComponent? = components.firstOrNull { it.name == name }

    /**
     * Whether one of its renderers reads [document]. The one that does can still show nothing of
     * its own for a reply, which then shows its text, as [showsUI] tells.
     *
     * @param document A reply's document.
     */
    public fun canShow(document: AIUIDocument): Boolean = position(document) != null

    /**
     * Whether a reply shows its UI rather than its text: one of the host's renderers reads
     * [document], and shows something of its own for the reply.
     *
     * @param document The reply's document.
     * @param messageId The reply's id.
     */
    public fun showsUI(document: AIUIDocument, messageId: String): Boolean = content(document, messageId) != null

    /**
     * Sends what the person did to the agent.
     *
     * @param action What they did, and in which reply's UI.
     */
    public fun send(action: AIUIAction) {
        send.invoke(action)
    }

    /**
     * Runs one of the app's tools for a reply's UI, such as to load data on this device.
     *
     * @param name The tool's name.
     * @param arguments Its arguments, as plain Kotlin values.
     * @param userInitiated Whether the person asked for it, such as with a tap, rather than the UI
     * while it shows. Only a read-only tool runs when they didn't.
     * @return The tool's output, as plain Kotlin values.
     * @throws AIUIToolException When the host has no such tool, it is not read-only and the person
     * did not ask for it, it fails or throws, or its output is not JSON.
     */
    public suspend fun runTool(
        name: String,
        arguments: Map<String, Any?> = emptyMap(),
        userInitiated: Boolean = false,
    ): Any? {
        val tool = runnable(name, userInitiated)
        val call = AIToolCallPart(
            id = UUID.randomUUID().toString(),
            name = name,
            status = AIToolCallPart.Status.AwaitingClient,
            executor = AIToolCallPart.Executor.Client,
            arguments = Json.write(arguments),
        )
        val result = run(tool, call)
        result.failure?.let { throw AIUIToolException(it) }
        val output = result.output ?: return null
        return try {
            Json.parse(output)
        } catch (_: JSONException) {
            throw AIUIToolException("$name returned output that is not JSON")
        }
    }

    // The tool of that name, when it may run: a read-only one whenever the UI asks, any other only
    // when the person asked for it.
    private fun runnable(name: String, userInitiated: Boolean): AIClientTool {
        val tool = tools[name] ?: throw AIUIToolException("No tool named $name")
        if (!userInitiated && !tool.isReadOnly) {
            throw AIUIToolException("$name isn't read-only, so it runs only when the person asks for it")
        }
        return tool
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private suspend fun run(tool: AIClientTool, call: AIToolCallPart): AIClientToolResult = try {
        tool.run(call)
    } catch (cancellation: CancellationException) {
        // Rethrows when the caller is cancelled; otherwise the tool's own timeout.
        currentCoroutineContext().ensureActive()
        AIClientToolResult.failed("${call.name} timed out")
    } catch (_: Exception) {
        AIClientToolResult.failed("${call.name} failed")
    }

    /**
     * Whether a reply's UI may open [url]: a link of one of the host's `linkSchemes`, web and
     * email links by default, with a host of its own when it is a web link.
     *
     * @param url The link the agent wrote.
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
        val host = authority.substringAfterLast('@')
        // An IPv6 host keeps its colons in brackets; otherwise a colon starts the port.
        val name = if (host.startsWith("[")) host.substringBefore(']') else host.substringBefore(':')
        return name.isNotEmpty()
    }

    /**
     * Opens a link from a reply's UI with [uriHandler], when [canOpen] lets it.
     *
     * @param url The link the agent wrote.
     * @param uriHandler What opens it, such as `LocalUriHandler.current`.
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

    /**
     * The state a reply's UI was left in, such as a form's values, kept while the host is.
     *
     * @param messageId The reply's id.
     */
    public fun state(messageId: String): Any? = states[messageId]

    /**
     * Keeps the state a reply's UI was left in, or forgets it when [state] is `null`.
     *
     * @param messageId The reply's id.
     * @param state The state to keep, as plain Kotlin values.
     */
    public fun setState(messageId: String, state: Any?) {
        if (state == null) states.remove(messageId) else states[messageId] = state
    }

    /**
     * Gives its renderers the UI of the conversation's replies, oldest first, as each arrives or
     * changes, whether or not the reply is on screen, so a format whose later replies change an
     * earlier one's UI follows them all. It also tells them when a reply leaves the conversation,
     * or its UI does, such as when it is deleted. `AIConversation` does it: call it with the
     * conversation's messages when you show replies yourself with [AIUIView].
     *
     * @param messages The conversation's messages, oldest first.
     */
    public fun receive(messages: List<AIMessage>) {
        val documents = LinkedHashMap<String, AIUIDocument>()
        messages.forEach { message ->
            val document = message.uiDocument
            if (message.role != AIMessageRole.User && document != null) documents[message.id] = document
        }
        // What it received is its own bookkeeping, not something to recompose for.
        Snapshot.withoutReadObservation {
            (received.keys - documents.keys).forEach { messageId ->
                val document = received.remove(messageId) ?: return@forEach
                states.remove(messageId)
                position(document)?.let { engine(it).remove(messageId) }
            }
            documents.forEach { (messageId, document) ->
                if (received[messageId] != document) {
                    received[messageId] = document
                    position(document)?.let { engine(it).receive(document, messageId) }
                }
            }
        }
    }

    /**
     * What the agent's backend needs to write UI this app shows, as plain Kotlin values: each
     * format it reads, with what its renderer adds, such as the instructions that list the app's
     * tools, and the app's components.
     */
    public val capabilities: Map<String, Any?>
        get() = mapOf(
            "v" to 1,
            "renderers" to renderers.map { renderer ->
                renderer.capabilities(components, toolList) + ("format" to renderer.format.rawValue)
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
