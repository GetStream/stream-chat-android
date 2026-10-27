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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.conversation.AIMessage
import io.getstream.chat.android.ai.compose.conversation.AIMessageRole
import io.getstream.chat.android.ai.compose.conversation.AIPendingMessage
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
 * To float the composer over the list, pass the space it covers as [contentPadding]:
 *
 * ```
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
 * @param approver Who answers tool calls' questions on this device, to ask them.
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
) {
    val tracker = remember { AIPendingMessageTracker() }
    // A copy, so a list changed in place still counts as changed.
    val shown = messages.toList()
    val (entries, isAwaitingReply) = remember(tracker, shown, pendingMessage) {
        tracker.entries(shown, pendingMessage) to tracker.isAwaitingReply(shown, pendingMessage)
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
        AIConversationEntryContent(entry = entry, approver = approver)
    }
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
private fun AIConversationEntryContent(entry: AIConversationEntry, approver: AIToolApprover?) {
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
        else -> AIConversationReply(message = message, isLatestReply = entry.isLatestReply, approver = approver)
    }
}

@Composable
private fun AIConversationReply(message: AIMessage, isLatestReply: Boolean, approver: AIToolApprover?) {
    val factory = LocalChatAiComponentFactory.current
    // Whether the reply's answer is still being written out, which its actions wait for so they
    // do not move down with it. Null while its view has not said.
    var isWriting by remember { mutableStateOf<Boolean?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CompositionLocalProvider(LocalAnswerWriting provides { isWriting = it }) {
            with(factory) {
                AIConversationAssistantMessage(
                    AIConversationAssistantMessageParams(
                        message = message,
                        text = message.text,
                        isGenerating = message.isGenerating,
                        parts = message.parts,
                        attachments = message.attachments,
                        reasoningText = message::reasoningText,
                        approver = approver,
                    ),
                )
            }
        }
        AnimatedVisibility(
            visible = !message.isGenerating && message.text.isNotEmpty() && isWriting != true,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            with(factory) {
                AIConversationReplyActions(
                    AIConversationReplyActionsParams(
                        message = message,
                        text = message.text,
                        isLatestReply = isLatestReply,
                    ),
                )
            }
        }
    }
}
