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

@file:Suppress("TooManyFunctions")

package io.getstream.chat.android.compose.ui.messages.composer.schedule

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.StreamCardBottomSheet
import io.getstream.chat.android.compose.ui.components.button.StreamButtonStyleDefaults
import io.getstream.chat.android.compose.ui.components.button.StreamTextButton
import io.getstream.chat.android.compose.ui.components.messages.QuotedMessageAttachmentPreview
import io.getstream.chat.android.compose.ui.components.messages.rememberBodyBuilder
import io.getstream.chat.android.compose.ui.components.rememberStreamSheetState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ScheduleMessageSheetParams
import io.getstream.chat.android.compose.ui.theme.ScheduleMessageTimeSheetParams
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.feature.messages.composer.scheduled.scheduleMessageQuickDates
import io.getstream.chat.android.ui.common.state.messages.composer.ScheduleMessageError
import io.getstream.chat.android.ui.common.state.messages.composer.ScheduleMessageState
import io.getstream.result.Error
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * The default sheet with the quick dates to schedule a message, and a row that opens the custom time.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduleMessageSheet(params: ScheduleMessageSheetParams) {
    StreamCardBottomSheet(
        modifier = Modifier.testTag("Stream_ScheduleMessageSheet"),
        onDismissRequest = params.onDismiss,
    ) {
        ScheduleMessageSheetContent(params = params, now = remember { Date() })
    }
}

@Composable
internal fun ScheduleMessageSheetContent(
    params: ScheduleMessageSheetParams,
    now: Date,
    timeZone: TimeZone = TimeZone.getDefault(),
) {
    val context = LocalContext.current
    val state = params.state
    val maxPerChannel = state.maxPerChannel
    val isEnabled = !state.isChannelLimitReached
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = StreamTokens.spacingMd),
    ) {
        SheetTitle(
            text = if (maxPerChannel != null && state.isChannelLimitReached) {
                stringResource(R.string.stream_compose_schedule_message_limit_reached, maxPerChannel)
            } else {
                stringResource(R.string.stream_compose_schedule_message_title)
            },
        )
        params.dates.forEachIndexed { index, date ->
            QuickDateRow(
                modifier = Modifier.testTag("Stream_ScheduleMessageQuickDate$index"),
                text = context.formatQuickDate(date, now, timeZone),
                enabled = isEnabled,
                onClick = { params.onDateClick(date) },
            )
        }
        QuickDateRow(
            modifier = Modifier.testTag("Stream_ScheduleMessageCustomTime"),
            text = stringResource(R.string.stream_compose_schedule_message_custom_time),
            enabled = isEnabled,
            trailingIcon = painterResource(R.drawable.stream_design_ic_chevron_right),
            onClick = params.onCustomTimeClick,
        )
    }
}

@Composable
private fun QuickDateRow(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: Painter? = null,
) {
    val color = if (enabled) ChatTheme.colors.textPrimary else ChatTheme.colors.textDisabled
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = ListItemMinHeight)
            .padding(horizontal = StreamTokens.spacingMd, vertical = StreamTokens.spacingXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingSm),
    ) {
        Icon(
            painter = painterResource(R.drawable.stream_design_ic_clock),
            contentDescription = null,
            tint = color,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = ChatTheme.typography.bodyDefault,
            color = color,
        )
        trailingIcon?.let {
            Icon(painter = it, contentDescription = null, tint = color)
        }
    }
}

