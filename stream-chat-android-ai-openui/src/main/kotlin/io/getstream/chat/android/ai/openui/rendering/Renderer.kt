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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.openui.lang.ActionEvent
import io.getstream.chat.android.ai.openui.lang.ElementNode
import io.getstream.chat.android.ai.openui.lang.JS
import io.getstream.chat.android.ai.openui.lang.OpenUIError
import io.getstream.chat.android.ai.openui.lang.OpenUIObject
import io.getstream.chat.android.ai.openui.lang.OpenUIRuntime
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.ParseResult
import io.getstream.chat.android.ai.openui.lang.ToolProvider
import io.getstream.chat.android.ai.openui.lang.stringValue
import io.getstream.chat.android.ai.openui.utils.OpenUILink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Renders a response from a runtime the caller owns, such as the generative UI engine's runtime
 * per reply. The view collects `runtime.state` and re-renders as statements arrive.
 *
 * @param runtime The runtime that owns the response's state.
 * @param modifier The modifier to apply.
 * @param library The renderers; must be the library whose schema the runtime was created with.
 * @param onAction Receives what the person does that is meant for the model. When `null`, it goes
 * to the handler `runtime.onAction` had before.
 * @param onOpenUrl Opens links. Defaults to the platform's URI handler.
 */
@Composable
internal fun OpenUIView(
    runtime: OpenUIRuntime,
    modifier: Modifier = Modifier,
    library: Library = openuiChatLibrary,
    onAction: ((ActionEvent) -> Unit)? = null,
    onOpenUrl: ((String) -> Unit)? = null,
) {
    val state by runtime.state.collectAsState()
    val store by runtime.storeSnapshot.collectAsState()
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    // A host handler decides which schemes it opens; on its own the view opens web and email links.
    val hostOpens = onOpenUrl != null
    val canOpen: (String?) -> Boolean = remember(hostOpens) {
        if (hostOpens) OpenUILink::isWellFormed else OpenUILink::isOpenable
    }
    val openUrl: (String) -> Unit = remember(onOpenUrl, uriHandler, canOpen) {
        val open = onOpenUrl ?: { url: String ->
            runCatching { uriHandler.openUri(url) }
            Unit
        }
        { url: String -> if (canOpen(url)) open(url) }
    }
    val currentOnAction by rememberUpdatedState(onAction)
    val currentOpenUrl by rememberUpdatedState(openUrl)

    // Links open here; other actions go to `onAction`, or to the runtime's own handler without one.
    DisposableEffect(runtime) {
        val previous = runtime.onAction
        runtime.onAction = { event ->
            val action = currentOnAction
            when {
                event.isOpenUrl -> event.params["url"].stringValue?.let(currentOpenUrl)
                action != null -> action(event)
                else -> previous?.invoke(event)
            }
        }
        onDispose { runtime.onAction = previous }
    }

    val root = state.root ?: return
    val alpha by animateFloatAsState(if (state.isQueryLoading) 0.7f else 1f, label = "queryLoading")
    CompositionLocalProvider(
        // Resolved once here, so components don't each build a theme from MaterialTheme.
        LocalOpenUITheme provides (LocalOpenUITheme.current ?: OpenUITheme.fromMaterial()),
        LocalOpenUIRuntime provides runtime,
        LocalComponentLibrary provides library,
        LocalOpenUIStreaming provides state.isStreaming,
        LocalOpenUIStoreSnapshot provides store,
        LocalOpenUIScope provides scope,
        LocalOpenUIOpenUrl provides openUrl,
        LocalOpenUICanOpenUrl provides canOpen,
        // Markdown and inline links open the way the host asked.
        LocalUriHandler provides remember(openUrl) { OpenUrlHandler(openUrl) },
    ) {
        Box(modifier) {
            Box(Modifier.alpha(alpha)) { OpenUIElement(root) }
            if (state.isQueryLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.TopEnd).padding(OpenUITheme.current.spacing.s).size(16.dp),
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

/**
 * Renders an OpenUI Lang response with native Compose components (port of `Renderer`).
 *
 * The renderer keeps the response's state, its `$variables`, forms and queries, for as long as it
 * stays in the composition, and follows [response] as it streams: partial programs render what
 * has parsed. The model writes every link, so without [onOpenUrl] only web and email links open.
 *
 * @param response The response text so far.
 * @param library The library the model was prompted with.
 * @param modifier The modifier to apply.
 * @param isStreaming Whether the model is still writing it; queries start once it ends.
 * @param onAction Receives what the person does that is meant for the model, such as a follow-up
 * or a form's submit button, and the library's custom actions.
 * @param onStateUpdate Receives the state whenever it changes, to persist and pass back as
 * [initialState].
 * @param initialState State persisted from an earlier [onStateUpdate].
 * @param onParseResult Receives each new parse of the response.
 * @param toolProvider Runs the tools of `Query()` and `Mutation()`.
 * @param onError Receives the response's errors whenever they change, empty once there are none.
 * @param onOpenUrl Opens links (`@OpenUrl`, `open_url` actions, Markdown links and citations),
 * and decides which schemes it accepts. Defaults to the platform's URI handler for web and email links.
 */
@Composable
public fun Renderer(
    response: String?,
    library: Library,
    modifier: Modifier = Modifier,
    isStreaming: Boolean = false,
    onAction: ((ActionEvent) -> Unit)? = null,
    onStateUpdate: ((OpenUIObject) -> Unit)? = null,
    initialState: OpenUIObject? = null,
    onParseResult: ((ParseResult?) -> Unit)? = null,
    toolProvider: ToolProvider? = null,
    onError: ((List<OpenUIError>) -> Unit)? = null,
    onOpenUrl: ((String) -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val currentOnStateUpdate by rememberUpdatedState(onStateUpdate)
    val currentOnError by rememberUpdatedState(onError)
    val currentOnParseResult by rememberUpdatedState(onParseResult)
    // Created with the text already parsed, so the first frame renders it.
    val runtime = remember(library) {
        OpenUIRuntime(library.schema, scope, toolProvider, initialState).also { runtime ->
            runtime.onStateUpdate = { state -> currentOnStateUpdate?.invoke(state) }
            runtime.onError = { errors -> currentOnError?.invoke(errors) }
            runtime.update(response, isStreaming)
        }
    }
    // Queries and mutations use the latest provider.
    SideEffect { runtime.queryManager.toolProvider = toolProvider }
    DisposableEffect(runtime) { onDispose { runtime.dispose() } }
    LaunchedEffect(runtime, response, isStreaming) { runtime.update(response, isStreaming) }
    LaunchedEffect(runtime) {
        runtime.state.map { it.parseResult }.distinctUntilChanged().collect { currentOnParseResult?.invoke(it) }
    }
    OpenUIView(runtime, modifier, library, onAction, onOpenUrl)
}

/** Renders any value the parser or evaluator produced: elements, arrays (as siblings) and primitives. */
@Composable
internal fun OpenUINode(value: OpenUIValue?) {
    when (value) {
        is OpenUIValue.Element -> OpenUIElement(value.node)
        is OpenUIValue.Arr -> value.items.forEach { OpenUINode(it) }
        is OpenUIValue.Str -> Text(
            value.value,
            style = OpenUITheme.current.typography.body,
            color = OpenUITheme.current.colors.textPrimary,
        )
        is OpenUIValue.Num -> Text(JS.numberToString(value.value), style = OpenUITheme.current.typography.body)
        is OpenUIValue.Bool -> Text(value.value.toString(), style = OpenUITheme.current.typography.body)
        else -> Unit
    }
}

/** Renders one element with its library renderer. Unknown and data-only components render nothing. */
@Composable
internal fun OpenUIElement(element: ElementNode) {
    val library = LocalComponentLibrary.current
    val renderer = library.renderer(element.typeName) ?: return
    val context = ComponentRenderProps(
        element = element,
        runtime = LocalOpenUIRuntime.current ?: return,
        library = library,
        isStreaming = LocalOpenUIStreaming.current,
        scope = LocalOpenUIScope.current ?: return,
    )
    renderer(context)
}

/** The runtime of the response being rendered. */
internal val LocalOpenUIRuntime: ProvidableCompositionLocal<OpenUIRuntime?> = staticCompositionLocalOf { null }

/** The library rendering the response. */
internal val LocalComponentLibrary: ProvidableCompositionLocal<Library> =
    staticCompositionLocalOf { openuiChatLibrary }

/** Whether the response is still streaming. */
internal val LocalOpenUIStreaming: ProvidableCompositionLocal<Boolean> = compositionLocalOf { false }

/** The store snapshot; reading it subscribes to changes of `$variables` and form fields. */
internal val LocalOpenUIStoreSnapshot = compositionLocalOf { OpenUIObject.Empty }

internal val LocalOpenUIScope = staticCompositionLocalOf<CoroutineScope?> { null }

/** Opens a URL the way the host asked. */
internal val LocalOpenUIOpenUrl = staticCompositionLocalOf<(String) -> Unit> { {} }

/** Whether a link can open: shown as a link and clickable only then. */
internal val LocalOpenUICanOpenUrl = staticCompositionLocalOf<(String?) -> Boolean> { OpenUILink::isOpenable }

/** The enclosing `Form`'s name (its fields are stored under it). */
internal val LocalOpenUIFormName: ProvidableCompositionLocal<String?> = compositionLocalOf { null }

private class OpenUrlHandler(private val open: (String) -> Unit) : UriHandler {
    override fun openUri(uri: String) = open(uri)
}
