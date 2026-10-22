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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.getstream.chat.android.ai.compose.conversation.AIMessage
import io.getstream.chat.android.ai.compose.conversation.AIMessageRole
import io.getstream.chat.android.ai.compose.conversation.AIPendingMessage
import io.getstream.chat.android.ai.compose.ui.component.internal.AIConversationEntry
import io.getstream.chat.android.ai.compose.ui.component.internal.AIPendingMessageTracker

/**
 * Shows [AIMessage]s with the views of [LocalChatAiComponentFactory]: each message the person sent
 * with [ChatAiComponentFactory.AIConversationUserMessage] and each reply with
 * [ChatAiComponentFactory.AIConversationAssistantMessage].
 *
 * A reply joins the list once it shows something; until then the list shows the agent thinking.
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
 */
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
    ) { entry ->
        AIConversationEntryContent(entry = entry, approver = approver)
    }
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
        else -> AIConversationReply(message = message, approver = approver)
    }
}

@Composable
private fun AIConversationReply(message: AIMessage, approver: AIToolApprover?) {
    with(LocalChatAiComponentFactory.current) {
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
