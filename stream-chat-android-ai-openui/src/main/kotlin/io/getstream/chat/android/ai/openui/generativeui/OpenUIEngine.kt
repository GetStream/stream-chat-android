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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.ai.compose.generativeui.AIUIAction
import io.getstream.chat.android.ai.compose.generativeui.AIUIComponent
import io.getstream.chat.android.ai.compose.generativeui.AIUIComponentContext
import io.getstream.chat.android.ai.compose.generativeui.AIUIDocument
import io.getstream.chat.android.ai.compose.generativeui.AIUIEngine
import io.getstream.chat.android.ai.compose.generativeui.AIUIEntrance
import io.getstream.chat.android.ai.compose.generativeui.AIUIFormat
import io.getstream.chat.android.ai.compose.generativeui.AIUIHost
import io.getstream.chat.android.ai.openui.R
import io.getstream.chat.android.ai.openui.lang.ActionEvent
import io.getstream.chat.android.ai.openui.lang.OpenUIError
import io.getstream.chat.android.ai.openui.lang.OpenUIRuntime
import io.getstream.chat.android.ai.openui.lang.ToolProvider
import io.getstream.chat.android.ai.openui.lang.withoutSecrets
import io.getstream.chat.android.ai.openui.library.ComponentSchema
import io.getstream.chat.android.ai.openui.rendering.DefinedComponent
import io.getstream.chat.android.ai.openui.rendering.Library
import io.getstream.chat.android.ai.openui.rendering.OpenUIView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * The OpenUI replies of one conversation, each with a runtime of its own, kept while the host is.
 *
 * @param library The library the agent writes with, before the app's components join it.
 * @param host The conversation's host.
 * @param toolProvider Runs the tools of `Query()` and `Mutation()`.
 * @param onError Receives each problem with a reply's program, with the reply's message id.
 * @param scope Where the runtimes run their tools: the main thread by default.
 */