/**
 * The default sheet to pick the date and the time a message is sent at, to schedule a new message or to reschedule
 * one. The Date and Time rows open the Material date and time pickers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduleMessageTimeSheet(params: ScheduleMessageTimeSheetParams) {
    var date by rememberSaveable { mutableStateOf(params.initialDate.time) }
    var isDatePickerVisible by rememberSaveable { mutableStateOf(false) }
    var isTimePickerVisible by rememberSaveable { mutableStateOf(false) }
    var now by remember { mutableStateOf(Date()) }
    // The picked date can become too soon while the sheet is open.
    LaunchedEffect(Unit) {
        while (true) {
            delay(NowRefreshMillis)
            now = Date()
        }
    }
    StreamCardBottomSheet(
        modifier = Modifier.testTag("Stream_ScheduleMessageTimeSheet"),
        onDismissRequest = params.onDismiss,
        // Fully expanded, so an error shown later does not push the buttons off screen.
        sheetState = rememberStreamSheetState(
            initialValueInInspection = SheetValue.Expanded,
            skipPartiallyExpanded = true,
        ),
    ) {
        ScheduleMessageTimeSheetContent(
            params = params,
            date = Date(date),
            now = now,
            onDateClick = { isDatePickerVisible = true },
            onTimeClick = { isTimePickerVisible = true },
        )
    }
    if (isDatePickerVisible) {
        ScheduleMessageDatePicker(
            date = Date(date),
            maxDate = Date(now.time + TimeUnit.SECONDS.toMillis(params.state.maxLeadTimeSeconds.toLong())),
            onDatePicked = { date = it.time },
            onDismiss = { isDatePickerVisible = false },
        )
    }
    if (isTimePickerVisible) {
        ScheduleMessageTimePicker(
            date = Date(date),
            onTimePicked = { date = it.time },
            onDismiss = { isTimePickerVisible = false },
        )
    }
}

@Suppress("LongMethod")
@Composable
internal fun ScheduleMessageTimeSheetContent(
    params: ScheduleMessageTimeSheetParams,
    date: Date,
    now: Date,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    timeZone: TimeZone = TimeZone.getDefault(),
) {
    val context = LocalContext.current
    val isReschedule = params.scheduledAt != null
    val error = params.error ?: params.state.dateError(date, now)
    Column(modifier = Modifier.fillMaxWidth()) {
        SheetTitle(
            text = if (isReschedule) {
                stringResource(R.string.stream_compose_schedule_message_reschedule_title)
            } else {
                stringResource(R.string.stream_compose_schedule_message_title)
            },
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = StreamTokens.spacingMd, vertical = StreamTokens.spacingMd),
            verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingMd),
        ) {
            MessagePreview(
                title = params.scheduledAt?.let { scheduledAt ->
                    stringResource(
                        R.string.stream_compose_schedule_message_scheduled_for,
                        context.formatFullDate(scheduledAt, timeZone),
                    )
                } ?: params.message.replyTo?.user?.name?.takeIf(String::isNotBlank)?.let { name ->
                    stringResource(R.string.stream_compose_schedule_message_reply_to, name)
                } ?: stringResource(R.string.stream_compose_schedule_message_to, params.channelName),
                message = params.message,
            )
            error?.let {
                SheetNote(
                    modifier = Modifier
                        .testTag("Stream_ScheduleMessageError")
                        .semantics { liveRegion = LiveRegionMode.Polite },
                    icon = painterResource(R.drawable.stream_design_ic_exclamation_circle),
                    title = errorMessage(it, isReschedule),
                    accent = ChatTheme.colors.accentError,
                )
            }
            Column {
                PickerRow(
                    modifier = Modifier.testTag("Stream_ScheduleMessageDate"),
                    icon = painterResource(R.drawable.stream_design_ic_calendar),
                    label = stringResource(R.string.stream_compose_schedule_message_date),
                    value = formatPickerDate(date, timeZone),
                    onClick = onDateClick,
                )
                PickerRow(
                    modifier = Modifier.testTag("Stream_ScheduleMessageTime"),
                    icon = painterResource(R.drawable.stream_design_ic_clock),
                    label = stringResource(R.string.stream_compose_schedule_message_time),
                    value = context.formatTime(date, timeZone),
                    onClick = onTimeClick,
                )
            }
            val timeZoneNote = stringResource(
                R.string.stream_compose_schedule_message_time_zone,
                formatTimeZoneOffset(timeZone, date),
            )
            val maxPerChannel = params.state.maxPerChannel
            val countNote = if (!isReschedule && maxPerChannel != null && params.state.pendingInChannel > 0) {
                stringResource(
                    R.string.stream_compose_schedule_message_channel_count,
                    params.state.pendingInChannel,
                    maxPerChannel,
                )
            } else {
                null
            }
            SheetNote(
                icon = painterResource(R.drawable.stream_design_ic_info),
                title = countNote ?: timeZoneNote,
                subtitle = timeZoneNote.takeIf { countNote != null },
                accent = ChatTheme.colors.accentPrimary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs, Alignment.End),
            ) {
                StreamTextButton(
                    modifier = Modifier.testTag("Stream_ScheduleMessageCancel"),
                    onClick = params.onDismiss,
                    text = stringResource(R.string.stream_compose_schedule_message_cancel),
                    style = StreamButtonStyleDefaults.primaryGhost,
                )
                StreamTextButton(
                    modifier = Modifier.testTag("Stream_ScheduleMessageConfirm"),
                    // A failed request can be tried again.
                    enabled = error == null || error is ScheduleMessageError.Failed,
                    onClick = { params.onConfirm(date) },
                    text = if (isReschedule) {
                        stringResource(R.string.stream_compose_schedule_message_reschedule)
                    } else {
                        stringResource(R.string.stream_compose_schedule_message_schedule)
                    },
                )
            }
        }
    }
}

@Composable
private fun SheetTitle(text: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = StreamTokens.spacingSm, end = StreamTokens.spacingSm, bottom = StreamTokens.spacingSm)
            .semantics { heading() },
        text = text,
        style = ChatTheme.typography.headingSmall,
        color = ChatTheme.colors.textPrimary,
        textAlign = TextAlign.Center,
    )
}

/**
 * The message a date is picked for: who it is for, or when it is scheduled, and its content.
 */
