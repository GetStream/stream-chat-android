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

package io.getstream.chat.android.ai.compose.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.conversation.AIAgentActivity
import io.getstream.chat.android.ai.compose.conversation.AIConversationState
import io.getstream.chat.android.ai.compose.conversation.AIConversationStatus
import io.getstream.chat.android.ai.compose.conversation.AIMessage
import io.getstream.chat.android.ai.compose.conversation.AIMessageRole
import io.getstream.chat.android.ai.compose.conversation.AIPendingMessage
import io.getstream.chat.android.ai.compose.conversation.AIReplyActions
import io.getstream.chat.android.ai.compose.generativeui.AIUIDocument
import io.getstream.chat.android.ai.compose.generativeui.AIUIEntranceStage
import io.getstream.chat.android.ai.compose.generativeui.AIUIHost
import io.getstream.chat.android.ai.compose.ui.component.internal.AIConversationEntry
import io.getstream.chat.android.ai.compose.ui.component.internal.AIPendingMessageTracker
import io.getstream.chat.android.ai.compose.ui.component.internal.LocalAnswerWriting

/**
 * Shows [AIMessage]s with the views of [LocalChatAiComponentFactory]: each message the person sent
 * with [ChatAiComponentFactory.AIConversationUserMessage], each reply with
 * [ChatAiComponentFactory.AIConversationAssistantMessage], and under a finished reply, once its
 * answer is written out, [ChatAiComponentFactory.AIConversationReplyActions].
 *
 * A reply joins the list once it shows something; until then the list shows the agent thinking.
 *
 * Given a [ui] host, a reply whose [AIMessage.uiDocument] one of its renderers shows has that UI
 * in place of its text, after its steps:
 *
 * ```
 * AIConversation(messages = messages, ui = viewModel.ui)
 * ```
 *
 * To float the composer over the list, pass the space it covers as [contentPadding]:
 *
 * ```
 * var composerHeight by remember { mutableStateOf(0.dp) }
 * val density = LocalDensity.current
 * Box {
 *     AIConversation(
 *         messages = messages,
 *         contentPadding = PaddingValues(bottom = composerHeight),
 *     )
 *     ChatComposer(
 *         onSendClick = ::send,
 *         onStopClick = ::stop,
 *         isGenerating = isGenerating,
 *         modifier = Modifier
 *             .align(Alignment.BottomCenter)
 *             .onSizeChanged { composerHeight = with(density) { it.height.toDp() } },
 *     )
 * }
 * ```
 *
 * @param messages The conversation, oldest message first.
 * @param modifier The modifier to apply.
 * @param pendingMessage A message the person sent that is not in [messages] yet. Keep passing it
 * until its reply starts: the list stops showing it once its copy arrives.
 * @param isThinking Whether the agent is answering the latest message and its reply shows nothing
 * yet. The list also knows it is while [pendingMessage] waits, and while the latest reply is being
 * written but shows nothing.
 * @param approver Who answers tool calls' questions and sign-ins on this device, to ask them.
 * @param loadOlderMessages Loads the messages before the first one, which the list asks for as the
 * person scrolls to its start. It shows a spinner meanwhile, and what is on screen stays in place
 * as they arrive. Pass null once there are none left.
 * @param footer A view under the latest turn's messages, such as what the agent is doing, or an
 * error with a way to retry.
 * @param emptyContent What the list shows while there are no messages, centered in it.
 * @param colors The palette of the list's own views: the button that brings the end back into view,
 * and the spinner while older messages load.
 * @param icons The icons of the list's own views.
 * @param contentPadding Space at the edges of the list that something drawn over it covers, such as
 * a composer floating over its bottom. The messages scroll under it but stop clear of it, and the
 * list's own views show inside it.
 * @param ui The conversation's generated UI: the renderers of the formats the agent writes, and
 * the app's components. A reply whose UI none of them shows has its text. Keep the same host while
 * the conversation shows, so each reply's UI keeps its state.
 */
