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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.conversation.AIMessageAttachment
import io.getstream.chat.android.ai.compose.conversation.AIMessageRole
import io.getstream.chat.android.ai.compose.parts.AIMessagePart
import io.getstream.chat.android.ai.compose.parts.AIReasoningPart
import io.getstream.chat.android.ai.compose.ui.component.internal.LocalAIThinkingTurn
import io.getstream.chat.android.ai.compose.ui.component.internal.LocalAnswerWriting
import io.getstream.chat.android.ai.compose.ui.component.internal.RichText
import kotlinx.coroutines.delay

/**
 * A reply from the agent: the steps it took, then its answer, written out as it arrives, and then
 * its attachments, across the whole width.
 *
 * ```
 * AIAssistantMessage(text = reply.text, isGenerating = reply.isGenerating, parts = reply.parts)
 * ```
 *
 * Reasoning shows with [StreamingReasoning], open while the model thinks, and other steps with
 * [AIMessagePartItem], which asks this person about a tool call that waits for their approval.
 * The answer shows with [StreamingText]. Each attachment shows through
 * [ChatAiComponentFactory.AIConversationAttachment], which renders [AIMessageAttachmentItem] by
 * default.
 *
 * @param text The answer, in Markdown.
 * @param modifier The modifier to apply to the reply.
 * @param isGenerating Whether the agent is still writing it.
 * @param parts The steps the agent took, in order.
 * @param attachments What the agent sent with it, such as images and files.
 * @param approver Who answers tool calls' questions and sign-ins on this device, to ask them.
 * @param reasoningText The whole of a reasoning step's thinking, when the app has it. Without it,
 * the step shows its own excerpt.
 */
@Suppress("LongParameterList") // One parameter per part of a reply.
@Composable
public fun AIAssistantMessage(
    text: String,
    modifier: Modifier = Modifier,
    isGenerating: Boolean = false,
    parts: List<AIMessagePart> = emptyList(),
    attachments: List<AIMessageAttachment> = emptyList(),
    approver: AIToolApprover? = null,
    reasoningText: (AIReasoningPart) -> String? = { null },
) {
    // Whether the answer is shown as still being written, which is what types it out. A fast
    // reply's text can arrive in the same update that finishes it, and only what arrives while
    // the answer is being written is typed, so it stays written a moment after the reply ends.
    // The answer counts as written until all of it shows.
    var writing by remember { mutableStateOf(isGenerating) }
    LaunchedEffect(isGenerating) {
        if (isGenerating) {
            writing = true
        } else {
            delay(WritingTailMs)
            writing = false
        }
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (parts.isNotEmpty()) {
            AIMessageParts(parts = parts) { part ->
                val step = part.reasoning
                if (step != null) {
                    StreamingReasoning(part = step, text = reasoningText(step))
                } else {
                    AIMessagePartItem(part = part, approver = approver)
                }
            }
        }
        // In a conversation, the answer waits for the reasoning above it to show and fold first.
        // This is read after the steps, since a first step that arrives with the answer is what
        // sets when the answer starts.
        val turn = LocalAIThinkingTurn.current
        val answerStartsAt = turn?.clock?.answerStartsAt(turn.turn)
        var held by remember(answerStartsAt) {
            mutableStateOf(answerStartsAt != null && answerStartsAt > turn.clock.now())
        }
        LaunchedEffect(answerStartsAt) {
            if (held && answerStartsAt != null) {
                delay(answerStartsAt - turn.clock.now())
                held = false
            }
        }
        val animate = writing || held
        // What of the answer shows so far. It starts as the text does, so an answer shown at once
        // isn't taken for one still being typed.
        var shown by remember { mutableStateOf(if (animate) "" else text) }
        val typing = text.isNotEmpty() && shown.length < text.length
        val answerWriting = isGenerating || writing || held || typing
        val reportWriting = LocalAnswerWriting.current
        LaunchedEffect(answerWriting, reportWriting) { reportWriting(answerWriting) }
        if (text.isNotEmpty()) {
            // A held answer types out once it is let go, and one made again after that shows at once.
            StreamingText(text = if (held) "" else text, animate = animate) { displayedText ->
                SideEffect { shown = displayedText }
                RichText(text = displayedText)
            }
        }
        val factory = LocalChatAiComponentFactory.current
        attachments.forEach { attachment ->
            with(factory) {
                AIConversationAttachment(AIConversationAttachmentParams(attachment, AIMessageRole.Assistant))
            }
        }
    }
}

/** How long a reply stays written after the agent finishes it, for text that came with the end. */
private const val WritingTailMs = 100L