@Composable
private fun MessagePreview(title: String, message: Message) {
    val bodyBuilder = rememberBodyBuilder()
    val body = remember(bodyBuilder, message) { bodyBuilder.build(message, null) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = ChatTheme.colors.chatBgOutgoing, shape = RoundedCornerShape(StreamTokens.radiusLg))
            .padding(StreamTokens.spacingXs)
            .height(IntrinsicSize.Min)
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = StreamTokens.spacing3xs),
            thickness = PreviewIndicatorWidth,
            color = ChatTheme.colors.chatReplyIndicatorOutgoing,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = PreviewMinHeight),
            verticalArrangement = Arrangement.spacedBy(StreamTokens.spacing3xs, Alignment.CenterVertically),
        ) {
            Text(
                text = title,
                style = ChatTheme.typography.metadataEmphasis,
                color = ChatTheme.colors.chatTextIncoming,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                body.iconId?.let { iconId ->
                    Icon(
                        modifier = Modifier.size(PreviewIconSize),
                        painter = painterResource(iconId),
                        contentDescription = null,
                        tint = ChatTheme.colors.chatTextIncoming,
                    )
                }
                Text(
                    modifier = Modifier.testTag("Stream_ScheduleMessagePreview"),
                    text = body.text,
                    style = ChatTheme.typography.metadataDefault,
                    color = ChatTheme.colors.chatTextIncoming,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        QuotedMessageAttachmentPreview(body)
    }
}

@Composable
private fun PickerRow(
    icon: Painter,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = ListItemMinHeight)
            .padding(vertical = StreamTokens.spacingXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingSm),
    ) {
        Icon(painter = icon, contentDescription = null, tint = ChatTheme.colors.textPrimary)
        Text(
            modifier = Modifier.weight(1f),
            text = label,
            style = ChatTheme.typography.bodyDefault,
            color = ChatTheme.colors.textPrimary,
        )
        Text(
            modifier = Modifier.width(PickerValueWidth),
            text = value,
            style = ChatTheme.typography.captionDefault,
            color = ChatTheme.colors.textTertiary,
        )
    }
}

