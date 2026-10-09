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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.R
import io.getstream.chat.android.ai.compose.parts.AIToolApproval
import io.getstream.chat.android.ai.compose.parts.AIToolCallPart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * The person answering tool calls' questions on this device, and how their answer reaches your
 * backend, which holds each call until it gets one.
 *
 * The approver keeps where each answer is, so create it where it outlives the screen, such as a
 * ViewModel. It is confined to the main thread: pass a [scope] that runs on it, such as a
 * `viewModelScope`.
 *
 * @param userId The person signed in, matched against a call's `target_user_id`.
 * @param clientId This install, matched against a client tool call's `target_client_id`. Use
 * `AIClientIdentity.installId`.
 * @param scope Where answers are sent.
 * @param decide Sends the answer. Your backend checks it is this person's (and this install's, for
 * a client tool), then updates the call's step; until then the question stays, with its buttons
 * disabled. A thrown exception lets the person answer again.
 */
public class AIToolApprover(
    public val userId: String,
    public val clientId: String,
    private val scope: CoroutineScope,
    decide: suspend (call: AIToolCallPart, allowed: Boolean) -> Unit,
) {
    private val send = decide
    private val states = mutableStateMapOf<String, AIToolApprovalState>()

    /** Where the person's answer to [call] is. */
    internal fun state(call: AIToolCallPart): AIToolApprovalState = states[call.id] ?: AIToolApprovalState()

    /** Sends the person's answer to [call], one at a time. A failed send can be answered again. */
    @Suppress("TooGenericExceptionCaught")
    internal fun decide(call: AIToolCallPart, allowed: Boolean) {
        if (state(call).isSending) return
        states[call.id] = AIToolApprovalState(isSending = true)
        scope.launch {
            try {
                send(call, allowed)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                states[call.id] = AIToolApprovalState(failed = true)
            }
        }
    }
}

/**
 * Where the person's answer to a question is.
 *
 * @param isSending The answer is on its way, or arrived and the call's step hasn't changed yet.
 * @param failed The answer couldn't be sent, and the person can answer again.
 */
public data class AIToolApprovalState(
    val isSending: Boolean = false,
    val failed: Boolean = false,
)

/**
 * Asks the person a tool call waits for whether it may run: the question its `ai_tool_call` step
 * carries (status `awaiting_approval`, with `approval`). It shows only to that person, on the
 * device the call names, and only while the call waits, so it can sit under every tool call:
 *
 * ```
 * AIToolCall(part = call)
 * AIToolApprovalPrompt(call = call, approver = approver)
 * ```
 *
 * To ask in your own design, pass the content. It gets the question, where the answer is, and a
 * function that answers:
 *
 * ```
 * AIToolApprovalPrompt(call = call, approver = approver) { approval, state, decide ->
 *     MyApprovalCard(approval.title, busy = state.isSending, onAllow = { decide(true) }, onDecline = { decide(false) })
 * }
 * ```
 *
 * @param call The tool call.
 * @param approver Who answers on this device, and how the answer is sent.
 * @param modifier The modifier to apply to the question.
 * @param content Shows the question. Defaults to [AIToolApprovalCard].
 */
@Composable
public fun AIToolApprovalPrompt(
    call: AIToolCallPart,
    approver: AIToolApprover,
    modifier: Modifier = Modifier,
    content: @Composable (
        approval: AIToolApproval,
        state: AIToolApprovalState,
        decide: (allowed: Boolean) -> Unit,
    ) -> Unit =
        { approval, state, decide -> AIToolApprovalCard(approval = approval, state = state, decide = decide) },
) {
    val approval = call.approval
    if (approval == null || !call.isAwaitingApproval(approver.userId, approver.clientId)) return
    Box(modifier = modifier) {
        content(approval, approver.state(call)) { allowed -> approver.decide(call, allowed) }
    }
}