@Suppress("LongParameterList")
@Composable
public fun AIConversation(
    messages: List<AIMessage>,
    modifier: Modifier = Modifier,
    pendingMessage: AIPendingMessage? = null,
    isThinking: Boolean = false,
    approver: AIToolApprover? = null,
    loadOlderMessages: (suspend () -> Unit)? = null,
    footer: @Composable () -> Unit = {},
    emptyContent: @Composable () -> Unit = {},
    colors: AIConversationColors = AIConversationDefaults.colors(),
    icons: AIConversationIcons = AIConversationDefaults.icons(),
    contentPadding: PaddingValues = PaddingValues(),
    ui: AIUIHost? = null,
) {
    AIConversationContent(
        messages = messages,
        modifier = modifier,
        pendingMessage = pendingMessage,
        isThinking = isThinking,
        approver = approver,
        loadOlderMessages = loadOlderMessages,
        footer = footer,
        emptyContent = emptyContent,
        colors = colors,
        icons = icons,
        contentPadding = contentPadding,
        ui = ui,
        replyActions = null,
    )
}

// The list of AIMessages, with what the conversation offers under each finished reply, if anything.
@Suppress("LongParameterList")
@Composable
private fun AIConversationContent(
    messages: List<AIMessage>,
    modifier: Modifier,
    pendingMessage: AIPendingMessage?,
    isThinking: Boolean,
    approver: AIToolApprover?,
    loadOlderMessages: (suspend () -> Unit)?,
    footer: @Composable () -> Unit,
    emptyContent: @Composable () -> Unit,
    colors: AIConversationColors,
    icons: AIConversationIcons,
    contentPadding: PaddingValues,
    ui: AIUIHost?,
    replyActions: ((AIMessage) -> AIReplyActions)?,
) {
    val tracker = remember { AIPendingMessageTracker() }
    // A copy, so a list changed in place still counts as changed.
    val shown = messages.toList()
    // The list composes only the replies on screen, so the engines take every reply's UI first.
    ui?.receive(shown)
    val (entries, isAwaitingReply) = remember(tracker, shown, pendingMessage, ui) {
        // A reply's UI counts only when the host reads its format; otherwise the reply waits.
        val canShow: (AIUIDocument) -> Boolean = { document -> ui?.canShow(document) == true }
        tracker.entries(shown, pendingMessage, canShow) to tracker.isAwaitingReply(shown, pendingMessage, canShow)
    }
    AIConversation(
        messages = entries,
        key = AIConversationEntry::id,
        isFromUser = AIConversationEntry::isFromUser,
        modifier = modifier,
        isThinking = isThinking || isAwaitingReply,
        loadOlderMessages = loadOlderMessages,
        footer = footer,
        emptyContent = emptyContent,
        colors = colors,
        icons = icons,
        contentPadding = contentPadding,
    ) { entry ->
        AIConversationEntryContent(entry = entry, approver = approver, ui = ui, replyActions = replyActions)
    }
}

/**
 * Shows an [AIConversationState]: its messages and the message on its way, the agent thinking
 * until its reply starts, what the agent says it is doing, and older messages as the person
 * scrolls to the start. The state's [AIConversationState.approver] answers tool calls' questions.
 *
 * ```
 * Column {
 *     AIConversation(state = conversation, modifier = Modifier.weight(1f))
 *     ChatComposer(state = conversation)
 * }
 * ```
 *
 * Under each finished reply, [ChatAiComponentFactory.AIConversationReplyActions] gets the reply's
 * actions: by default a row that copies, shares and rates it, through [AIConversationState.rate].
 *
 * While the agent says it is doing something besides thinking, such as checking external sources,
 * the latest turn ends with an [AITypingIndicator] showing what it says, in place of [AIThinking].
 *
 * @param state The conversation to show.
 * @param modifier The modifier to apply.
 * @param footer A view under the latest turn's messages, such as the state's error with a way to
 * [AIConversationState.retry].
 * @param emptyContent What the list shows while there are no messages, centered in it.
 * @param colors The palette of the list's own views: the button that brings the end back into view,
 * and the spinner while older messages load.
 * @param icons The icons of the list's own views.
 * @param contentPadding Space at the edges of the list that something drawn over it covers, such as
 * a composer floating over its bottom.
 * @param ui The conversation's generated UI. Make its host with [AIConversationState.send] as the
 * action callback, so what the person does in a reply's UI reaches the agent.
 */