internal class OpenUIEngine(
    library: Library,
    private val host: AIUIHost,
    private val toolProvider: ToolProvider,
    private val onError: (error: OpenUIError, messageId: String) -> Unit = { _, _ -> },
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) : AIUIEngine {

    /** The library with each part coming in as it shows, and the app's components joined. */
    val library: Library = library.withEntrances().addingContent(
        OpenUIRenderer.placeable(host.components, library.definition).map { (component, schema) ->
            definition(component, schema)
        },
    )

    private val runtimes = mutableMapOf<String, OpenUIRuntime>()

    // The person's message for a control without a label, from the app's resources once a reply shows.
    private var untitledLabel: String = ""

    /** The runtime of a reply, once its UI first showed. */
    fun runtime(messageId: String): OpenUIRuntime? = runtimes[messageId]

    override fun receive(document: AIUIDocument, messageId: String) {
        // A reply whose UI showed follows its program even while it is off screen.
        val program = document.text
        if (program == null) {
            // Without a program the reply shows its text, so its queries stop refreshing.
            dispose(messageId)
            return
        }
        runtimes[messageId]?.update(program, document.isStreaming)
    }

    // A reply that left the conversation stops its runtime, with its queries' refreshes.
    override fun remove(messageId: String) {
        dispose(messageId)
    }

    override fun content(document: AIUIDocument, messageId: String): (@Composable () -> Unit)? {
        val program = document.text ?: return null
        val runtime = runtimes[messageId]
            ?.also { it.update(program, document.isStreaming) }
            ?: makeRuntime(messageId, program, document.isStreaming)
        // A finished program with nothing to draw, such as plain prose, leaves the reply's text showing.
        if (!document.isStreaming && runtime.root == null) {
            dispose(messageId)
            return null
        }
        return { ReplyContent(runtime, messageId, program, document.isStreaming) }
    }

    // Stops a reply's runtime, with its refresh timers, and forgets it.
    private fun dispose(messageId: String) {
        runtimes.remove(messageId)?.dispose()
    }

    @Composable
    private fun ReplyContent(runtime: OpenUIRuntime, messageId: String, program: String, isStreaming: Boolean) {
        val uriHandler = LocalUriHandler.current
        val untitled = stringResource(R.string.stream_ai_openui_action_untitled)
        SideEffect {
            untitledLabel = untitled
            runtime.update(program, isStreaming)
        }
        CompositionLocalProvider(LocalReply provides Reply(messageId, uriHandler)) {
            OpenUIView(runtime, library = library, onOpenUrl = { url -> host.open(url, uriHandler) })
        }
    }

    // A reply's runtime starts with its program, so its first frame already shows it.
    private fun makeRuntime(messageId: String, program: String, isStreaming: Boolean): OpenUIRuntime {
        val state = (host.state(messageId) as? Map<*, *>)?.let(::openUIObject)
        val runtime = OpenUIRuntime(library.schema, scope, toolProvider, state)
        runtime.onAction = { event -> handle(event, messageId) }
        runtime.onError = { errors -> errors.forEach { onError(it, messageId) } }
        // What password fields hold stays on the device.
        runtime.onStateUpdate = { snapshot -> host.setState(messageId, snapshot.withoutSecrets.plainValue) }
        runtimes[messageId] = runtime
        runtime.update(program, isStreaming)
        return runtime
    }

    // The OpenUI component that shows one of the app's, with the app's view as it is.
    private fun definition(component: AIUIComponent, schema: ComponentSchema) = DefinedComponent(schema) { context ->
        val reply = LocalReply.current
        if (reply != null) {
            val componentContext = AIUIComponentContext(
                messageId = reply.messageId,
                isStreaming = context.isStreaming,
                host = host,
                uriHandler = reply.uriHandler,
            ) { event, label, payload ->
                val details = payload + (ComponentKey to component.name)
                host.send(AIUIAction(reply.messageId, AIUIFormat.OpenUI, event, label, details))
            }
            AIUIEntrance { component.Content(context.props.values.plainValue, componentContext) }
        }
    }

    /** Sends what the person did that is for the agent to the host; links open in `OpenUIView`. */
    fun handle(event: ActionEvent, messageId: String) {
        action(event, messageId, untitledLabel)?.let(host::send)
    }

    /** Which reply a part of the UI is in, and the URI handler around it. */
    private class Reply(val messageId: String, val uriHandler: UriHandler)

    internal companion object {
        private val LocalReply = staticCompositionLocalOf<Reply?> { null }

        /**
         * The action an OpenUI event sends to the agent: named after OpenUI's action and labeled
         * with the control the person used ([untitled] when it has no label), with the action's
         * `context`, its other `params`, the model's `message` for it, the `form` and the form's
         * `values`, each only when there is one. A link sends nothing.
         */
        fun action(event: ActionEvent, messageId: String, untitled: String): AIUIAction? {
            if (event.isOpenUrl) return null
            val params = event.params.without("context")
            val payload = buildMap {
                event.context?.let { put("context", it.plainValue) }
                if (params.isNotEmpty()) put("params", params.plainValue)
                // The person's message is the control they used; the model's words for it go to
                // the agent only.
                val message = event.humanFriendlyMessage
                if (message.isNotEmpty() && message != event.label) put("message", message)
                // Only the values of the form the action belongs to go, as the person's next
                // message: outside a form the action carries none, and a password never.
                event.formName?.let { form ->
                    put("form", form)
                    event.formState?.withoutSecrets?.takeIf { it.isNotEmpty() }?.let { put("values", it.plainValue) }
                }
            }
            val label = event.label.ifEmpty { untitled }
            return AIUIAction(messageId, AIUIFormat.OpenUI, event.type, label, payload)
        }
    }
}

/** The library with each of its parts coming in as it shows, as the generative UI brings parts in. */
internal fun Library.withEntrances(): Library = Library(
    definition,
    definition.componentNames.associateWith { name ->
        renderer(name)?.let { render -> { context -> AIUIEntrance { render(context) } } }
    },
)

// The payload key that names the app's component an event came from.
private const val ComponentKey = "component"
