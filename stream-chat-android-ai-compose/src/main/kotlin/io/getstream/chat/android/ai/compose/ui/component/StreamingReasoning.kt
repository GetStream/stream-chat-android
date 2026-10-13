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

import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.R
import io.getstream.chat.android.ai.compose.parts.AIReasoningPart
import io.getstream.chat.android.ai.compose.ui.component.internal.ReasoningFoldState
import io.getstream.chat.android.ai.compose.ui.component.internal.ReasoningPanel
import io.getstream.chat.android.ai.compose.ui.component.internal.ReasoningStrings
import io.getstream.chat.android.ai.compose.ui.component.internal.pulse
import io.getstream.chat.android.ai.compose.ui.component.internal.reasoningTitle
import io.getstream.chat.android.ai.compose.ui.component.internal.rememberDurationStrings
import io.getstream.chat.android.ai.compose.ui.component.internal.shimmer
import io.getstream.chat.android.ai.compose.ui.component.internal.thinkingTitle
import kotlinx.coroutines.delay
import kotlin.math.max

/**
 * A model's reasoning (its "thinking"), shown alongside its reply.
 *
 * While the model thinks, the reasoning is open under a "Thinking… 7s" header: a panel that grows
 * with the thoughts, up to [maxExpandedHeight], then keeps the newest in view, revealing new text
 * smoothly as it arrives. When the model is done the view folds into "Thought for 12s" and its
 * summary, unless the reader opened or closed it themselves, and tapping the header opens the
 * whole reasoning again.
 *
 * Reasoning can run to tens of kilobytes and grow many times a second, so the view only lays out
 * what changes: the text is split into paragraphs that render lazily, and only the paragraph still
 * being written is laid out again. Folding shrinks the panel in place, so whatever sits below it
 * glides up with it instead of jumping.
 *
 * ```
 * StreamingReasoning(text = reasoning, isThinking = answer.isEmpty(), durationSeconds = 12.0)
 * ```
 *
 * @param text The reasoning so far. Blank lines separate paragraphs, and inline Markdown (bold,
 * italics, code and links) is rendered.
 * @param isThinking Whether the model is still thinking. While it is, the header counts the
 * seconds and the reasoning streams into view.
 * @param modifier The modifier to apply to the view.
 * @param durationSeconds How long the model has thought, shown as "Thought for 12s" once it is
 * done.
 * @param summary A one-line summary shown beside the header once the model is done.
 * @param footnote A note under the open reasoning, such as how long it is kept.
 * @param initiallyExpanded Whether the reasoning is open once the model is done.
 * @param showsLiveReasoning Whether the reasoning is open while the model thinks.
 * @param maxExpandedHeight How tall the reasoning grows before it scrolls.
 * @param textStyle The style of the reasoning. The header uses it in a medium weight.
 * @param colors The palette of the view.
 */
