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

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.a2ui.compose.A2UICatalog
import io.getstream.chat.android.ai.a2ui.compose.A2UIComponentScope
import io.getstream.chat.android.ai.a2ui.compose.A2UISurfaceView
import io.getstream.chat.android.ai.a2ui.core.A2UIJson
import io.getstream.chat.android.ai.a2ui.core.A2UIProtocol
import io.getstream.chat.android.ai.a2ui.core.A2UIStream
import io.getstream.chat.android.ai.a2ui.core.AgentMessage
import io.getstream.chat.android.ai.a2ui.core.BasicCatalog
import io.getstream.chat.android.ai.a2ui.core.ComponentDefinition
import io.getstream.chat.android.ai.a2ui.core.DataContext
import io.getstream.chat.android.ai.a2ui.core.DynamicValue
import io.getstream.chat.android.ai.a2ui.core.FunctionResponse
import io.getstream.chat.android.ai.a2ui.core.MessageProcessor
import io.getstream.chat.android.ai.a2ui.core.RendererError
import io.getstream.chat.android.ai.a2ui.core.RendererMessage
import io.getstream.chat.android.ai.a2ui.core.RendererServices
import io.getstream.chat.android.ai.a2ui.core.TopologyValidator
import io.getstream.chat.android.ai.compose.generativeui.AIUIAction
import io.getstream.chat.android.ai.compose.generativeui.AIUIComponent
import io.getstream.chat.android.ai.compose.generativeui.AIUIComponentContext
import io.getstream.chat.android.ai.compose.generativeui.AIUIDocument
import io.getstream.chat.android.ai.compose.generativeui.AIUIEngine
import io.getstream.chat.android.ai.compose.generativeui.AIUIFormat
import io.getstream.chat.android.ai.compose.generativeui.AIUIHost
import io.getstream.chat.android.ai.compose.generativeui.AIUIToolException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * The A2UI replies of one conversation, each with a message processor of its own, kept while the
 * host is.
 *
 * @param renderer The renderer that made it.
 * @param host The conversation's host.
 * @param schedule Runs a block on the main thread after a delay, for calls to the agent that time out.
 * @param scope Runs the host's tools, answering on the main thread.
 */
