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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.parts.AIMessagePart

/**
 * The steps an AI agent took while replying, in order. Show it before the reply's text.
 *
 * ```
 * AIMessageParts(parts = parts)
 * ```
 *
 * Each step shows through [AIMessagePartItem]: reasoning, tool calls, and a neutral placeholder
 * for kinds this SDK doesn't know. With an [AIToolApprover], a call waiting for this person's
 * approval shows its question under it. To show some steps your own way, such as reasoning you
 * stream separately or a kind of your own, use the overload that takes content and fall back to
 * [AIMessagePartItem] for the rest.
 *
 * @param parts The reply's steps, from [AIMessagePart.parts].
 * @param modifier The modifier to apply to the list.
 * @param approver Who answers calls' questions on this device, to ask them.
 * @param textStyle The style of the steps.
 * @param reasoningColors The palette of reasoning steps.
 * @param toolCallColors The palette of tool calls and of the placeholder for unknown steps.
 * @param toolApprovalColors The palette of calls' questions.
 */
@Suppress("LongParameterList") // One palette per kind of step.
@Composable
public fun AIMessageParts(
    parts: List<AIMessagePart>,
    modifier: Modifier = Modifier,
    approver: AIToolApprover? = null,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    reasoningColors: StreamingReasoningColors = StreamingReasoningDefaults.colors(),
    toolCallColors: AIToolCallColors = AIToolCallDefaults.colors(),
    toolApprovalColors: AIToolApprovalColors = AIToolApprovalDefaults.colors(),
) {
    AIMessageParts(parts = parts, modifier = modifier) { part ->
        AIMessagePartItem(
            part = part,
            approver = approver,
            textStyle = textStyle,
            reasoningColors = reasoningColors,
            toolCallColors = toolCallColors,
            toolApprovalColors = toolApprovalColors,
        )
    }
}

/**
 * The steps an AI agent took while replying, in order, each shown by [content].
 *
 * ```
 * AIMessageParts(parts = parts) { part ->
 *     val reasoning = part.reasoning
 *     if (reasoning != null) {
 *         StreamingReasoning(part = reasoning, text = liveText[reasoning.id])
 *     } else {
 *         AIMessagePartItem(part = part)
 *     }
 * }
 * ```
 *
 * @param parts The reply's steps, from [AIMessagePart.parts].
 * @param modifier The modifier to apply to the list.
 * @param content Shows one step. Steps are keyed by their ID, so a step keeps its state as the
 * reply updates.
 */
@Composable
public fun AIMessageParts(
    parts: List<AIMessagePart>,
    modifier: Modifier = Modifier,
    content: @Composable (part: AIMessagePart) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        parts.forEach { part ->
            key(part.kind.rawValue, part.id) { content(part) }
        }
    }
}

/**
 * One step of a reply: a round of reasoning with its preview, a tool call (with its question, when
 * it waits for the approver), or a neutral placeholder for a step this SDK doesn't know.
 *
 * @param part The step.
 * @param modifier The modifier to apply to the step.
 * @param approver Who answers calls' questions on this device, to ask them.
 * @param textStyle The style of the step.
 * @param reasoningColors The palette of a reasoning step.
 * @param toolCallColors The palette of a tool call and of the placeholder for an unknown step.
 * @param toolApprovalColors The palette of a call's question.
 */
@Suppress("LongParameterList") // One palette per kind of step.
@Composable
public fun AIMessagePartItem(
    part: AIMessagePart,
    modifier: Modifier = Modifier,
    approver: AIToolApprover? = null,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    reasoningColors: StreamingReasoningColors = StreamingReasoningDefaults.colors(),
    toolCallColors: AIToolCallColors = AIToolCallDefaults.colors(),
    toolApprovalColors: AIToolApprovalColors = AIToolApprovalDefaults.colors(),
) {
    val reasoning = part.reasoning
    val call = part.toolCall
    when {
        reasoning != null -> StreamingReasoning(
            part = reasoning,
            modifier = modifier,
            textStyle = textStyle,
            colors = reasoningColors,
        )
        call != null -> Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AIToolCall(part = call, textStyle = textStyle, colors = toolCallColors)
            if (approver != null) {
                AIToolApprovalPrompt(call = call, approver = approver) { approval, state, decide ->
                    AIToolApprovalCard(
                        approval = approval,
                        state = state,
                        decide = decide,
                        textStyle = textStyle,
                        colors = toolApprovalColors,
                    )
                }
            }
        }
        else -> UnsupportedPart(textStyle = textStyle, colors = toolCallColors, modifier = modifier)
    }
}
