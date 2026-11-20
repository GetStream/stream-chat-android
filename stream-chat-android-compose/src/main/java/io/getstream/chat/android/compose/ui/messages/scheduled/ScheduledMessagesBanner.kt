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

package io.getstream.chat.android.compose.ui.messages.scheduled

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.messages.composer.schedule.formatFullDate
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.ui.common.state.messages.composer.ScheduledMessagesBannerState
import java.util.Date
import java.util.TimeZone

/**
 * The banner about the scheduled messages of a channel or a thread, which floats above the composer. Its changes are
 * announced politely to screen readers.
 *
 * @param state What the banner shows.
 * @param onClick Called when the banner is clicked, to show the scheduled messages. The banner can't be clicked and
 * has no View link when it is null.
 * @param modifier Modifier for styling.
 * @param timeZone The time zone of the date of the next scheduled message.
 */
@Composable
internal fun ScheduledMessagesBanner(
    state: ScheduledMessagesBannerState,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    timeZone: TimeZone = TimeZone.getDefault(),
) {
    val isFailed = state is ScheduledMessagesBannerState.Failed
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = StreamTokens.elevation3, shape = CircleShape)
            .background(ChatTheme.colors.backgroundCoreElevation2)
            .border(width = 1.dp, color = ChatTheme.colors.borderCoreDefault, shape = CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }
            .testTag("Stream_ScheduledMessagesBanner")
            .padding(horizontal = StreamTokens.spacingMd, vertical = StreamTokens.spacingSm),
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(IconSize),
            painter = painterResource(
                if (isFailed) R.drawable.stream_design_ic_exclamation_circle else R.drawable.stream_design_ic_clock,
            ),
            contentDescription = null,
            tint = if (isFailed) ChatTheme.colors.accentError else ChatTheme.colors.accentPrimary,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = state.text(timeZone),
            style = ChatTheme.typography.metadataEmphasis,
            color = ChatTheme.colors.textPrimary,
        )
        if (onClick != null) {
            Text(
                text = stringResource(
                    if (state.isSingle) {
                        R.string.stream_compose_scheduled_messages_view
                    } else {
                        R.string.stream_compose_scheduled_messages_view_all
                    },
                ),
                style = ChatTheme.typography.metadataDefault,
                color = ChatTheme.colors.textLink,
            )
        }
    }
}

@Composable
private fun ScheduledMessagesBannerState.text(timeZone: TimeZone): String = when (this) {
    is ScheduledMessagesBannerState.Failed ->
        pluralStringResource(R.plurals.stream_compose_scheduled_messages_failed_count, count, count)

    is ScheduledMessagesBannerState.LimitReached ->
        stringResource(R.string.stream_compose_scheduled_messages_limit_reached, count, limit)

    is ScheduledMessagesBannerState.Scheduled -> when (count) {
        1 -> stringResource(
            R.string.stream_compose_schedule_message_scheduled_for,
            LocalContext.current.formatFullDate(nextScheduledAt, timeZone),
        )
        else -> if (isInThread) {
            pluralStringResource(R.plurals.stream_compose_scheduled_messages_replies_count, count, count)
        } else {
            pluralStringResource(R.plurals.stream_compose_scheduled_messages_count, count, count)
        }
    }

    else -> ""
}

/** Whether the banner is about a single message, so its link says View rather than View all. */
private val ScheduledMessagesBannerState.isSingle: Boolean
    get() = when (this) {
        is ScheduledMessagesBannerState.Failed -> count == 1
        is ScheduledMessagesBannerState.Scheduled -> count == 1
        else -> false
    }

private val IconSize = 20.dp

@Preview(showBackground = true)
@Composable
private fun ScheduledMessagesBannerPreview() {
    ChatTheme {
        ScheduledMessagesBannerContent()
    }
}

@Composable
internal fun ScheduledMessagesBannerContent() {
    Column(
        modifier = Modifier.padding(StreamTokens.spacingMd),
        verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingMd),
    ) {
        PreviewBanner(ScheduledMessagesBannerState.Scheduled(1, PreviewDate))
        PreviewBanner(
            ScheduledMessagesBannerState.Scheduled(count = 3, nextScheduledAt = PreviewDate),
        )
        PreviewBanner(ScheduledMessagesBannerState.Scheduled(2, PreviewDate, parentMessageId = "thread"))
        PreviewBanner(ScheduledMessagesBannerState.Failed(1))
        PreviewBanner(ScheduledMessagesBannerState.Failed(2))
        PreviewBanner(ScheduledMessagesBannerState.LimitReached(count = 10, limit = 10))
    }
}

@Preview(showBackground = true)
@Composable
private fun ScheduledMessagesBannerWithoutLinkPreview() {
    ChatTheme {
        ScheduledMessagesBannerWithoutLinkContent()
    }
}

@Composable
internal fun ScheduledMessagesBannerWithoutLinkContent() {
    Column(modifier = Modifier.padding(StreamTokens.spacingMd)) {
        PreviewBanner(ScheduledMessagesBannerState.Scheduled(1, PreviewDate), onClick = null)
    }
}

@Composable
private fun PreviewBanner(state: ScheduledMessagesBannerState, onClick: (() -> Unit)? = {}) {
    ScheduledMessagesBanner(state = state, onClick = onClick, timeZone = TimeZone.getTimeZone("UTC"))
}

/** Friday, October 23, 2026 at 09:00 UTC. */
private val PreviewDate = Date(PreviewDateMillis)
private const val PreviewDateMillis = 1_792_746_000_000L