@Composable
private fun SheetNote(
    icon: Painter,
    title: String,
    accent: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = accent.copy(alpha = NoteBackgroundAlpha),
                shape = RoundedCornerShape(StreamTokens.radiusMd),
            )
            .defaultMinSize(minHeight = NoteMinHeight)
            .padding(start = StreamTokens.spacingSm, end = StreamTokens.spacing2xs)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
    ) {
        Icon(modifier = Modifier.size(NoteIconSize), painter = icon, contentDescription = null, tint = accent)
        Column(
            modifier = Modifier.padding(
                end = StreamTokens.spacingXs,
                top = StreamTokens.spacingSm,
                bottom = StreamTokens.spacingSm,
            ),
            verticalArrangement = Arrangement.spacedBy(StreamTokens.spacing3xs),
        ) {
            Text(text = title, style = ChatTheme.typography.metadataEmphasis, color = ChatTheme.colors.textPrimary)
            subtitle?.let {
                Text(text = it, style = ChatTheme.typography.metadataDefault, color = ChatTheme.colors.textSecondary)
            }
        }
    }
}

@Composable
private fun errorMessage(error: ScheduleMessageError, isReschedule: Boolean): String = when (error) {
    is ScheduleMessageError.TooSoon -> if (isReschedule) {
        stringResource(R.string.stream_compose_schedule_message_error_too_soon_reschedule)
    } else {
        val minutes = TimeUnit.SECONDS.toMinutes(error.minLeadTimeSeconds.toLong() + MinuteSeconds - 1)
            .toInt()
            .coerceAtLeast(1)
        pluralStringResource(R.plurals.stream_compose_schedule_message_error_too_soon, minutes, minutes)
    }

    is ScheduleMessageError.TooFar -> stringResource(R.string.stream_compose_schedule_message_error_too_far)
    is ScheduleMessageError.ChannelLimitReached ->
        stringResource(R.string.stream_compose_schedule_message_error_channel_limit, error.limit)

    is ScheduleMessageError.UserLimitReached ->
        stringResource(R.string.stream_compose_schedule_message_error_user_limit, error.limit)

    ScheduleMessageError.CommandNotAllowed -> stringResource(R.string.stream_compose_schedule_message_error_command)
    else -> stringResource(R.string.stream_compose_schedule_message_error_failed)
}

/**
 * Formats a quick date as "Tomorrow at 09:00", or with the name of its day, like "Monday at 09:00".
 */
internal fun Context.formatQuickDate(date: Date, now: Date, timeZone: TimeZone = TimeZone.getDefault()): String {
    val time = formatTime(date, timeZone)
    return if (daysBetween(now, date, timeZone) == 1) {
        getString(R.string.stream_compose_schedule_message_tomorrow_at, time)
    } else {
        val day = SimpleDateFormat("EEEE", Locale.getDefault()).apply { this.timeZone = timeZone }.format(date)
        getString(R.string.stream_compose_schedule_message_day_at, day, time)
    }
}

/**
 * Formats a date with its day and its time, like "Fri, Oct 23 at 09:00".
 */
private fun Context.formatFullDate(date: Date, timeZone: TimeZone): String =
    getString(
        R.string.stream_compose_schedule_message_day_at,
        formatPickerDate(date, timeZone),
        formatTime(date, timeZone),
    )

private fun Context.formatTime(date: Date, timeZone: TimeZone): String =
    DateFormat.getTimeFormat(this).apply { this.timeZone = timeZone }.format(date)

private fun formatPickerDate(date: Date, timeZone: TimeZone): String {
    val locale = Locale.getDefault()
    return SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "EEEMMMd"), locale)
        .apply { this.timeZone = timeZone }
        .format(date)
}

/**
 * The offset of [timeZone] at [date], like "GMT+2" or "GMT+5:30".
 */
internal fun formatTimeZoneOffset(timeZone: TimeZone, date: Date): String {
    val offsetMinutes = TimeUnit.MILLISECONDS.toMinutes(timeZone.getOffset(date.time).toLong())
    val sign = if (offsetMinutes < 0) "-" else "+"
    val hours = abs(offsetMinutes) / MinuteSeconds
    val minutes = abs(offsetMinutes) % MinuteSeconds
    return when {
        offsetMinutes == 0L -> "GMT"
        minutes == 0L -> "GMT$sign$hours"
        else -> String.format(Locale.ROOT, "GMT%s%d:%02d", sign, hours, minutes)
    }
}