internal class A2UIEngine(
    renderer: A2UIRenderer,
    private val host: AIUIHost,
    private val schedule: (Long, () -> Unit) -> Unit = { delay, block -> MainHandler.postDelayed(block, delay) },
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) : AIUIEngine {

    private val placeable: List<Pair<AIUIComponent, ComponentDefinition>> =
        PortableComponents.placeable(host.components, BasicCatalog.catalog)

    /** The catalogs every reply's processor reads, with the app's components joined. */
    private val catalogs = renderer.catalogs(host.components)

    private val defaultCatalogId = renderer.catalogId

    /** How the basic catalog's components look. */
    private val basicLook: A2UICatalog = renderer.catalog

    private val replies = mutableMapOf<String, A2UIReply>()

    /** The reply with [messageId], once its UI first showed. */
    fun reply(messageId: String): A2UIReply? = replies[messageId]

    override fun receive(document: AIUIDocument, messageId: String) {
        // A reply whose UI showed follows its messages even while it is off screen.
        replies[messageId]?.update(messages(document), document.isStreaming)
    }

    // A reply that left the conversation lets go of its surfaces and their data.
    override fun remove(messageId: String) {
        replies.remove(messageId)
    }

    override fun content(document: AIUIDocument, messageId: String): (@Composable () -> Unit)? {
        val messages = messages(document) ?: return null
        val reply = replies[messageId] ?: makeReply(messageId, messages, document.isStreaming)
        // A finished reply that drew no surface shows its text instead.
        if (!document.isStreaming && reply.processor.surfaces.isEmpty() && reply.applied == messages) return null
        return { ReplyContent(reply, messages, document.isStreaming) }
    }

    @Composable
    private fun ReplyContent(reply: A2UIReply, messages: List<String>, isStreaming: Boolean) {
        val uriHandler = LocalUriHandler.current
        // The handler holds the screen's context, so a reply lets it go once no composition shows it.
        DisposableEffect(reply, uriHandler) {
            reply.uriHandlers.add(uriHandler)
            onDispose { reply.uriHandlers.remove(uriHandler) }
        }
        SideEffect { reply.update(messages, isStreaming) }
        val revision by reply.revision
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(SurfaceSpacing)) {
            // Keyed by id, so a surface never takes over what a deleted one remembered.
            reply.processor.surfaces.values.forEach { surface ->
                key(surface.surfaceId) {
                    A2UISurfaceView(
                        surface = surface,
                        revision = revision,
                        catalog = reply.catalog,
                        entrances = true,
                    )
                }
            }
        }
    }

    // A reply's surfaces start with its messages so far, so its first frame already shows them.
    private fun makeReply(messageId: String, messages: List<String>, isStreaming: Boolean): A2UIReply {
        val reply = A2UIReply(messageId)
        replies[messageId] = reply
        reply.update(messages, isStreaming)
        return reply
    }

    // One of the app's components in [reply], with the arguments the agent wrote for its parameters.
    @Composable
    private fun AppComponent(component: AIUIComponent, scope: A2UIComponentScope, reply: A2UIReply) {
        val arguments = component.definition.parameters.mapNotNull { parameter ->
            scope.value(parameter.name)?.let { parameter.name to it }
        }.toMap()
        val context = AIUIComponentContext(
            messageId = reply.messageId,
            isStreaming = reply.isStreaming,
            host = host,
            uriHandler = reply.uriHandler ?: LocalUriHandler.current,
        ) { event, label, payload ->
            val details = payload + (COMPONENT_KEY to component.name)
            host.send(AIUIAction(reply.messageId, AIUIFormat.A2UI, event, label, details))
        }
        // The parent's weight and the agent's accessibility attributes apply to it like to any other.
        Box(scope.modifier) { component.Content(arguments, context) }
    }

    /** Sends what the person did to the host, runs the host's tools, and logs what can't show. */
    fun handle(message: RendererMessage, reply: A2UIReply) {
        when (message) {
            is RendererMessage.Action -> host.send(action(message, reply))
            is RendererMessage.Error ->
                Log.w(TAG, "A reply's A2UI can't show: ${message.error.code} ${message.error.message}")
            is RendererMessage.CallAgentFunction -> run(message, reply)
            is RendererMessage.RendererFunctionResponse -> Unit
        }
    }

    // A function the catalogs don't define runs as one of the host's tools, on this device, as a
    // chat has no channel to ask the agent. While a surface renders only a read-only tool runs; any
    // other only from a person's gesture. Without that tool the call fails at once, and the reply
    // reads the failure as the function's.
    private fun run(message: RendererMessage.CallAgentFunction, reply: A2UIReply) {
        val call = message.call
        scope.launch {
            val response = try {
                val output = host.runTool(call.name, call.arguments, userInitiated = message.userActivated)
                FunctionResponse(message.functionCallId, value = A2UIJson.normalize(output))
            } catch (error: AIUIToolException) {
                val failure = FunctionResponse.Failure(RendererError.EXECUTION_ERROR, error.message.orEmpty())
                FunctionResponse(message.functionCallId, error = failure)
            }
            // A reply that left the conversation meanwhile has no one to answer.
            if (replies[reply.messageId] === reply) {
                reply.processor.handle(AgentMessage.AgentFunctionResponse(response))
            }
        }
    }

    /**
     * The action a button's event sends: named after the event and labelled with the text of the
     * button, or else the event's name as words, such as "Submit form" for `submit_form`. The label
     * is never the agent's words, which could say something the person didn't choose. The payload
     * holds its surface, its component, its resolved context, the event's `userMessage`, and the
     * surface's data model when the agent created the surface with `sendDataModel`. What a person
     * typed into an `obscured` text field is in none of them.
     */
    fun action(message: RendererMessage.Action, reply: A2UIReply): AIUIAction = AIUIAction(
        messageId = reply.messageId,
        format = AIUIFormat.A2UI,
        name = message.name,
        label = buttonText(message, reply) ?: words(message.name),
        payload = buildMap {
            put("surfaceId", message.surfaceId)
            put("sourceComponentId", message.sourceComponentId)
            put("context", message.context)
            message.userMessage?.takeIf { it.isNotBlank() }?.let { put("userMessage", it) }
            reply.processor.synchronizedDataModel()[message.surfaceId]?.let { put("dataModel", it) }
        },
    )

    // What the button the person tapped says, never a secret the person typed. A path
    // relative to a list item isn't read: the action doesn't say which item the button is in.
    private fun buttonText(message: RendererMessage.Action, reply: A2UIReply): String? {
        val surface = reply.processor.surface(message.surfaceId) ?: return null
        val childId = surface.component(message.sourceComponentId)?.properties?.get("child") as? String ?: return null
        val text = when (val value = surface.component(childId)?.dynamic("text")) {
            is DynamicValue.Literal -> value.value
            is DynamicValue.Binding ->
                value.path.takeIf { it.isAbsolute }?.let(DataContext(surface.dataModelWithoutSecrets())::value)
            else -> null
        }
        return A2UIJson.string(text)?.takeIf { it.isNotBlank() }
    }

    /**
     * One reply's surfaces, which follow its messages as the agent writes them.
     *
     * @param messageId The reply's id.
     */
    inner class A2UIReply(val messageId: String) {
        /** Changes whenever a surface's components or data do. */
        val revision = mutableIntStateOf(0)

        /** The URI handlers of the compositions that show the reply, the latest last. */
        val uriHandlers = mutableListOf<UriHandler>()

        /** The URI handler of the composition that shows the reply, for its links. */
        val uriHandler: UriHandler? get() = uriHandlers.lastOrNull()

        // Whether the whole tree was checked since the agent last finished the reply.
        private var checked = false

        /** Whether the agent is still writing the reply; the app's components recompose when it ends. */
        var isStreaming: Boolean by mutableStateOf(false)
            private set

        /** How the reply's components look: the basic catalog's, and the app's drawing themselves. */
        val catalog: A2UICatalog = basicLook + A2UICatalog(
            placeable.associate { (component, definition) ->
                definition.name to { scope -> AppComponent(component, scope, this) }
            },
        )

        /** The messages applied so far, as written. */
        var applied: List<String> = emptyList()
            private set

        val processor: MessageProcessor = MessageProcessor(
            catalogs = catalogs,
            defaultCatalogId = defaultCatalogId,
            services = RendererServices(
                openUrl = { url -> uriHandler?.let { host.open(url, it) } ?: false },
                canOpenUrl = host::canOpen,
            ),
            schedule = schedule,
        ) { message -> handle(message, this) }.also { processor ->
            processor.addChangeListener { revision.intValue++ }
        }

        /**
         * Applies the [messages] not applied yet. Messages that changed, rather than grew, start the
         * surfaces again. Once the agent finished, the whole tree of each surface is checked.
         */
        fun update(messages: List<String>?, isStreaming: Boolean) {
            if (messages == null) return
            this.isStreaming = isStreaming
            if (messages.size < applied.size || messages.subList(0, applied.size) != applied) {
                processor.clear()
                applied = emptyList()
                checked = false
            }
            for (raw in messages.drop(applied.size)) {
                val json = A2UIJson.map(A2UIJson.parseOrNull(raw))
                if (json == null) {
                    Log.w(TAG, "A reply's A2UI message isn't a JSON object")
                    continue
                }
                processor.handleAny(normalizedVersion(json))
            }
            applied = messages
            // A reply can arrive finished, so this doesn't wait for streaming to stop. Placeholders
            // an incremental reply replaced stay behind, unreachable, so orphans are fine.
            if (isStreaming) {
                checked = false
            } else if (!checked) {
                checked = true
                processor.validateTopology(TopologyValidator(allowOrphans = true))
            }
        }
    }

    internal companion object {
        private const val TAG = "StreamA2UI"
        private const val COMPONENT_KEY = "component"
        private val SurfaceSpacing = 12.dp
        private val MainHandler by lazy { Handler(Looper.getMainLooper()) }

        // The fence names the version 1.0, which a model may copy into its messages in another spelling.
        private val VERSION_SPELLINGS = setOf("1", "1.0.0", "v1", "v1.0")

        /**
         * The A2UI messages of a document, as written: one JSON object after another in text, or a
         * message, a list of them or `{"messages": [...]}` sent as JSON.
         */
        fun messages(document: AIUIDocument): List<String>? = when (val content = document.content) {
            is String -> A2UIStream.messages(content)
            // An entry that isn't an object stays, so applying it logs it.
            is List<*> -> content.map { item -> A2UIJson.write(item) }
            is Map<*, *> -> {
                val list = A2UIJson.list(content["messages"])
                list?.map { item -> A2UIJson.write(item) } ?: listOf(A2UIJson.write(content))
            }
            else -> null
        }

        /**
         * An event's name as words, such as "Submit form" for `submit_form` or `submitForm`, for a
         * button without text of its own. Underscores, hyphens and spaces separate words, as does
         * an uppercase letter after a lowercase one; the words are lowercase but for the first letter.
         */
        fun words(name: String): String {
            val words = mutableListOf<String>()
            val word = StringBuilder()
            for (char in name) {
                val separates = char == '_' || char == '-' || char == ' '
                val startsWord = char.isUpperCase() && word.lastOrNull()?.isLowerCase() == true
                if (separates || startsWord) {
                    if (word.isNotEmpty()) words.add(word.toString())
                    word.clear()
                    if (!separates) word.append(char)
                } else {
                    word.append(char)
                }
            }
            if (word.isNotEmpty()) words.add(word.toString())
            return words.joinToString(" ").lowercase(Locale.ROOT).replaceFirstChar { it.uppercase(Locale.ROOT) }
        }

        private fun normalizedVersion(json: Map<String, Any?>): Map<String, Any?> {
            val list = A2UIJson.list(json["messages"])
            if (list != null) {
                return json + ("messages" to list.map { item -> A2UIJson.map(item)?.let(::normalizedVersion) ?: item })
            }
            return if (json["version"] in VERSION_SPELLINGS) json + ("version" to A2UIProtocol.VERSION) else json
        }
    }
}