@Suppress("LongParameterList")
@Composable
public fun AIConversation(
    state: AIConversationState<*>,
    modifier: Modifier = Modifier,
    footer: @Composable () -> Unit = {},
    emptyContent: @Composable () -> Unit = {},
    colors: AIConversationColors = AIConversationDefaults.colors(),
    icons: AIConversationIcons = AIConversationDefaults.icons(),
    contentPadding: PaddingValues = PaddingValues(),
    ui: AIUIHost? = null,
) {
    val messages by state.messages.collectAsState()
    val pending by state.pendingMessage.collectAsState()
    val status by state.status.collectAsState()
    val activity by state.activity.collectAsState()
    val hasOlderMessages by state.hasOlderMessages.collectAsState()
    val loadOlderMessages = remember<suspend () -> Unit>(state) { { state.loadOlderMessages() } }
    val ratings by state.ratings.collectAsState()
    val working = activity?.takeIf { it.isWorking }
    AIConversationContent(
        messages = messages,
        modifier = modifier,
        pendingMessage = pending,
        isThinking = status == AIConversationStatus.Submitted && working == null,
        approver = state.approver,
        loadOlderMessages = loadOlderMessages.takeIf { hasOlderMessages },
        footer = {
            if (working != null && status.isAnswering) {
                val text = working.text
                if (text.isNullOrBlank()) {
                    AITypingIndicator()
                } else {
                    AITypingIndicator(label = {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    })
                }
            }
            footer()
        },
        emptyContent = emptyContent,
        colors = colors,
        icons = icons,
        contentPadding = contentPadding,
        ui = ui,
        replyActions = { reply -> state.replyActions(reply, ratings) },
    )
}

/** Whether the agent says it is doing something besides thinking about or writing its answer. */
private val AIAgentActivity.isWorking: Boolean
    get() = state != AIAgentActivity.State.Thinking &&
        state != AIAgentActivity.State.Generating &&
        state != AIAgentActivity.State.Error

/**
 * The [AIConversation] signature without `ui`, kept so apps compiled against it keep working.
 *
 * @param messages The conversation, oldest message first.
 * @param modifier The modifier to apply.
 * @param pendingMessage A message the person sent that is not in [messages] yet.
 * @param isThinking Whether the agent is answering the latest message and its reply shows nothing
 * yet.
 * @param approver Who answers tool calls' questions and sign-ins on this device, to ask them.
 * @param loadOlderMessages Loads the messages before the first one.
 * @param footer A view under the latest turn's messages.
 * @param emptyContent What the list shows while there are no messages.
 * @param colors The palette of the list's own views.
 * @param icons The icons of the list's own views.
 * @param contentPadding Space at the edges of the list that something drawn over it covers.
 */