/**
 * The number of calendar days from [from] to [to] in [timeZone].
 */
private fun daysBetween(from: Date, to: Date, timeZone: TimeZone): Int {
    fun Date.dayIndex(): Long {
        val calendar = Calendar.getInstance(timeZone).apply { time = this@dayIndex }
        val offset = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)
        return TimeUnit.MILLISECONDS.toDays(time + offset)
    }
    return (to.dayIndex() - from.dayIndex()).toInt()
}

private const val NowRefreshMillis = 1_000L
private const val MinuteSeconds = 60L
private const val NoteBackgroundAlpha = 0.1f
private val PreviewIndicatorWidth = 2.dp
private val PreviewMinHeight = 40.dp
private val PreviewIconSize = 12.dp
private val NoteIconSize = 20.dp
private val NoteMinHeight = 46.dp
private val ListItemMinHeight = 48.dp
private val PickerValueWidth = 120.dp

@Preview(showBackground = true)
@Composable
private fun ScheduleMessageSheetPreview() {
    ChatTheme {
        ScheduleMessageSheetContent()
    }
}

@Composable
internal fun ScheduleMessageSheetContent() {
    PreviewQuickSheet(
        state = ScheduleMessageState(canSchedule = true, pendingInChannel = 2, maxPerChannel = 10),
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduleMessageSheetLimitReachedPreview() {
    ChatTheme {
        ScheduleMessageSheetLimitReachedContent()
    }
}

@Composable
internal fun ScheduleMessageSheetLimitReachedContent() {
    PreviewQuickSheet(
        state = ScheduleMessageState(canSchedule = true, pendingInChannel = 10, maxPerChannel = 10),
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduleMessageTimeSheetPreview() {
    ChatTheme {
        ScheduleMessageTimeSheetContent()
    }
}

@Composable
internal fun ScheduleMessageTimeSheetContent() {
    PreviewTimeSheet(
        state = ScheduleMessageState(canSchedule = true, pendingInChannel = 2, maxPerChannel = 10),
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduleMessageTimeSheetWithoutCountPreview() {
    ChatTheme {
        ScheduleMessageTimeSheetWithoutCountContent()
    }
}

@Composable
internal fun ScheduleMessageTimeSheetWithoutCountContent() {
    PreviewTimeSheet(
        state = ScheduleMessageState(canSchedule = true, maxPerChannel = 10),
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduleMessageTimeSheetTooSoonPreview() {
    ChatTheme {
        ScheduleMessageTimeSheetTooSoonContent()
    }
}

@Composable
internal fun ScheduleMessageTimeSheetTooSoonContent() {
    PreviewTimeSheet(
        state = ScheduleMessageState(canSchedule = true),
        date = Date(PreviewNow.time + PreviewTooSoonMillis),
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduleMessageTimeSheetTooFarPreview() {
    ChatTheme {
        ScheduleMessageTimeSheetTooFarContent()
    }
}

@Composable
internal fun ScheduleMessageTimeSheetTooFarContent() {
    PreviewTimeSheet(
        state = ScheduleMessageState(canSchedule = true, maxLeadTimeSeconds = PreviewShortMaxLeadTimeSeconds),
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduleMessageTimeSheetUserLimitPreview() {
    ChatTheme {
        ScheduleMessageTimeSheetUserLimitContent()
    }
}

@Composable
internal fun ScheduleMessageTimeSheetUserLimitContent() {
    PreviewTimeSheet(
        state = ScheduleMessageState(canSchedule = true, pendingForUser = 100, maxPerUser = 100),
        error = ScheduleMessageError.UserLimitReached(limit = 100),
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduleMessageTimeSheetCommandPreview() {
    ChatTheme {
        ScheduleMessageTimeSheetCommandContent()
    }
}

@Composable
internal fun ScheduleMessageTimeSheetCommandContent() {
    PreviewTimeSheet(
        state = ScheduleMessageState(canSchedule = true, hasCommands = true),
        message = Message(text = "/giphy launch day"),
        error = ScheduleMessageError.CommandNotAllowed,
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduleMessageTimeSheetFailedPreview() {
    ChatTheme {
        ScheduleMessageTimeSheetFailedContent()
    }
}

@Composable
internal fun ScheduleMessageTimeSheetFailedContent() {
    PreviewTimeSheet(
        state = ScheduleMessageState(canSchedule = true),
        error = ScheduleMessageError.Failed(Error.GenericError("Failed")),
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduleMessageTimeSheetReplyPreview() {
    ChatTheme {
        ScheduleMessageTimeSheetReplyContent()
    }
}

@Composable
internal fun ScheduleMessageTimeSheetReplyContent() {
    PreviewTimeSheet(
        state = ScheduleMessageState(canSchedule = true),
        message = Message(
            text = "@Maya Ross, can you check the final plan?",
            replyTo = Message(user = User(id = "alex", name = "Alex Ross")),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduleMessageTimeSheetReschedulePreview() {
    ChatTheme {
        ScheduleMessageTimeSheetRescheduleContent()
    }
}

@Composable
internal fun ScheduleMessageTimeSheetRescheduleContent() {
    PreviewTimeSheet(
        state = ScheduleMessageState(canSchedule = true, pendingInChannel = 2),
        scheduledAt = PreviewTomorrow,
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduleMessageTimeSheetRescheduleTooSoonPreview() {
    ChatTheme {
        ScheduleMessageTimeSheetRescheduleTooSoonContent()
    }
}

@Composable
internal fun ScheduleMessageTimeSheetRescheduleTooSoonContent() {
    PreviewTimeSheet(
        state = ScheduleMessageState(canSchedule = true),
        date = Date(PreviewNow.time + PreviewTooSoonMillis),
        scheduledAt = PreviewTomorrow,
    )
}

@Composable
private fun PreviewQuickSheet(state: ScheduleMessageState) {
    Column(modifier = Modifier.background(ChatTheme.colors.backgroundCoreElevation1)) {
        ScheduleMessageSheetContent(
            params = ScheduleMessageSheetParams(
                dates = scheduleMessageQuickDates(PreviewNow, PreviewTimeZone),
                state = state,
                onDateClick = {},
                onCustomTimeClick = {},
                onDismiss = {},
            ),
            now = PreviewNow,
            timeZone = PreviewTimeZone,
        )
    }
}

@Composable
private fun PreviewTimeSheet(
    state: ScheduleMessageState,
    message: Message = Message(text = "Heads up: the launch moved to Monday. I'll post the final plan there."),
    date: Date = PreviewTomorrow,
    error: ScheduleMessageError? = null,
    scheduledAt: Date? = null,
) {
    Column(modifier = Modifier.background(ChatTheme.colors.backgroundCoreElevation1)) {
        ScheduleMessageTimeSheetContent(
            params = ScheduleMessageTimeSheetParams(
                message = message,
                channelName = "Maya Ross",
                initialDate = date,
                state = state,
                error = error,
                onConfirm = {},
                onDismiss = {},
                scheduledAt = scheduledAt,
            ),
            date = date,
            now = PreviewNow,
            onDateClick = {},
            onTimeClick = {},
            timeZone = PreviewTimeZone,
        )
    }
}

private val PreviewTimeZone: TimeZone = TimeZone.getTimeZone("UTC")

/** Saturday, January 16, 2027 at 10:00 UTC. */
private val PreviewNow = Date(PreviewNowMillis)

/** Sunday, January 17, 2027 at 09:00 UTC. */
private val PreviewTomorrow = Date(PreviewTomorrowMillis)

private const val PreviewNowMillis = 1_800_093_600_000L
private const val PreviewTomorrowMillis = 1_800_176_400_000L
private const val PreviewTooSoonMillis = 30_000L
private const val PreviewShortMaxLeadTimeSeconds = 3600
