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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.R
import io.getstream.chat.android.ai.compose.parts.AIToolCallPart
import io.getstream.chat.android.ai.compose.ui.component.internal.formatToolDuration
import io.getstream.chat.android.ai.compose.ui.component.internal.rememberDurationStrings
import io.getstream.chat.android.ai.compose.ui.component.internal.shimmer

/**
 * One tool call: what it is doing, where it runs, and how it went.
 *
 * @param part The tool call.
 * @param modifier The modifier to apply to the view.
 * @param textStyle The style of the call's title. Its outcome uses a smaller style.
 * @param colors The palette of the view.
 */
@Composable
public fun AIToolCall(
    part: AIToolCallPart,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    colors: AIToolCallColors = AIToolCallDefaults.colors(),
) {
    val status = toolCallStatus(part)
    val detail = part.summary ?: status.text.takeIf { status.shownAsDetail }
    // The icon carries the status for TalkBack unless the detail already says it.
    val statusDescription = status.text.takeUnless { status.shownAsDetail && part.summary == null }
    val duration = part.durationSeconds?.takeIf { part.status.isFinished }
    val durationStrings = rememberDurationStrings()
    val lineHeight = firstLineHeight(textStyle)
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(lineHeight),
            contentAlignment = Alignment.Center,
        ) {
            ToolCallIcon(part, colors, description = statusDescription)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = part.displayTitle ?: part.name.replace('_', ' '),
                style = textStyle,
                color = colors.title,
            )
            if (detail != null) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.detail,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (duration != null) {
            Box(modifier = Modifier.height(lineHeight), contentAlignment = Alignment.Center) {
                Text(
                    text = formatToolDuration(duration, durationStrings),
                    style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                    color = colors.detail,
                )
            }
        }
    }
}

/**
 * The palette of [AIToolCall].
 *
 * @param title Color of what a call is doing, such as "Checking your location".
 * @param detail Color of a call's outcome, its duration and the placeholder for unknown steps.
 * @param accent Color of a call in progress, including one waiting for a device.
 * @param success Color of a completed call's check mark.
 * @param failure Color of a failed call's mark.
 */
@Immutable
public data class AIToolCallColors(
    val title: Color,
    val detail: Color,
    val accent: Color,
    val success: Color,
    val failure: Color,
)

/** Defaults of [AIToolCall]. */
public object AIToolCallDefaults {

    /**
     * The default palette, from the current [MaterialTheme].
     *
     * @param title Color of what a call is doing.
     * @param detail Color of a call's outcome, its duration and the placeholder for unknown steps.
     * @param accent Color of a call in progress, including one waiting for a device.
     * @param success Color of a completed call's check mark.
     * @param failure Color of a failed call's mark.
     */
    @Composable
    public fun colors(
        title: Color = MaterialTheme.colorScheme.onSurface,
        detail: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        accent: Color = MaterialTheme.colorScheme.primary,
        success: Color = Color(SuccessGreen),
        failure: Color = MaterialTheme.colorScheme.error,
    ): AIToolCallColors = AIToolCallColors(
        title = title,
        detail = detail,
        accent = accent,
        success = success,
        failure = failure,
    )
}

/** A step from a newer SDK: say that something happened without guessing what. */
@Composable
internal fun UnsupportedPart(textStyle: TextStyle, colors: AIToolCallColors, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.stream_ai_compose_ic_sparkles),
            contentDescription = null,
            tint = colors.detail,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = stringResource(R.string.stream_ai_compose_part_unsupported),
            style = textStyle,
            color = colors.detail,
        )
    }
}

/**
 * A call's status in words. Waiting, failed and cancelled calls show it as their detail when the
 * agent sends no summary.
 */
private data class ToolCallStatus(val text: String, val shownAsDetail: Boolean)

@Composable
private fun toolCallStatus(part: AIToolCallPart): ToolCallStatus = if (part.isDeclined) {
    ToolCallStatus(stringResource(R.string.stream_ai_compose_tool_call_declined), shownAsDetail = true)
} else {
    statusOf(part.status)
}

@Composable
private fun statusOf(status: AIToolCallPart.Status): ToolCallStatus = when (status) {
    AIToolCallPart.Status.AwaitingApproval ->
        ToolCallStatus(stringResource(R.string.stream_ai_compose_tool_call_awaiting_approval), shownAsDetail = true)
    AIToolCallPart.Status.AwaitingClient ->
        ToolCallStatus(stringResource(R.string.stream_ai_compose_tool_call_awaiting_client), shownAsDetail = true)
    AIToolCallPart.Status.Failed ->
        ToolCallStatus(stringResource(R.string.stream_ai_compose_tool_call_failed), shownAsDetail = true)
    AIToolCallPart.Status.Cancelled ->
        ToolCallStatus(stringResource(R.string.stream_ai_compose_tool_call_cancelled), shownAsDetail = true)
    AIToolCallPart.Status.Completed ->
        ToolCallStatus(stringResource(R.string.stream_ai_compose_tool_call_completed), shownAsDetail = false)
    else -> ToolCallStatus(stringResource(R.string.stream_ai_compose_tool_call_running), shownAsDetail = false)
}

/** A status this SDK doesn't know reads as still in progress. */
@Composable
private fun ToolCallIcon(part: AIToolCallPart, colors: AIToolCallColors, description: String?) {
    when (part.status) {
        AIToolCallPart.Status.AwaitingApproval -> StatusIcon(
            R.drawable.stream_ai_compose_ic_hand,
            colors.accent,
            15.dp,
            description,
            Modifier.shimmer(true, colors.title),
        )
        AIToolCallPart.Status.AwaitingClient -> StatusIcon(
            R.drawable.stream_ai_compose_ic_device,
            colors.accent,
            16.dp,
            description,
            Modifier.shimmer(true, colors.title),
        )
        AIToolCallPart.Status.Completed ->
            StatusIcon(
                R.drawable.stream_ai_compose_ic_check,
                colors.success,
                14.dp,
                description,
            )
        AIToolCallPart.Status.Failed ->
            StatusIcon(
                R.drawable.stream_ai_compose_ic_exclamation,
                colors.failure,
                14.dp,
                description,
            )
        AIToolCallPart.Status.Cancelled ->
            StatusIcon(
                R.drawable.stream_ai_compose_ic_close,
                colors.detail,
                14.dp,
                description,
            )
        // The indicator's own progress semantics would keep the status out of the merged call.
        else -> Box(Modifier.semantics { description?.let { contentDescription = it } }) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(12.dp)
                    .clearAndSetSemantics {},
                color = colors.accent,
                strokeWidth = 1.5.dp,
            )
        }
    }
}

@Composable
private fun StatusIcon(
    @DrawableRes icon: Int,
    tint: Color,
    size: Dp,
    description: String?,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(icon),
        contentDescription = description,
        tint = tint,
        modifier = modifier.size(size),
    )
}

/** The height of the first line of [style], so icons sit centered on it. */
@Composable
private fun firstLineHeight(style: TextStyle): Dp = with(LocalDensity.current) {
    // Only sp converts to dp; em is relative to the font size.
    val fontSize = style.fontSize.takeIf { it.isSp }
    when {
        style.lineHeight.isSp -> style.lineHeight.toDp()
        style.lineHeight.isEm && fontSize != null -> (fontSize * style.lineHeight.value).toDp()
        fontSize != null -> (fontSize * LineHeightFactor).toDp()
        else -> DEFAULT_LINE_HEIGHT
    }
}

private const val SuccessGreen = 0xFF34A853
private const val LineHeightFactor = 1.4f
private val DEFAULT_LINE_HEIGHT = 20.dp