@Deprecated("Kept for binary compatibility.", level = DeprecationLevel.HIDDEN)
@Suppress("LongParameterList")
@Composable
public fun AIConversation(
    messages: List<AIMessage>,
    modifier: Modifier = Modifier,
    pendingMessage: AIPendingMessage? = null,
    isThinking: Boolean = false,
    approver: AIToolApprover? = null,
    loadOlderMessages: (suspend () -> Unit)? = null,
    footer: @Composable () -> Unit = {},
    emptyContent: @Composable () -> Unit = {},
    colors: AIConversationColors = AIConversationDefaults.colors(),
    icons: AIConversationIcons = AIConversationDefaults.icons(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    AIConversation(
        messages = messages,
        modifier = modifier,
        pendingMessage = pendingMessage,
        isThinking = isThinking,
        approver = approver,
        loadOlderMessages = loadOlderMessages,
        footer = footer,
        emptyContent = emptyContent,
        colors = colors,
        icons = icons,
        contentPadding = contentPadding,
        ui = null,
    )
}

/**
 * The [AIConversation] signature without `contentPadding`, kept so apps compiled against it keep
 * working.
 *
 * @param messages The conversation, oldest message first.
 * @param modifier The modifier to apply.
 * @param pendingMessage A message the person sent that is not in [messages] yet.
 * @param isThinking Whether the agent is answering the latest message and its reply shows nothing
 * yet.
 * @param approver Who answers tool calls' questions on this device, to ask them.
 * @param loadOlderMessages Loads the messages before the first one.
 * @param footer A view under the latest turn's messages.
 * @param emptyContent What the list shows while there are no messages.
 * @param colors The palette of the list's own views.
 * @param icons The icons of the list's own views.
 */
@Deprecated("Kept for binary compatibility.", level = DeprecationLevel.HIDDEN)
@Suppress("LongParameterList")
@Composable
public fun AIConversation(
    messages: List<AIMessage>,
    modifier: Modifier = Modifier,
    pendingMessage: AIPendingMessage? = null,
    isThinking: Boolean = false,
    approver: AIToolApprover? = null,
    loadOlderMessages: (suspend () -> Unit)? = null,
    footer: @Composable () -> Unit = {},
    emptyContent: @Composable () -> Unit = {},
    colors: AIConversationColors = AIConversationDefaults.colors(),
    icons: AIConversationIcons = AIConversationDefaults.icons(),
) {
    AIConversation(
        messages = messages,
        modifier = modifier,
        pendingMessage = pendingMessage,
        isThinking = isThinking,
        approver = approver,
        loadOlderMessages = loadOlderMessages,
        footer = footer,
        emptyContent = emptyContent,
        colors = colors,
        icons = icons,
        contentPadding = PaddingValues(),
    )
}

@Composable
private fun AIConversationEntryContent(
    entry: AIConversationEntry,
    approver: AIToolApprover?,
    ui: AIUIHost?,
    replyActions: ((AIMessage) -> AIReplyActions)?,
) {
    val factory = LocalChatAiComponentFactory.current
    val message = entry.message
    val pending = entry.pending
    when {
        message == null && pending != null -> with(factory) {
            AIConversationUserMessage(
                AIConversationUserMessageParams(message = null, text = pending.text, attachments = pending.attachments),
            )
        }
        message == null -> Unit
        message.role == AIMessageRole.User -> with(factory) {
            AIConversationUserMessage(
                AIConversationUserMessageParams(
                    message = message,
                    text = message.text,
                    attachments = entry.sentAttachments.ifEmpty { message.attachments },
                ),
            )
        }
        else -> AIConversationReply(
            message = message,
            isLatestReply = entry.isLatestReply,
            approver = approver,
            ui = ui,
            actions = replyActions?.invoke(message),
        )
    }
}

@Composable
private fun AIConversationReply(
    message: AIMessage,
    isLatestReply: Boolean,
    approver: AIToolApprover?,
    ui: AIUIHost?,
    actions: AIReplyActions?,
) {
    val factory = LocalChatAiComponentFactory.current
    // Whether the reply's answer is still being written out, which its actions wait for so they
    // do not move down with it. Null while its view has not said.
    var isWriting by remember { mutableStateOf<Boolean?>(null) }
    // The UI the agent generated for the reply, when one of the host's renderers shows it.
    val document = message.uiDocument
    val generated = if (ui != null && document != null) ui.content(document, message.id) else null
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CompositionLocalProvider(LocalAnswerWriting provides { isWriting = it }) {
            with(factory) {
                AIConversationAssistantMessage(
                    AIConversationAssistantMessageParams(
                        message = message,
                        text = if (generated == null) message.text else "",
                        isGenerating = message.isGenerating,
                        parts = message.parts,
                        attachments = message.attachments,
                        reasoningText = message::reasoningText,
                        approver = approver,
                    ),
                )
            }
        }
        if (generated != null && document != null) {
            AIUIEntranceStage(isStreaming = document.isStreaming, modifier = Modifier.fillMaxWidth()) {
                generated()
            }
        }
        val isWritingUI = generated != null && document?.isStreaming == true
        AnimatedVisibility(
            visible = !message.isGenerating &&
                (message.text.isNotEmpty() || generated != null) &&
                isWriting != true &&
                !isWritingUI,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            with(factory) {
                AIConversationReplyActions(
                    AIConversationReplyActionsParams(
                        message = message,
                        text = message.text,
                        isLatestReply = isLatestReply,
                        actions = actions,
                    ),
                )
            }
        }
    }
}