/**
 * A tool call's question: what it asks, the agent's reason and what allowing it shares, and
 * buttons to allow or decline it.
 *
 * @param approval The question.
 * @param state Where the person's answer is. Buttons are disabled while it is sent.
 * @param decide Answers: `true` allows the call, `false` declines it.
 * @param modifier The modifier to apply to the card.
 * @param textStyle The style of the question. Its lines use a smaller style.
 * @param colors The palette of the card.
 */
@Composable
public fun AIToolApprovalCard(
    approval: AIToolApproval,
    state: AIToolApprovalState,
    decide: (allowed: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    colors: AIToolApprovalColors = AIToolApprovalDefaults.colors(),
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background, shape)
            .border(1.dp, colors.border, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = approval.title, style = textStyle.copy(fontWeight = FontWeight.SemiBold), color = colors.title)
            approvalLines(approval).forEach { line ->
                Text(text = line, style = MaterialTheme.typography.bodySmall, color = colors.message)
            }
        }
        ApprovalButtons(approval, state, decide, colors)
        if (state.failed) {
            Text(
                text = stringResource(R.string.stream_ai_compose_tool_approval_not_sent),
                style = MaterialTheme.typography.labelSmall,
                color = colors.failure,
            )
        }
    }
}

/**
 * The palette of [AIToolApprovalCard].
 *
 * @param title Color of the question, such as "Share your location?".
 * @param message Color of the agent's reason and what allowing it shares.
 * @param background Background of the card.
 * @param border Border of the card.
 * @param accent Tint of the buttons that allow or decline the call.
 * @param failure Color of the note when an answer could not be sent.
 */
@Immutable
public data class AIToolApprovalColors(
    val title: Color,
    val message: Color,
    val background: Color,
    val border: Color,
    val accent: Color,
    val failure: Color,
)

/** Defaults of [AIToolApprovalCard]. */
public object AIToolApprovalDefaults {

    /**
     * The default palette, from the current [MaterialTheme].
     *
     * @param title Color of the question.
     * @param message Color of the agent's reason and what allowing it shares.
     * @param background Background of the card.
     * @param border Border of the card.
     * @param accent Tint of the buttons that allow or decline the call.
     * @param failure Color of the note when an answer could not be sent.
     */
    @Composable
    public fun colors(
        title: Color = MaterialTheme.colorScheme.onSurface,
        message: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        background: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border: Color = MaterialTheme.colorScheme.outlineVariant,
        accent: Color = MaterialTheme.colorScheme.primary,
        failure: Color = MaterialTheme.colorScheme.error,
    ): AIToolApprovalColors = AIToolApprovalColors(
        title = title,
        message = message,
        background = background,
        border = border,
        accent = accent,
        failure = failure,
    )
}

/** The agent's reason, as a sentence, then what allowing it shares. */
internal fun approvalLines(approval: AIToolApproval): List<String> = buildList {
    val reason = approval.reason?.trim()
    if (!reason.isNullOrEmpty()) {
        val sentence = reason.replaceFirstChar { it.uppercase() }
        add(if (sentence.last() in SentenceEnds) sentence else "$sentence.")
    }
    approval.message?.let(::add)
}

@Composable
private fun ApprovalButtons(
    approval: AIToolApproval,
    state: AIToolApprovalState,
    decide: (Boolean) -> Unit,
    colors: AIToolApprovalColors,
) {
    val allow = approval.allowTitle ?: stringResource(R.string.stream_ai_compose_tool_approval_allow)
    val decline = approval.declineTitle ?: stringResource(R.string.stream_ai_compose_tool_approval_decline)
    // Long titles or a large font move a button to the next line instead of squeezing it.
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = { decide(true) },
            enabled = !state.isSending,
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
            contentPadding = BUTTON_PADDING,
        ) {
            Text(allow)
        }
        OutlinedButton(
            onClick = { decide(false) },
            enabled = !state.isSending,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.accent),
            border = BorderStroke(1.dp, colors.border),
            contentPadding = BUTTON_PADDING,
        ) {
            Text(decline)
        }
        if (state.isSending) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = colors.accent, strokeWidth = 2.dp)
        }
    }
}

private const val SentenceEnds = ".!?…"
private val BUTTON_PADDING = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