@Suppress("LongParameterList") // Mirrors StreamingReasoningView in stream-chat-swift-ai.
@Composable
public fun StreamingReasoning(
    text: String,
    isThinking: Boolean,
    modifier: Modifier = Modifier,
    durationSeconds: Double? = null,
    summary: String? = null,
    footnote: String? = null,
    initiallyExpanded: Boolean = false,
    showsLiveReasoning: Boolean = true,
    maxExpandedHeight: Dp = 260.dp,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    colors: StreamingReasoningColors = StreamingReasoningDefaults.colors(),
) {
    // Previews show live reasoning open at once rather than unfolding.
    val inspecting = LocalInspectionMode.current
    val state = remember {
        ReasoningFoldState(isThinking, showsLiveReasoning, initiallyExpanded, startsOpen = !isThinking || inspecting)
    }
    state.visibility.targetState = state.open
    LaunchedEffect(isThinking) {
        state.thinkingChanged(isThinking, durationSeconds, showsLiveReasoning, initiallyExpanded)
    }

    Column(
        modifier = modifier
            .drawBehind {
                val width = RULE_WIDTH.toPx()
                drawRoundRect(
                    color = if (isThinking) colors.shimmer else colors.rule,
                    size = Size(width, size.height),
                    cornerRadius = CornerRadius(width / 2),
                )
            }
            .padding(start = 12.dp),
    ) {
        ReasoningHeader(
            state = state,
            isThinking = isThinking,
            durationSeconds = durationSeconds ?: state.thoughtFor,
            summary = summary,
            textStyle = textStyle,
            colors = colors,
        )
        if (text.isNotEmpty()) {
            AnimatedVisibility(
                visibleState = state.visibility,
                enter = expandVertically(StreamingReasoningDefaults.foldAnimationSpec(), Alignment.Top) +
                    fadeIn(StreamingReasoningDefaults.foldAnimationSpec()),
                exit = shrinkVertically(StreamingReasoningDefaults.foldAnimationSpec(), Alignment.Top) +
                    fadeOut(StreamingReasoningDefaults.foldAnimationSpec()),
            ) {
                ReasoningBody(
                    text = text,
                    isThinking = isThinking,
                    // Only once the reader opens it: appearing as the reasoning folds by itself
                    // would push the reply down just as it starts.
                    footnote = footnote?.takeIf { state.showsFootnote(isThinking, initiallyExpanded) },
                    maxHeight = maxExpandedHeight,
                    textStyle = textStyle,
                    colors = colors,
                )
            }
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun ReasoningBody(
    text: String,
    isThinking: Boolean,
    footnote: String?,
    maxHeight: Dp,
    textStyle: TextStyle,
    colors: StreamingReasoningColors,
) {
    Column(
        modifier = Modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ReasoningPanel(
            text = text,
            isLive = isThinking,
            maxHeight = maxHeight,
            color = colors.text,
            style = textStyle,
        )
        if (footnote != null) {
            Text(
                text = footnote,
                style = MaterialTheme.typography.labelSmall,
                color = colors.footnote,
            )
        }
    }
}

/**
 * Shows a reasoning step: its live [text] when the app has it, otherwise the step's preview, with
 * its summary beside "Thought for 12s" once it is done.
 *
 * @param part The reasoning step.
 * @param modifier The modifier to apply to the view.
 * @param text The step's whole reasoning, when the app watched it live.
 * @param footnote A note under the open reasoning, such as how long it is kept.
 * @param textStyle The style of the reasoning. The header uses it in a medium weight.
 * @param colors The palette of the view.
 */
@Composable
public fun StreamingReasoning(
    part: AIReasoningPart,
    modifier: Modifier = Modifier,
    text: String? = null,
    footnote: String? = null,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    colors: StreamingReasoningColors = StreamingReasoningDefaults.colors(),
) {
    StreamingReasoning(
        text = text ?: part.preview ?: part.summary.orEmpty(),
        isThinking = part.isStreaming,
        modifier = modifier,
        durationSeconds = part.durationSeconds,
        summary = part.summary,
        footnote = footnote,
        textStyle = textStyle,
        colors = colors,
    )
}

/**
 * The palette of [StreamingReasoning].
 *
 * @param title Color of the header ("Thinking…", "Thought for 12s") and its icons.
 * @param text Color of the reasoning itself.
 * @param footnote Color of the note under the open reasoning.
 * @param shimmer Color of the highlight that sweeps across the header while the model thinks, and
 * of the rule along the reasoning's leading edge meanwhile.
 * @param rule Color of the rule along the reasoning's leading edge once the model is done.
 */
@Immutable
public data class StreamingReasoningColors(
    val title: Color,
    val text: Color,
    val footnote: Color,
    val shimmer: Color,
    val rule: Color,
)

/** Defaults of [StreamingReasoning]. */
public object StreamingReasoningDefaults {

    /**
     * How long the reasoning takes to fold once the model stops thinking. A reply that shows its
     * answer only after this lets the reasoning fold first, then types the answer below it, rather
     * than the two moving against each other.
     */
    public const val FoldDurationMillis: Int = 450

    /**
     * How the reasoning folds and opens. Use it for your own changes that should move with it.
     *
     * @param T The type of the animated value.
     */
    public fun <T> foldAnimationSpec(): SpringSpec<T> = spring(dampingRatio = FoldDamping, stiffness = FoldStiffness)

    /**
     * The default palette, from the current [MaterialTheme].
     *
     * @param title Color of the header and its icons.
     * @param text Color of the reasoning itself.
     * @param footnote Color of the note under the open reasoning.
     * @param shimmer Color of the highlight that sweeps across the header while the model thinks.
     * @param rule Color of the rule along the reasoning's leading edge.
     */
    @Composable
    public fun colors(
        title: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        text: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        footnote: Color = MaterialTheme.colorScheme.outline,
        shimmer: Color = MaterialTheme.colorScheme.onSurface,
        rule: Color = MaterialTheme.colorScheme.outlineVariant,
    ): StreamingReasoningColors = StreamingReasoningColors(
        title = title,
        text = text,
        footnote = footnote,
        shimmer = shimmer,
        rule = rule,
    )
}

@Suppress("LongParameterList")
@Composable
private fun ReasoningHeader(
    state: ReasoningFoldState,
    isThinking: Boolean,
    durationSeconds: Double?,
    summary: String?,
    textStyle: TextStyle,
    colors: StreamingReasoningColors,
) {
    val isOpen = state.open
    val onToggle = state::toggle
    val strings = rememberReasoningStrings()
    val title = reasoningTitle(isThinking, durationSeconds, strings)
    val hint = stringResource(
        if (isOpen) R.string.stream_ai_compose_reasoning_hide else R.string.stream_ai_compose_reasoning_show,
    )
    val openState = stringResource(
        if (isOpen) R.string.stream_ai_compose_reasoning_expanded else R.string.stream_ai_compose_reasoning_collapsed,
    )
    val description = listOfNotNull(title, summary.takeIf { !isThinking }).joinToString(". ")
    val chevronRotation by animateFloatAsState(if (isOpen) ChevronOpen else 0f, label = "chevron")
    val headerStyle = textStyle.copy(fontWeight = FontWeight.Medium, color = colors.title)
    Row(
        modifier = Modifier
            .clickable(onClickLabel = hint, role = Role.Button, onClick = onToggle)
            .minimumInteractiveComponentSize()
            .clearAndSetSemantics {
                contentDescription = description
                stateDescription = openState
                role = Role.Button
                onClick(label = hint) {
                    onToggle()
                    true
                }
            },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.stream_ai_compose_ic_reasoning),
            contentDescription = null,
            tint = colors.title,
            modifier = Modifier
                .size(16.dp)
                .pulse(isThinking),
        )
        HeaderTitle(isThinking, title, durationSeconds, strings, headerStyle, colors)
        if (summary != null && !isThinking && !isOpen) {
            Text(
                text = summary,
                style = textStyle.copy(fontWeight = FontWeight.Normal),
                color = colors.text.copy(alpha = SummaryAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
        Icon(
            painter = painterResource(R.drawable.stream_ai_compose_ic_chevron_right),
            contentDescription = null,
            tint = colors.title,
            modifier = Modifier
                .size(14.dp)
                .rotate(chevronRotation),
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun HeaderTitle(
    isThinking: Boolean,
    title: String,
    durationSeconds: Double?,
    strings: ReasoningStrings,
    style: TextStyle,
    colors: StreamingReasoningColors,
) {
    val modifier = Modifier.shimmer(isThinking, colors.shimmer)
    if (isThinking) {
        ThinkingTitle(durationSeconds, strings, style, modifier)
    } else {
        Text(text = title, style = style, modifier = modifier, maxLines = 1)
    }
}

/**
 * The header while the model thinks, counting the seconds. A view opened midway counts from how
 * long the model had already thought.
 */
@Composable
private fun ThinkingTitle(
    durationSeconds: Double?,
    strings: ReasoningStrings,
    style: TextStyle,
    modifier: Modifier,
) {
    val appeared = remember { SystemClock.elapsedRealtime() }
    var now by remember { mutableLongStateOf(appeared) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(SECOND - (now - appeared) % SECOND)
            now = SystemClock.elapsedRealtime()
        }
    }
    val elapsed = max(durationSeconds ?: 0.0, (now - appeared) / MILLIS)
    Text(
        text = thinkingTitle(elapsed, strings),
        style = style.copy(fontFeatureSettings = "tnum"),
        modifier = modifier,
        maxLines = 1,
    )
}

@Composable
private fun rememberReasoningStrings(): ReasoningStrings {
    val thinking = stringResource(R.string.stream_ai_compose_reasoning_thinking)
    val thinkingFor = stringResource(R.string.stream_ai_compose_reasoning_thinking_for)
    val thought = stringResource(R.string.stream_ai_compose_reasoning_thought)
    val thoughtFor = stringResource(R.string.stream_ai_compose_reasoning_thought_for)
    val duration = rememberDurationStrings()
    return remember(thinking, thinkingFor, thought, thoughtFor, duration) {
        ReasoningStrings(thinking, thinkingFor, thought, thoughtFor, duration)
    }
}

private val RULE_WIDTH = 2.dp
private const val ChevronOpen = 90f
private const val SummaryAlpha = 0.8f
private const val MILLIS = 1000.0
private const val SECOND = 1000L
private const val FoldDamping = 0.95f
private const val FoldStiffness = 195f
